package com.iso.hypo.brand.application.usecase.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.iso.hypo.brand.application.dto.UserDto;
import com.iso.hypo.brand.application.dto.search.UserSearchDto;
import com.iso.hypo.brand.application.exception.UserException;
import com.iso.hypo.brand.application.mapper.UserDtoMapper;
import com.iso.hypo.brand.application.repository.UserQueryRepository;
import com.iso.hypo.brand.application.usecase.UserQueryService;
import com.iso.hypo.brand.domain.model.User;
import com.iso.hypo.brand.domain.repository.UserRepository;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.application.usecase.AzureGraphClientService;

@Service
public class UserQueryServiceImpl implements UserQueryService {

	@Value("${app.test.run:false}")
	private boolean testRun;

	private final UserRepository userRepository;
	private final UserQueryRepository userQueryRepository;
	private final UserDtoMapper userMapper;

	private final AzureGraphClientService azureGraphClientService;

	private static final Logger logger = LoggerFactory.getLogger(UserQueryServiceImpl.class);

	private final RequestContext requestContext;

	public UserQueryServiceImpl(
			UserDtoMapper userMapper, 
			UserRepository userRepository,
			UserQueryRepository userQueryRepository,
			AzureGraphClientService azureGraphClientService, 
			RequestContext requestContext) {
		this.userQueryRepository = userQueryRepository;
		this.userMapper = userMapper;
		this.userRepository = userRepository;
		this.azureGraphClientService = azureGraphClientService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public UserDto find(String userUuid) throws UserException {
		try {
			Optional<User> entity = userRepository.findByUuidAndDeletedIsFalse(userUuid);
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
	public Optional<UserDto> findByEmail(String email) throws UserException {
		try {
			return userRepository.findByEmailAndDeletedIsFalse(email).map(userMapper::toDto);
		} catch (Exception e) {
			logger.error("Error - email={}", email, e);
			throw new UserException(requestContext.getTrackingNumber(), UserException.FIND_FAILED, e);
		}
	}

	@Override
	public Optional<UserDto> findByIdpId(String idpId) throws UserException {
		try {
			return userRepository.findByIdpIdAndDeletedIsFalse(idpId).map(userMapper::toDto);
		} catch (Exception e) {
			logger.error("Error - idpId={}", idpId, e);
			throw new UserException(requestContext.getTrackingNumber(), UserException.FIND_FAILED, e);
		}
	}

	@Override
	public PageResultDto<UserSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws UserException {
		try {
			PageResult<UserSearchDto> result = userQueryRepository.searchAutocomplete(criteria,
					PageRequest.of(page, pageSize), includeInactive);
			return PageResultDto.from(result);
		} catch (Exception e) {
			logger.error("Error - criteria={}", criteria, e);
			throw new UserException(requestContext.getTrackingNumber(), UserException.FIND_FAILED, e);
		}
	}

	@Override
	public PageResultDto<UserDto> list(int page, int pageSize, boolean includeInactive) throws UserException {
		try {
			PageRequest pageRequest = PageRequest.of(page, pageSize);
			PageResult<UserDto> result = includeInactive
					? userRepository.findAllByDeletedIsFalse(pageRequest)
							.map(userMapper::toDto)
					: userRepository.findAllByDeletedIsFalseAndActiveIsTrue(pageRequest)
							.map(userMapper::toDto);
			return PageResultDto.from(result);
		} catch (Exception e) {
			logger.error("Error", e);
			throw new UserException(requestContext.getTrackingNumber(), UserException.FIND_FAILED, e);
		}
	}
}
