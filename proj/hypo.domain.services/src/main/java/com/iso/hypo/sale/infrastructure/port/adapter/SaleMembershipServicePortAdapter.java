package com.iso.hypo.sale.infrastructure.port.adapter;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.membership.application.dto.MembershipDto;
import com.iso.hypo.membership.application.usecase.MembershipQueryService;
import com.iso.hypo.membership.application.usecase.MembershipService;
import com.iso.hypo.sale.application.port.MembershipServicePort;
import com.iso.hypo.sale.application.port.dto.MembershipRef;
import com.iso.hypo.sale.infrastructure.port.mapper.SaleMembershipRefMapper;

@Component
public class SaleMembershipServicePortAdapter implements MembershipServicePort {

	private static final Logger logger = LoggerFactory.getLogger(SaleMembershipServicePortAdapter.class);

	private final MembershipQueryService membershipQueryService;
	
	private final MembershipService membershipService;
	
	private final SaleMembershipRefMapper membershipRefMapper;

	@SuppressWarnings("unused")
	private final RequestContext requestContext;

	public SaleMembershipServicePortAdapter(
			MembershipQueryService membershipQueryService,
			MembershipService membershipService,
			SaleMembershipRefMapper membershipRefMapper,
			RequestContext requestContext) {
		this.membershipQueryService = membershipQueryService;
		this.membershipService = membershipService;
		this.membershipRefMapper = membershipRefMapper;
		this.requestContext = requestContext;
	}

	@Override
	public boolean isNewMember(String brandUuid, String memberUuid) {
		try {
			return membershipQueryService.isNewMember(brandUuid, memberUuid);
		} catch (Exception e) {
			logger.debug("Member not found or unavailable - brandUuid={} memberUuid={}", brandUuid, memberUuid);
			return false;
		}
	}

	@Override
	public Optional<MembershipRef> findByMembershipPlanUuid(String brandUuid, String memberUuid, String membershipPlanUuid) {
		try {
			Optional<MembershipDto> dto = membershipQueryService.findByMembershipPlanUuid(brandUuid, memberUuid, membershipPlanUuid);
			return Optional.of(membershipRefMapper.toRef(dto.get()));
			
		} catch (Exception e) {
			logger.debug("Member not found or unavailable - brandUuid={} memberUuid={}", brandUuid, memberUuid);
			return Optional.empty();
		}
	}

	@Override
	public List<MembershipRef> create(List<MembershipRef> membershipsRef) {
		try {
			List<MembershipDto> dtos = membershipsRef.stream().map(membershipRefMapper::toDto).toList();

			return membershipService.create(dtos).stream().map(membershipRefMapper::toRef).toList();
		} catch (Exception e) {
			logger.debug("Membership creation error", e);
			return null;
		}
	}
}
