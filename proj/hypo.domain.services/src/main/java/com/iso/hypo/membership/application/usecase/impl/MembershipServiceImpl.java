package com.iso.hypo.membership.application.usecase.impl;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.membership.application.dto.MembershipDto;
import com.iso.hypo.membership.application.exception.MembershipException;
import com.iso.hypo.membership.application.mapper.MembershipDtoMapper;
import com.iso.hypo.membership.application.port.BrandServicePort;
import com.iso.hypo.membership.application.port.dto.BrandRef;
import com.iso.hypo.membership.application.usecase.MembershipService;
import com.iso.hypo.membership.domain.model.Member;
import com.iso.hypo.membership.domain.model.Membership;
import com.iso.hypo.membership.domain.model.MembershipPlan;
import com.iso.hypo.membership.domain.model.enumeration.BillingFrequencyEnum;
import com.iso.hypo.membership.domain.repository.MemberRepository;
import com.iso.hypo.membership.domain.repository.MembershipPlanRepository;
import com.iso.hypo.membership.domain.repository.MembershipRepository;

@Service
public class MembershipServiceImpl implements MembershipService {

	private final BrandServicePort brandServicePort;

	private final MembershipRepository membershipRepository;

	private final MembershipPlanRepository membershipPlanRepository;

	private final MemberRepository memberRepository;

	private final MembershipDtoMapper membershipMapper;

	// replace field-injected logger with static logger
	private static final Logger logger = LoggerFactory.getLogger(MembershipServiceImpl.class);

	private final RequestContext requestContext;

	public MembershipServiceImpl(MembershipDtoMapper membershipMapper, MembershipRepository membershipRepository,
			MembershipPlanRepository membershipPlanRepository, MemberRepository memberRepository,
			BrandServicePort brandValidationPort, RequestContext requestContext) {
		this.membershipMapper = membershipMapper;
		this.membershipRepository = membershipRepository;
		this.membershipPlanRepository = membershipPlanRepository;
		this.memberRepository = memberRepository;
		this.brandServicePort = brandValidationPort;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	@Transactional(rollbackFor = MembershipException.class)
	public List<MembershipDto> create(List<MembershipDto> membershipsDto) throws MembershipException {

		List<MembershipDto> memberships = new java.util.ArrayList<>();

		Assert.notNull(membershipsDto, "membershipDto must not be null");

		for (MembershipDto membershipDto : membershipsDto) {
			try {
				Membership membership = membershipMapper.toEntity(membershipDto);

				validateMembership(membership);

				BrandRef brand = resolveBrand(membership.getBrandUuid());
				Member member = resolveMember(brand.getUuid(), membership.getMemberUuid());
				MembershipPlan membershipPlan = resolveMembershipPlan(brand.getUuid(),
						membership.getMembershipPlan().getUuid());

				membership.setBrandUuid(brand.getUuid());
				membership.setMembershipPlan(membershipPlan);
				membership.setMemberUuid(member.getUuid());
				membership.setRemainingClasses(membershipPlan.getNumberOfClasses());
				membership.setAutoRenewal(membershipPlan.getBillingFrequency() == BillingFrequencyEnum.monthly
						|| membershipPlan.getBillingFrequency() == BillingFrequencyEnum.weekly
						|| membershipPlan.getBillingFrequency() == BillingFrequencyEnum.biweekly);
				membership.setCreatedOn(Instant.now());
				membership.setCreatedBy(requestContext.getUsername());

				Membership saved = membershipRepository.save(membership);
				memberships.add(membershipMapper.toDto(saved));
			} catch (Exception e) {
				logger.error("Error - brandUuid={}", membershipDto != null ? membershipDto.getBrandUuid() : null, e);

				if (e instanceof MembershipException) {
					throw (MembershipException) e;
				}
				throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.CREATION_FAILED,	e);
			}
		}

		return memberships;
	}

	private void validateMembership(Membership membership) throws MembershipException {
		if (membership.getMembershipPlan() == null || membership.getMembershipPlan().getUuid() == null) {
			throw new MembershipException(requestContext.getTrackingNumber(),
					MembershipException.MISSING_MEMBERSHIP_PLAN, "Membership plan must be provided");
		}

		if (membership.getMemberUuid() == null || membership.getMemberUuid().isEmpty()) {
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.MISSING_MEMBER,
					"Member must be provided");
		}

		if (membership.getOrderUuid() == null || membership.getOrderUuid().isEmpty()) {
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.MISSING_ORDER,
					"Order must be provided");
		}
	}

	@Override
	@Transactional
	public MembershipDto update(MembershipDto membershipDto) throws MembershipException {
		try {
			return updateMembership(membershipDto, false);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}",
					membershipDto != null ? membershipDto.getBrandUuid() : null,
					membershipDto != null ? membershipDto.getUuid() : null, e);

			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MembershipDto patch(MembershipDto membershipDto) throws MembershipException {
		try {
			return updateMembership(membershipDto, true);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}",
					membershipDto != null ? membershipDto.getBrandUuid() : null,
					membershipDto != null ? membershipDto.getUuid() : null, e);

			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MembershipDto activate(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			BrandRef brand = resolveBrand(brandUuid);
			Membership entity = this.readByMembershipUuid(brand.getUuid(), membershipUuid);

			entity.activate(requestContext.getUsername());
			membershipRepository.save(entity);

			return membershipMapper.toDto(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}", brandUuid, membershipUuid, e);

			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MembershipDto deactivate(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			BrandRef brand = resolveBrand(brandUuid);
			Membership entity = this.readByMembershipUuid(brand.getUuid(), membershipUuid);

			entity.deactivate(requestContext.getUsername());
			membershipRepository.save(entity);

			return membershipMapper.toDto(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}", brandUuid, membershipUuid, e);

			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.DEACTIVATION_FAILED,
					e);
		}
	}

	@Override
	@Transactional
	public void delete(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			BrandRef brand = resolveBrand(brandUuid);
			Membership entity = this.readByMembershipUuid(brand.getUuid(), membershipUuid);

			entity.delete(requestContext.getUsername());
			membershipRepository.save(entity);

		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}", brandUuid, membershipUuid, e);

			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.DELETE_FAILED, e);
		}
	}

	@Override
	public void deleteAllByBrandUuid(String brandUuid) throws MembershipException {
		try {
			if (brandServicePort.brandDeleted(brandUuid)) {
				long deletedCount = membershipRepository.deleteAllByBrandUuid(brandUuid, requestContext.getUsername());

				logger.info("MembershipPlan deleted for brand - brandUuid={} deletedCount={} ", brandUuid, deletedCount);
			}
		} catch (Exception e) {
			logger.error("Error - brandId={}", brandUuid, e);

			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.DELETE_FAILED, e);
		}
	}

	private MembershipDto updateMembership(MembershipDto membershipDto, boolean skipNull) throws MembershipException {
		try {
			Assert.notNull(membershipDto, "membershipDto must not be null");
			Membership membership = membershipMapper.toEntity(membershipDto);

			BrandRef brand = resolveBrand(membershipDto.getBrandUuid());

			Membership oldMembership = this.readByMembershipUuid(brand.getUuid(), membership.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(skipNull);

			mapper = membershipMapper.initMembershipMappings(mapper);
			mapper.map(membership, oldMembership);

			oldMembership.setModifiedOn(Instant.now());
			oldMembership.setModifiedBy(requestContext.getUsername());

			Membership saved = membershipRepository.save(oldMembership);
			return membershipMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}",
					membershipDto != null ? membershipDto.getBrandUuid() : null,
					membershipDto != null ? membershipDto.getUuid() : null, e);

			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.UPDATE_FAILED, e);
		}
	}

	private Membership readByMembershipUuid(String brandUuid, String membershipUuid) throws MembershipException {
		Optional<Membership> entity = membershipRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid,
				membershipUuid);
		if (entity.isEmpty()) {
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.MEMBERSHIP_NOT_FOUND,
					"Membership not found");
		}

		return entity.get();
	}

	private BrandRef resolveBrand(String brandUuid) throws MembershipException {
		return brandServicePort.find(brandUuid)
				.orElseThrow(() -> new MembershipException(requestContext.getTrackingNumber(),
						MembershipException.BRAND_NOT_FOUND, "Brand not found - brandUuid=" + brandUuid));
	}

	private Member resolveMember(String brandUuid, String memberUuid) throws MembershipException {
		return memberRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid)
				.orElseThrow(() -> new MembershipException(requestContext.getTrackingNumber(),
						MembershipException.MEMBER_NOT_FOUND, "Member not found - memberUuid=" + memberUuid));
	}

	private MembershipPlan resolveMembershipPlan(String brandUuid, String membershipPlanUuid)
			throws MembershipException {
		return membershipPlanRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, membershipPlanUuid)
				.orElseThrow(() -> new MembershipException(requestContext.getTrackingNumber(),
						MembershipException.MEMBERSHIP_PLAN_NOT_FOUND,
						"Member not found - membershipPlanUuid=" + membershipPlanUuid));
	}
}