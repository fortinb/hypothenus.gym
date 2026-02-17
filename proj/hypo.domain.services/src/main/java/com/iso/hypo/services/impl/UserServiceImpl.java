package com.iso.hypo.services.impl;

import java.time.Instant;
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
import com.microsoft.graph.models.AppRole;
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
					// Delete user in identity provider
					//azureGraphClientService.deleteUser(idpUser.get().getId());
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

			User saved = userRepository.save(user);
			UserDto savedDto = userMapper.toDto(saved);
			
			if (!testRun) {
				// Create user in identity provider
				com.microsoft.graph.models.User newUser = new com.microsoft.graph.models.User();

				newUser.setAccountEnabled(false);
				newUser.setDisplayName(user.getFirstname() + " " + user.getLastname());
				newUser.setGivenName(user.getFirstname());
				newUser.setSurname(user.getLastname());
				newUser.setMailNickname(user.getUuid());
				newUser.setUserPrincipalName(user.getUuid());
				newUser.setPasswordProfile(new PasswordProfile());
				newUser.getPasswordProfile().setForceChangePasswordNextSignIn(true);
				newUser.getPasswordProfile().setPassword(null); // Let Azure generate random password
				com.microsoft.graph.models.User createdUser = azureGraphClientService.createUser(newUser);

				// Verify security level
				if (!Roles.isRolesAssignmentAllowed(requestContext.getRoles(), userDto.getRoles())) {
					Message message = new Message();
					message.setCode(UserException.ROLE_ASSIGNMENT_NOT_ALLOWED);
					message.setDescription("Role assignment not allowed");
					message.setSeverity(MessageSeverityEnum.warning);
					userDto.getMessages().add(message);

					throw new UserException(requestContext.getTrackingNumber(), UserException.ROLE_ASSIGNMENT_NOT_ALLOWED,
							"Role assignment not allowed", userDto);
				}
				
				// Assign roles to user
				savedDto.setRoles(new java.util.ArrayList<RoleEnum>());
				for (RoleEnum role : userDto.getRoles()) {
					AppRole appRole = azureGraphClientService.assignRole(createdUser.getId(), role.toString());
					savedDto.getRoles().add(RoleEnum.valueOf(appRole.getValue()));
				}

				user.setIdpId(createdUser.getId());
				user.setUpn(createdUser.getUserPrincipalName());
			} else {
				// For test run, generate random UUID for idpId and use email as upn
				savedDto.setRoles(userDto.getRoles());
			}

			return savedDto;
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
			Optional<User> userOpt = userRepository.activate(userUuid);
			if (userOpt.isEmpty()) {
				throw new UserException(requestContext.getTrackingNumber(), UserException.USER_NOT_FOUND,
						"User not found");
			}

			return userMapper.toDto(userOpt.get());
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
			Optional<User> userOpt = userRepository.deactivate(userUuid);
			if (userOpt.isEmpty()) {
				throw new UserException(requestContext.getTrackingNumber(), UserException.USER_NOT_FOUND,
						"User not found");
			}

			return userMapper.toDto(userOpt.get());
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

			User saved = userRepository.save(oldUser);
			UserDto savedDto = userMapper.toDto(saved);
			
			if (!testRun) {
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.findUser(oldUser.getIdpId());
				if (idpUser.isPresent()) {
					// Update user in identity provider
					idpUser.get().setDisplayName(oldUser.getFirstname() + " " + oldUser.getLastname());
					idpUser.get().setGivenName(oldUser.getFirstname());
					idpUser.get().setSurname(oldUser.getLastname());
					idpUser.get().setMail(oldUser.getEmail());
					azureGraphClientService.updateUser(idpUser.get());
				}

				// Update roles if provided in request
				if (userDto.getRoles() != null) {
					if (!Roles.isRolesAssignmentAllowed(requestContext.getRoles(), userDto.getRoles())) {
						Message message = new Message();
						message.setCode(UserException.ROLE_ASSIGNMENT_NOT_ALLOWED);
						message.setDescription("Role assignment not allowed");
						message.setSeverity(MessageSeverityEnum.warning);
						userDto.getMessages().add(message);

						throw new UserException(requestContext.getTrackingNumber(), UserException.ROLE_ASSIGNMENT_NOT_ALLOWED,
								"Role assignment not allowed", userDto);
					}
					
					// Remove all roles from user
					for (AppRoleAssignment appRoleAssignment : idpUser.get().getAppRoleAssignments()) {
						azureGraphClientService.unassignRole(idpUser.get().getId(), azureGraphClientService.getRole(appRoleAssignment).getValue());
					}
					
					// Add all roles from request
					for (RoleEnum role : userDto.getRoles()) {
						azureGraphClientService.assignRole(idpUser.get().getId(), role.toString());
						savedDto.getRoles().add(role);
					}
				}
			}
			
			return savedDto;
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
