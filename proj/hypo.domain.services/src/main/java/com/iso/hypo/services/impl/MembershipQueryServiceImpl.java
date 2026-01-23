package com.iso.hypo.services.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
	
	public MembershipQueryServiceImpl(MembershipRepository membershipRepository, MembershipMapper membershipMapper) {
		this.membershipRepository = membershipRepository;
		this.membershipMapper = membershipMapper;
	}
	
	@Override
	public void assertExists(String brandUuid, String membershipUuid) throws MembershipException {
		Optional<Membership> entity = membershipRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, membershipUuid);
		if (entity.isEmpty()) {
			throw new MembershipException(MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}
	}
	
	@Override
	public MembershipDto find(String brandUuid, String membershipUuid) throws MembershipException {
		Optional<Membership> entity = membershipRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, membershipUuid);
		if (entity.isEmpty()) {
			throw new MembershipException(MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}

		return membershipMapper.toDto(entity.get());
	}

	@Override
	public Page<MembershipDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MembershipException {
		if (includeInactive) {
			return membershipRepository.findAllByBrandUuidAndIsDeletedIsFalse(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname")).map(m -> membershipMapper.toDto(m));
		}

		return membershipRepository.findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname")).map(m -> membershipMapper.toDto(m));
	}
}