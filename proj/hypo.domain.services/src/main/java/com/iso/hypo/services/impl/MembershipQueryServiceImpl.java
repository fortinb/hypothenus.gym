package com.iso.hypo.services.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.Membership;
import com.iso.hypo.domain.dto.MembershipDto;
import com.iso.hypo.repositories.MembershipRepository;
import com.iso.hypo.services.MembershipQueryService;
import com.iso.hypo.services.exception.MembershipException;
import com.iso.hypo.services.mappers.MembershipMapper;

@Service
public class MembershipQueryServiceImpl implements MembershipQueryService {

	private MembershipRepository membershipRepository;

	private MembershipMapper membershipMapper;

	@Autowired
	private RequestContext requestContext;

	@Autowired
	private Logger logger;

	public MembershipQueryServiceImpl(MembershipRepository membershipRepository, MembershipMapper membershipMapper) {
		this.membershipRepository = membershipRepository;
		this.membershipMapper = membershipMapper;
	}

	@Override
	public void assertExists(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			Optional<Membership> entity = membershipRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid,
					membershipUuid);
			if (entity.isEmpty()) {
				throw new MembershipException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
			}
		} catch (Exception e) {
			// Single generic logger call for all exception types
			logger.error("Error - brandUuid={}, membershipUuid={}, trackingNumber={}", brandUuid, membershipUuid,
				requestContext != null ? requestContext.getTrackingNumber() : null, e);

			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipException.FIND_FAILED, e);
		}
	}

	@Override
	public MembershipDto find(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			Optional<Membership> entity = membershipRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid,
					membershipUuid);
			if (entity.isEmpty()) {
				throw new MembershipException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
			}

			return membershipMapper.toDto(entity.get());
		} catch (Exception e) {
			// Single generic logger call for all exception types
			logger.error("Error - brandUuid={}, membershipUuid={}, trackingNumber={}", brandUuid, membershipUuid,
				requestContext != null ? requestContext.getTrackingNumber() : null, e);
			
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipException.FIND_FAILED, e);
		}
	}

	@Override
	public Page<MembershipDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MembershipException {
		try {

			if (includeInactive) {
				return membershipRepository
						.findAllByBrandUuidAndIsDeletedIsFalse(brandUuid,
							PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"))
						.map(m -> membershipMapper.toDto(m));
			}

			return membershipRepository
					.findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid,
						PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"))
					.map(m -> membershipMapper.toDto(m));

		} catch (Exception e) {
			// Single generic logger call for all exception types
			logger.error("Error - brandUuid={}, trackingNumber={}", brandUuid,
				requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipException.FIND_FAILED, e);
		}
	}
}