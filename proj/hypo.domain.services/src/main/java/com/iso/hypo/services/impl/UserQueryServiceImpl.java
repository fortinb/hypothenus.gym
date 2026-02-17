package com.iso.hypo.services.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.User;
import com.iso.hypo.domain.dto.UserDto;
import com.iso.hypo.domain.dto.search.UserSearchDto;
import com.iso.hypo.domain.security.RoleEnum;
import com.iso.hypo.repositories.UserRepository;
import com.iso.hypo.services.UserQueryService;
import com.iso.hypo.services.clients.AzureGraphClientService;
import com.iso.hypo.services.exception.UserException;
import com.iso.hypo.services.mappers.UserMapper;
import com.microsoft.graph.models.AppRoleAssignment;

@Service
public class UserQueryServiceImpl implements UserQueryService {

	@Value("${app.test.run:false}")
	private boolean testRun;

	private final UserRepository userRepository;

	private final UserMapper userMapper;

	private final AzureGraphClientService azureGraphClientService;

	private static final Logger logger = LoggerFactory.getLogger(UserQueryServiceImpl.class);

	private final RequestContext requestContext;

	public UserQueryServiceImpl(UserMapper userMapper, UserRepository userRepository,
			AzureGraphClientService azureGraphClientService, RequestContext requestContext) {
		this.userMapper = userMapper;
		this.userRepository = userRepository;
		this.azureGraphClientService = azureGraphClientService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public UserDto find(String userUuid) throws UserException {
		try {
			Optional<User> entity = userRepository.findByUuidAndIsDeletedIsFalse(userUuid);
			if (entity.isEmpty()) {
				throw new UserException(requestContext.getTrackingNumber(), UserException.USER_NOT_FOUND,
						"User not found");
			}
			
			UserDto userDto = userMapper.toDto(entity.get());
			
			if (!testRun) {
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.findUser(entity.get().getIdpId());
				if (!idpUser.isPresent()) {
					throw new UserException(requestContext.getTrackingNumber(), UserException.USER_NOT_FOUND,
							"User not found");
				}
				
				for (AppRoleAssignment appRoleAssignment : idpUser.get().getAppRoleAssignments()) {
					userDto.getRoles().add(RoleEnum.valueOf(azureGraphClientService.getRole(appRoleAssignment).getValue()));
				}
			}

			return userDto;
		} catch (Exception e) {
			logger.error("Error - userUuid={}", userUuid, e);
			if (e instanceof UserException) {
				throw (UserException) e;
			}
			throw new UserException(requestContext.getTrackingNumber(), UserException.FIND_FAILED, e);
		}
	}

	@Override
	public Page<UserSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws UserException {
		try {
			return userRepository.searchAutocomplete(criteria,
					PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"), includeInactive);
		} catch (Exception e) {
			logger.error("Error - criteria={}", criteria, e);
			throw new UserException(requestContext.getTrackingNumber(), UserException.FIND_FAILED, e);
		}
	}

	@Override
	public Page<UserDto> list(int page, int pageSize, boolean includeInactive) throws UserException {
		try {
			if (includeInactive) {
				return userRepository
						.findAllByIsDeletedIsFalse(PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"))
						.map(m -> userMapper.toDto(m));
			}

			return userRepository
					.findAllByIsDeletedIsFalseAndIsActiveIsTrue(
							PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"))
					.map(m -> userMapper.toDto(m));
		} catch (Exception e) {
			logger.error("Error", e);
			throw new UserException(requestContext.getTrackingNumber(), UserException.FIND_FAILED, e);
		}
	}
}
