package com.iso.hypo.brand.application.usecase.impl;

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

import com.iso.hypo.brand.application.dto.UserDto;
import com.iso.hypo.brand.application.event.UserEvent;
import com.iso.hypo.brand.application.exception.UserException;
import com.iso.hypo.brand.application.mapper.UserDtoMapper;
import com.iso.hypo.brand.application.usecase.UserService;
import com.iso.hypo.brand.domain.model.User;
import com.iso.hypo.brand.domain.repository.UserRepository;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.event.enumeration.OperationEnum;
import com.iso.hypo.common.application.security.RoleEnum;
import com.iso.hypo.common.application.security.Roles;
import com.iso.hypo.common.application.usecase.AzureGraphClientService;
import com.iso.hypo.common.domain.model.Message;
import com.iso.hypo.common.domain.model.enumeration.MessageSeverityEnum;
import com.microsoft.graph.models.AppRoleAssignment;
import com.microsoft.graph.models.PasswordProfile;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	private final UserDtoMapper userMapper;

	private final AzureGraphClientService azureGraphClientService;

	private final ApplicationEventPublisher eventPublisher;

	@Value("${app.test.run:false}")
	private boolean testRun;

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	private final RequestContext requestContext;

	public UserServiceImpl(UserDtoMapper userMapper, UserRepository userRepository,
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
	public UserDto create(UserDto userDto, String password, String groupName) throws UserException {
		try {
			Assert.notNull(userDto, "userDto must not be null");

			User user = userMapper.toEntity(userDto);
			user.setUuid(UUID.randomUUID().toString());
			
			Optional<User> existingUser = userRepository.findByEmailAndDeletedIsFalse(user.getEmail());
			if (existingUser.isPresent()) {
				Message message = new Message();
				message.setCode(UserException.USER_ALREADY_EXIST);
				message.setDescription("Duplicate user");
				message.setSeverity(MessageSeverityEnum.warning);
				existingUser.get().setMessages(List.of(message));

				throw new UserException(requestContext.getTrackingNumber(), UserException.USER_ALREADY_EXIST,
						"Duplicate user", userMapper.toDto(existingUser.get()));
			}

			if (!testRun) {
				// Find user in identity provider with same email
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.userExists(user.getEmail());
				if (idpUser.isPresent()) {
					Message message = new Message();
					message.setCode(UserException.USER_ALREADY_EXIST_IN_IDP);
					message.setDescription("Duplicate user");
					message.setSeverity(MessageSeverityEnum.warning);
					existingUser.get().setMessages(List.of(message));

					throw new UserException(requestContext.getTrackingNumber(), UserException.USER_ALREADY_EXIST,
							"Duplicate member", userMapper.toDto(existingUser.get()));
				}
				
				// Verify security level
				if (!Roles.isRolesAssignmentAllowed(requestContext.getRoles(), null, user.getRoles())) {
					Message message = new Message();
					message.setCode(UserException.ROLE_ASSIGNMENT_NOT_ALLOWED);
					message.setDescription("Role assignment not allowed");
					message.setSeverity(MessageSeverityEnum.warning);
					existingUser.get().setMessages(List.of(message));

					throw new UserException(requestContext.getTrackingNumber(),
							UserException.ROLE_ASSIGNMENT_NOT_ALLOWED, "Role assignment not allowed",
							userMapper.toDto(existingUser.get()));
				}

				// Create user in identity provider
				com.microsoft.graph.models.User newIdpUser = new com.microsoft.graph.models.User();

				newIdpUser.setAccountEnabled(true);
				newIdpUser.setDisplayName(user.getFirstname() + " " + user.getLastname());
				newIdpUser.setGivenName(user.getFirstname());
				newIdpUser.setSurname(user.getLastname());
				newIdpUser.setMailNickname(user.getUuid());
				newIdpUser.setMail(user.getEmail());
				newIdpUser.setUserPrincipalName(user.getUuid());
				newIdpUser.setPasswordProfile(new PasswordProfile());
				newIdpUser.getPasswordProfile().setForceChangePasswordNextSignIn(false);
				newIdpUser.getPasswordProfile().setPassword(password);

				newIdpUser = azureGraphClientService.createUser(newIdpUser);

				// Assign role to user, if role already exists in IDP it will be ignored by Graph API
				for (RoleEnum role : user.getRoles()) {
					azureGraphClientService.assignRole(newIdpUser.getId(), role.toString());
				}

				// Assign Group to user,if group already exists in IDP it will be ignored by Graph API
				azureGraphClientService.addToGroup(newIdpUser.getId(), groupName);
			} else {
				// For test run, generate random UUID for idpId and use email as upn
				logger.debug("Skipping createUser in IDP because app.test-run=true.");
				user.setIdpId(UUID.randomUUID().toString());
				user.setUpn(user.getEmail());
			}

			// Create user
			user.setCreatedOn(Instant.now());
			user.setCreatedBy(requestContext.getUsername());

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
	public UserDto createAdmin(UserDto userDto) throws UserException {
		try {
			Assert.notNull(userDto, "userDto must not be null");

			User user = userMapper.toEntity(userDto);

			Optional<User> existingUser = userRepository.findByEmailAndDeletedIsFalse(user.getEmail());
			if (existingUser.isPresent()) {
				Message message = new Message();
				message.setCode(UserException.USER_ALREADY_EXIST);
				message.setDescription("Duplicate user");
				message.setSeverity(MessageSeverityEnum.warning);
				existingUser.get().setMessages(List.of(message));

				throw new UserException(requestContext.getTrackingNumber(), UserException.USER_ALREADY_EXIST,
						"Duplicate user", userMapper.toDto(existingUser.get()));
			}
			
			if (!testRun) {
				// Find user in identity provider with same email
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.userExists(user.getEmail());
				if (idpUser.isPresent()) {
					Message message = new Message();
					message.setCode(UserException.USER_ALREADY_EXIST);
					message.setDescription("Duplicate user");
					message.setSeverity(MessageSeverityEnum.warning);
					user.setMessages(List.of(message));
					
					throw new UserException(requestContext.getTrackingNumber(), UserException.USER_ALREADY_EXIST,
							"Duplicate user", userMapper.toDto(user));
				}
			}

			// Create user
			user.setUuid(UUID.randomUUID().toString());
			user.setCreatedOn(Instant.now());
			user.setCreatedBy(requestContext.getUsername());
			
			if (!testRun) {
				// Verify security level
				if (!Roles.isRolesAssignmentAllowed(requestContext.getRoles(), null, user.getRoles())) {
					Message message = new Message();
					message.setCode(UserException.ROLE_ASSIGNMENT_NOT_ALLOWED);
					message.setDescription("Role assignment not allowed");
					message.setSeverity(MessageSeverityEnum.warning);
					existingUser.get().setMessages(List.of(message));

					throw new UserException(requestContext.getTrackingNumber(), UserException.ROLE_ASSIGNMENT_NOT_ALLOWED,
							"Role assignment not allowed", userMapper.toDto(existingUser.get()));
				}
				
				// Create user in identity provider
				com.microsoft.graph.models.User newIdpUser = new com.microsoft.graph.models.User();

				newIdpUser.setAccountEnabled(true);
				newIdpUser.setDisplayName(user.getFirstname() + " " + user.getLastname());
				newIdpUser.setGivenName(user.getFirstname());
				newIdpUser.setSurname(user.getLastname());
				newIdpUser.setMailNickname(user.getUuid());
				newIdpUser.setMail(user.getEmail());
				newIdpUser.setUserPrincipalName(user.getUuid());
				newIdpUser.setPasswordProfile(new PasswordProfile());
				newIdpUser.getPasswordProfile().setForceChangePasswordNextSignIn(true);
				newIdpUser.getPasswordProfile().setPassword("password.test.1");
				
				com.microsoft.graph.models.User createdUser = azureGraphClientService.createUser(newIdpUser);

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

			User entity = this.readByUserUuid(userUuid);
			entity.activate(requestContext.getUsername());
			userRepository.save(entity);

			if (!testRun) {
				// Find user in identity provider with same email
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.findUser(entity.getIdpId());
				if (idpUser.isPresent()) {
					// Deactivate user in identity provider
					idpUser.get().setAccountEnabled(true);
					azureGraphClientService.updateUser(idpUser.get());
				}
			}

			return userMapper.toDto(entity);
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
			User entity = this.readByUserUuid(userUuid);
			entity.deactivate(requestContext.getUsername());
			userRepository.save(entity);

			if (!testRun) {
				// Find user in identity provider with same email
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.findUser(entity.getIdpId());
				if (idpUser.isPresent()) {
					// Deactivate user in identity provider
					idpUser.get().setAccountEnabled(false);
					azureGraphClientService.updateUser(idpUser.get());
				}
			}

			return userMapper.toDto(entity);
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
			
			if (!testRun) {
				// Find user in identity provider with same email
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.findUser(entity.getIdpId());
				if (idpUser.isPresent()) {
					// Delete user in identity provider
					azureGraphClientService.deleteUser(idpUser.get().getId());
				}
			}
			
			entity.delete(requestContext.getUsername());
			userRepository.save(entity);

			eventPublisher.publishEvent(new UserEvent(this, userMapper.toDto(entity), OperationEnum.delete));
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
			
			User user = userMapper.toEntity(userDto);
			User oldUser = this.readByUserUuid(user.getUuid());

			if (user.getEmail() != null && !user.getEmail().equals(oldUser.getEmail())) {
				Optional<User> existingUserEmail = userRepository.findByEmailAndDeletedIsFalse(user.getEmail());
				if (existingUserEmail.isPresent()) {
					Message message = new Message();
					message.setCode(UserException.USER_ALREADY_EXIST);
					message.setDescription("Duplicate user");
					message.setSeverity(MessageSeverityEnum.warning);
					oldUser.setMessages(List.of(message));

					throw new UserException(requestContext.getTrackingNumber(), UserException.USER_ALREADY_EXIST,
							"Duplicate user", userMapper.toDto(oldUser));
				}
			}

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(skipNull).setCollectionsMergeEnabled(false);

			mapper = userMapper.initUserMappings(mapper);
			mapper.map(user, oldUser);

			oldUser.setModifiedOn(Instant.now());
			oldUser.setModifiedBy(requestContext.getUsername());

			if (!testRun) {
				List<RoleEnum> sourceRoles = new java.util.ArrayList<RoleEnum>();
				List<RoleEnum> targetRoles = userDto.getRoles() != null ? userDto.getRoles()
						: new java.util.ArrayList<RoleEnum>();

				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.findUser(oldUser.getIdpId());
				if (idpUser.isPresent()) {
					for (AppRoleAssignment appRoleAssignment : idpUser.get().getAppRoleAssignments()) {
						sourceRoles.add(RoleEnum.valueOf(azureGraphClientService.getRole(appRoleAssignment).getValue()));
					}

					// Update user in identity provider
					idpUser.get().setDisplayName(oldUser.getFirstname() + " " + oldUser.getLastname());
					idpUser.get().setGivenName(oldUser.getFirstname());
					idpUser.get().setSurname(oldUser.getLastname());
					idpUser.get().setMail(oldUser.getEmail());
					azureGraphClientService.updateUser(idpUser.get());
				}

				// Update roles if provided in request
				if (targetRoles != null) {
					if (!Roles.isRolesAssignmentAllowed(requestContext.getRoles(), sourceRoles, targetRoles)) {
						Message message = new Message();
						message.setCode(UserException.ROLE_ASSIGNMENT_NOT_ALLOWED);
						message.setDescription("Role assignment not allowed");
						message.setSeverity(MessageSeverityEnum.warning);
						oldUser.setMessages(List.of(message));

						throw new UserException(requestContext.getTrackingNumber(),
								UserException.ROLE_ASSIGNMENT_NOT_ALLOWED, "Role assignment not allowed",
								userMapper.toDto(oldUser));
					}

					// Remove roles from user
					for (RoleEnum sourceRole : sourceRoles) {
						if (!oldUser.getRoles().contains(sourceRole)) {
							logger.debug("Debug - unassign role ={}", sourceRole);
							azureGraphClientService.unassignRole(idpUser.get().getId(), sourceRole.toString());
						}
					}

					// Add roles from request
					for (RoleEnum targetRole : targetRoles) {
						if (!sourceRoles.contains(targetRole)) {
							logger.debug("Debug - assign role ={}", targetRole);
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
		Optional<User> entity = userRepository.findByUuidAndDeletedIsFalse(userUuid);
		if (entity.isEmpty()) {
			throw new UserException(requestContext.getTrackingNumber(), UserException.USER_NOT_FOUND, "User not found");
		}

		return entity.get();
	}


}
