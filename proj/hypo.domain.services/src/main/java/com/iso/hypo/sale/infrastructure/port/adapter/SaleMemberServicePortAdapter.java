package com.iso.hypo.sale.infrastructure.port.adapter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.membership.application.dto.MemberDto;
import com.iso.hypo.membership.application.usecase.MemberQueryService;
import com.iso.hypo.sale.application.port.MemberServicePort;
import com.iso.hypo.sale.application.port.dto.MemberRef;
import com.iso.hypo.sale.infrastructure.port.mapper.SaleMemberRefMapper;

@Component
public class SaleMemberServicePortAdapter implements MemberServicePort {

	private static final Logger logger = LoggerFactory.getLogger(SaleMemberServicePortAdapter.class);

	private final MemberQueryService memberQueryService;
	private final SaleMemberRefMapper memberRefMapper;

	@SuppressWarnings("unused")
	private final RequestContext requestContext;

	public SaleMemberServicePortAdapter(MemberQueryService memberQueryService, SaleMemberRefMapper memberRefMapper,
			RequestContext requestContext) {
		this.memberQueryService = memberQueryService;
		this.memberRefMapper = memberRefMapper;
		this.requestContext = requestContext;
	}

	@Override
	public Optional<MemberRef> find(String brandUuid, String memberUuid) {
		try {
			MemberDto dto = memberQueryService.find(brandUuid, memberUuid);
			return Optional.of(memberRefMapper.toRef(dto));
		} catch (Exception e) {
			logger.debug("Member not found or unavailable - brandUuid={} memberUuid={}", brandUuid, memberUuid);
			return Optional.empty();
		}
	}
	
    @Override
    public boolean memberDeleted(String brandUuid, String memberUuid) {
        try {
        	return memberQueryService.assertDeleted(brandUuid, memberUuid);
        } catch (Exception e) {
            logger.debug("Brand not found or not deleted - brandUuid={}", brandUuid);
            return false;
        }
    }
}
