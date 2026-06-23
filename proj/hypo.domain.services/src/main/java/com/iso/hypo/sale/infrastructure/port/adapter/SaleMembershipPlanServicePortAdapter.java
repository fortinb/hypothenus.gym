package com.iso.hypo.sale.infrastructure.port.adapter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.membership.application.dto.MembershipPlanDto;
import com.iso.hypo.membership.application.usecase.MembershipPlanQueryService;
import com.iso.hypo.sale.application.port.MembershipPlanServicePort;
import com.iso.hypo.sale.application.port.dto.MembershipPlanRef;
import com.iso.hypo.sale.infrastructure.port.mapper.SaleMembershipPlanRefMapper;

@Component
public class SaleMembershipPlanServicePortAdapter implements MembershipPlanServicePort {

    private static final Logger logger = LoggerFactory.getLogger(SaleMembershipPlanServicePortAdapter.class);

    private final MembershipPlanQueryService membershipPlanQueryService;
    private final SaleMembershipPlanRefMapper membershipPlanRefMapper;
    
	@SuppressWarnings("unused")
	private final RequestContext requestContext;
	
    public SaleMembershipPlanServicePortAdapter(
    		MembershipPlanQueryService membershipPlanQueryService, 
    		SaleMembershipPlanRefMapper membershipPlanRefMapper,
    		RequestContext requestContext) {
        this.membershipPlanQueryService = membershipPlanQueryService;
        this.membershipPlanRefMapper = membershipPlanRefMapper;
		this.requestContext = requestContext;
    }

	@Override
	public Optional<MembershipPlanRef> find(String brandUuid, String membershipPlanUuid) {
		 try {
	            MembershipPlanDto dto = membershipPlanQueryService.find(brandUuid, membershipPlanUuid);
	            return Optional.of(membershipPlanRefMapper.toRef(dto));
	        } catch (Exception e) {
	            logger.debug("MembershipPlan not found or unavailable - brandUuid={} membershipPlanUuid={}", brandUuid, membershipPlanUuid);
	            return Optional.empty();
	        }
	}
}
