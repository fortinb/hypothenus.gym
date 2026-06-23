package com.iso.hypo.membership.infrastructure.port.adapter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.UserDto;
import com.iso.hypo.brand.application.usecase.UserQueryService;
import com.iso.hypo.brand.application.usecase.UserService;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.membership.application.port.UserServicePort;
import com.iso.hypo.membership.application.port.dto.UserRef;
import com.iso.hypo.membership.infrastructure.port.mapper.MembershipUserRefMapper;

/**
 * Infrastructure adapter that satisfies {@link UserServicePort} by delegating
 * to {@link UserQueryService} / {@link UserService}. Translates between the
 * brand-owned {@link UserDto} and the membership-owned {@link UserRef} so that
 * the membership application layer has no compile-time dependency on the brand
 * application layer.
 */
@Component
public class MembershipUserServicePortAdapter implements UserServicePort {

	private static final Logger logger = LoggerFactory.getLogger(MembershipUserServicePortAdapter.class);

	private final UserQueryService userQueryService;
	private final UserService userService;
	private final MembershipUserRefMapper userRefMapper;

	@SuppressWarnings("unused")
	private final RequestContext requestContext;

	public MembershipUserServicePortAdapter(UserQueryService userQueryService, UserService userService,
			MembershipUserRefMapper userRefMapper, RequestContext requestContext) {
		this.userQueryService = userQueryService;
		this.userService = userService;
		this.userRefMapper = userRefMapper;
		this.requestContext = requestContext;
	}

	@Override
	public Optional<UserRef> findByEmail(String email) {
		try {
			return userQueryService.findByEmail(email).map(userRefMapper::toRef);
		} catch (Exception e) {
			logger.debug("User not found by email - email={}", email);
			return Optional.empty();
		}
	}

	@Override
	public Optional<UserRef> findByIdpId(String idpId) {
		try {
			return userQueryService.findByIdpId(idpId).map(userRefMapper::toRef);
		} catch (Exception e) {
			logger.debug("User not found by idpId - idpId={}", idpId);
			return Optional.empty();
		}
	}

	@Override
	public Optional<UserRef> findByUuid(String userUuid) {
		try {
			UserDto entity = userQueryService.find(userUuid);
			return Optional.of(userRefMapper.toRef(entity));
		} catch (Exception e) {
			logger.debug("User not found - exception={}", e.getMessage());
			return null;
		}
	}

	@Override
	public UserRef create(UserRef userRef, String password, String groupName) {
		try {
			UserDto created = userService.create(userRefMapper.toDto(userRef), password, groupName);
			return userRefMapper.toRef(created);
		} catch (Exception e) {
			logger.debug("Create user error - exception={}", e.getMessage());
			return null;
		}
	}

	@Override
	public UserRef patch(UserRef userRef) {
		try {
			UserDto updated = userService.patch(userRefMapper.toDto(userRef));
			return userRefMapper.toRef(updated);
		} catch (Exception e) {
			logger.debug("Create user error - exception={}", e.getMessage());
			return null;
		}
	}
}