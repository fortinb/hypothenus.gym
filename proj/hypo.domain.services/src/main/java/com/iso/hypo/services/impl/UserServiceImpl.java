package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.Message;
import com.iso.hypo.domain.aggregate.User;
import com.iso.hypo.domain.dto.UserDto;
import com.iso.hypo.domain.enumeration.MessageSeverityEnum;
import com.iso.hypo.domain.security.RoleEnum;
import com.iso.hypo.domain.security.Roles;
import com.iso.hypo.events.event.OperationEnum;
import com.iso.hypo.repositories.UserRepository;
import com.iso.hypo.services.UserService;
import com.iso.hypo.services.clients.AzureGraphClientService;
import com.iso.hypo.services.event.UserEvent;
import com.iso.hypo.services.exception.UserException;
import com.iso.hypo.services.mappers.UserMapper;
import com.microsoft.graph.models.AppRoleAssignment;
import com.microsoft.graph.models.PasswordProfile;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	private final UserMapper userMapper;

	private final AzureGraphClientService azureGraphClientService;

	private final ApplicationEventPublisher eventPublisher;

	@Value("${app.test.run:false}")
	private boolean testRun;

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	private final RequestContext requestContext;

	public UserServiceImpl(UserMapper userMapper, UserRepository userRepository,
			ApplicationEventPublisher eventPublisher, AzureGraphClientService azureGraphClientService,
			RequestContext requestContext) {
		this.userMapper = userMapper;
		this.userRepository = userRepository;
		this.azureGraphClientService = azureGraphClientService;
		this.eventPublisher = eventPublisher;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	@Transactional
	public UserDto create(UserDto userDto) throws UserException {
		try {
			Assert.notNull(userDto, "userDto must not be null");

			User user = userMapper.toEntity(userDto);

			Optional<User> existingUser = userRepository.findByEmailAndIsDeletedIsFalse(user.getEmail());
			if (existingUser.isPresent()) {
				Message message = new Message();
				message.setCode(UserException.USER_ALREADY_EXIST);
				message.setDescription("Duplicate user");
				message.setSeverity(MessageSeverityEnum.warning);
				user.getMessages().add(message);

				throw new UserException(requestContext.getTrackingNumber(), UserException.USER_ALREADY_EXIST,
						"Duplicate user", userMapper.toDto(user));
			}
			
			if (!testRun) {
				// Find user in identity provider with same email
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.userExists(user.getEmail());
				if (idpUser.isPresent()) {
					Message message = new Message();
					message.setCode(UserException.USER_ALREADY_EXIST);
					message.setDescription("Duplicate user");
					message.setSeverity(MessageSeverityEnum.warning);
					user.getMessages().add(message);

					throw new UserException(requestContext.getTrackingNumber(), UserException.USER_ALREADY_EXIST,
							"Duplicate user", userMapper.toDto(user));
				}
			}

			// Create user
			user.setUuid(UUID.randomUUID().toString());
			user.setCreatedOn(Instant.now());
			user.setCreatedBy(requestContext.getUsername());
			
			if (!testRun) {
				// Create user in identity provider
				com.microsoft.graph.models.User newUser = new com.microsoft.graph.models.User();

				newUser.setAccountEnabled(true);
				newUser.setDisplayName(user.getFirstname() + " " + user.getLastname());
				newUser.setGivenName(user.getFirstname());
				newUser.setSurname(user.getLastname());
				newUser.setMailNickname(user.getUuid());
				newUser.setMail(user.getEmail());
				newUser.setUserPrincipalName(user.getUuid());
				newUser.setPasswordProfile(new PasswordProfile());
				newUser.getPasswordProfile().setForceChangePasswordNextSignIn(true);
				newUser.getPasswordProfile().setPassword("change.test.1");
				
				com.microsoft.graph.models.User createdUser = azureGraphClientService.createUser(newUser);

				// Verify security level
				if (!Roles.isRolesAssignmentAllowed(requestContext.getRoles(), null, userDto.getRoles())) {
					Message message = new Message();
					message.setCode(UserException.ROLE_ASSIGNMENT_NOT_ALLOWED);
					message.setDescription("Role assignment not allowed");
					message.setSeverity(MessageSeverityEnum.warning);
					userDto.getMessages().add(message);

					throw new UserException(requestContext.getTrackingNumber(), UserException.ROLE_ASSIGNMENT_NOT_ALLOWED,
							"Role assignment not allowed", userDto);
				}
				
				// Assign roles to user
				for (RoleEnum role : userDto.getRoles()) {
					azureGraphClientService.assignRole(createdUser.getId(), role.toString());
				}
				
				user.setIdpId(createdUser.getId());
				user.setUpn(createdUser.getUserPrincipalName());
			} else {
				// For test run, generate random UUID for idpId and use email as upn
				user.setIdpId(UUID.randomUUID().toString());
				user.setUpn(userDto.getEmail());
			}
			
			User saved = userRepository.save(user);
			return userMapper.toDto(saved);
		} catch (

		Exception e) {
			logger.error("Error - email={}", userDto != null ? userDto.getEmail() : null, e);

			if (e instanceof UserException) {
				throw (UserException) e;
			}
			throw new UserException(requestContext.getTrackingNumber(), UserException.CREATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public UserDto update(UserDto userDto) throws UserException {
		try {
			return updateUser(userDto, false);
		} catch (Exception e) {
			logger.error("Error - userUuid={}", userDto != null ? userDto.getUuid() : null, e);

			if (e instanceof UserException) {
				throw (UserException) e;
			}
			throw new UserException(requestContext.getTrackingNumber(), UserException.UPDATE_FAILED, e);
		}
	}

	@Override
	public UserDto patch(UserDto userDto) throws UserException {
		try {
			return updateUser(userDto, true);
		} catch (Exception e) {
			logger.error("Error - userUuid={}", userDto != null ? userDto.getUuid() : null, e);

			if (e instanceof UserException) {
				throw (UserException) e;
			}
			throw new UserException(requestContext.getTrackingNumber(), UserException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public UserDto activate(String userUuid) throws UserException {
		try {
			Optional<User> entity = userRepository.activate(userUuid);
			if (entity.isEmpty()) {
				throw new UserException(requestContext.getTrackingNumber(), UserException.USER_NOT_FOUND,
						"User not found");
			}

			if (!testRun) {
				// Find user in identity provider with same email
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.findUser(entity.get().getIdpId());
				if (idpUser.isPresent()) {
					// Deactivate user in identity provider
					idpUser.get().setAccountEnabled(true);
					azureGraphClientService.updateUser(idpUser.get());
				}
			}
			
			return userMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - userUuid={}", userUuid, e);

			if (e instanceof UserException) {
				throw (UserException) e;
			}
			throw new UserException(requestContext.getTrackingNumber(), UserException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public UserDto deactivate(String userUuid) throws UserException {
		try {
			Optional<User> entity = userRepository.deactivate(userUuid);
			if (entity.isEmpty()) {
				throw new UserException(requestContext.getTrackingNumber(), UserException.USER_NOT_FOUND,
						"User not found");
			}

			if (!testRun) {
				// Find user in identity provider with same email
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.findUser(entity.get().getIdpId());
				if (idpUser.isPresent()) {
					// Deactivate user in identity provider
					idpUser.get().setAccountEnabled(false);
					azureGraphClientService.updateUser(idpUser.get());
				}
			}
			
			return userMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - userUuid={}", userUuid, e);

			if (e instanceof UserException) {
				throw (UserException) e;
			}
			throw new UserException(requestContext.getTrackingNumber(), UserException.DEACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public void delete(String userUuid) throws UserException {
		try {
			User entity = this.readByUserUuid(userUuid);
			userRepository.delete(entity.getUuid(), requestContext.getUsername());
			
			if (!testRun) {
				// Find user in identity provider with same email
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.findUser(entity.getIdpId());
				if (idpUser.isPresent()) {
					// Delete user in identity provider
					azureGraphClientService.deleteUser(idpUser.get().getId());
				}
			}
				
			eventPublisher.publishEvent(new UserEvent(this, entity, OperationEnum.delete));
		} catch (Exception e) {
			logger.error("Error - userUuid={}", userUuid, e);

			if (e instanceof UserException) {
				throw (UserException) e;
			}
			throw new UserException(requestContext.getTrackingNumber(), UserException.DELETE_FAILED, e);
		}
	}

	private UserDto updateUser(UserDto userDto, boolean skipNull) throws UserException {
		try {
			Assert.notNull(userDto, "userDto must not be null");

			User oldUser = this.readByUserUuid(userDto.getUuid());

			if (userDto.getEmail() != null && !userDto.getEmail().equals(oldUser.getEmail())) {
				Optional<User> existingUser = userRepository.findByEmailAndIsDeletedIsFalse(userDto.getEmail());
				if (existingUser.isPresent()) {
					Message message = new Message();
					message.setCode(UserException.USER_ALREADY_EXIST);
					message.setDescription("Duplicate user");
					message.setSeverity(MessageSeverityEnum.warning);
					userDto.getMessages().add(message);

					throw new UserException(requestContext.getTrackingNumber(), UserException.USER_ALREADY_EXIST,
							"Duplicate user", userDto);
				}
			}

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(skipNull).setCollectionsMergeEnabled(false);

			mapper = userMapper.initUserMappings(mapper);
			mapper.map(userDto, oldUser);

			oldUser.setModifiedOn(Instant.now());
			oldUser.setModifiedBy(requestContext.getUsername());
			
			if (!testRun) {
				List<RoleEnum> sourceRoles = new java.util.ArrayList<RoleEnum>();
				List<RoleEnum> targetRoles = userDto.getRoles() != null ? userDto.getRoles() : new java.util.ArrayList<RoleEnum>();
				
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.findUser(oldUser.getIdpId());
				if (idpUser.isPresent()) {
					// Update user in identity provider
					idpUser.get().setDisplayName(oldUser.getFirstname() + " " + oldUser.getLastname());
					idpUser.get().setGivenName(oldUser.getFirstname());
					idpUser.get().setSurname(oldUser.getLastname());
					idpUser.get().setMail(oldUser.getEmail());
					azureGraphClientService.updateUser(idpUser.get());
					
					for (AppRoleAssignment appRoleAssignment : idpUser.get().getAppRoleAssignments()) {
						sourceRoles.add(RoleEnum.valueOf(azureGraphClientService.getRole(appRoleAssignment).getValue()));
					}
				}

				// Update roles if provided in request
				if (targetRoles != null) {
					if (!Roles.isRolesAssignmentAllowed(requestContext.getRoles(), sourceRoles, targetRoles)) {
						Message message = new Message();
						message.setCode(UserException.ROLE_ASSIGNMENT_NOT_ALLOWED);
						message.setDescription("Role assignment not allowed");
						message.setSeverity(MessageSeverityEnum.warning);
						userDto.getMessages().add(message);

						throw new UserException(requestContext.getTrackingNumber(), UserException.ROLE_ASSIGNMENT_NOT_ALLOWED,
								"Role assignment not allowed", userDto);
					}
					
					// Remove roles from user
					for (RoleEnum sourceRole : sourceRoles) {
						if (!oldUser.getRoles().contains(sourceRole)) {
							azureGraphClientService.unassignRole(idpUser.get().getId(), sourceRole.toString());
						}
					}
					
					// Add roles from request
					for (RoleEnum targetRole : targetRoles) {
						if (!sourceRoles.contains(targetRole)) {
							azureGraphClientService.assignRole(idpUser.get().getId(), targetRole.toString());
						}
					}
				}
			}
			User saved = userRepository.save(oldUser);
			return userMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - userUuid={}", userDto != null ? userDto.getUuid() : null, e);

			if (e instanceof UserException) {
				throw (UserException) e;
			}
			throw new UserException(requestContext.getTrackingNumber(), UserException.UPDATE_FAILED, e);
		}
	}
	
	private User readByUserUuid(String userUuid) throws UserException {
		Optional<User> entity = userRepository.findByUuidAndIsDeletedIsFalse(userUuid);
		if (entity.isEmpty()) {
			throw new UserException(requestContext.getTrackingNumber(), UserException.USER_NOT_FOUND, "User not found");
		}

		return entity.get();
	}
}
