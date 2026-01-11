package com.iso.hypo.member.services;

import java.time.Instant;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.member.exception.MemberException;
import com.iso.hypo.member.domain.aggregate.Membership;
import com.iso.hypo.member.repository.MembershipRepository;
import com.iso.hypo.member.dto.MembershipDto;
import com.iso.hypo.member.mappers.MemberMapper;

@Service
public class MembershipService {

	private MembershipRepository membershipRepository;

	private MemberMapper memberMapper;

	@Autowired
	private RequestContext requestContext;
	
	public MembershipService(MembershipRepository membershipRepository, MemberMapper memberMapper) {
		this.membershipRepository = membershipRepository;
		this.memberMapper = memberMapper;
	}

	public MembershipDto create(String brandId, MembershipDto membershipDto) throws MemberException {
		Membership membership = memberMapper.toEntity(membershipDto);
		if (!membership.getBrandId().equals(brandId)) {
			throw new MemberException(MemberException.INVALID_BRAND, "Invalid brand");
		}
		
		membership.setCreatedOn(Instant.now());
		membership.setCreatedBy(requestContext.getUsername());
		
		Membership saved = membershipRepository.save(membership);
		return memberMapper.toDto(saved);
	}

	public MembershipDto update(String brandId, MembershipDto membershipDto) throws MemberException {
		Membership membership = memberMapper.toEntity(membershipDto);
		if (!membership.getBrandId().equals(brandId)) {
			throw new MemberException(MemberException.INVALID_BRAND, "Invalid brand");
		}
		
		Membership oldMembership = this.memberMapper.toEntity(this.findByMembershipId(brandId, membership.getId()));

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = initMembershipMappings(mapper);
		mapper.map(membership, oldMembership);

		oldMembership.setModifiedOn(Instant.now());
		oldMembership.setModifiedBy(requestContext.getUsername());
		
		Membership saved = membershipRepository.save(oldMembership);
		return memberMapper.toDto(saved);
	}

	public MembershipDto patch(String brandId, MembershipDto membershipDto) throws MemberException {
		Membership membership = memberMapper.toEntity(membershipDto);
		if (!membership.getBrandId().equals(brandId)) {
			throw new MemberException(MemberException.INVALID_BRAND, "Invalid brand");
		}
		
		Membership oldMembership = this.memberMapper.toEntity(this.findByMembershipId(brandId, membership.getId()));

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = initMembershipMappings(mapper);
		mapper.map(membership, oldMembership);
		
		oldMembership.setModifiedOn(Instant.now());
		oldMembership.setModifiedBy(requestContext.getUsername());
		
		Membership saved = membershipRepository.save(oldMembership);
		return memberMapper.toDto(saved);
	}

	public void delete(String brandId, String membershipId) throws MemberException {
		Membership oldMembership = memberMapper.toEntity(this.findByMembershipId(brandId,  membershipId));
		oldMembership.setDeleted(true);

		oldMembership.setDeletedOn(Instant.now());
		oldMembership.setDeletedBy(requestContext.getUsername());
		
		membershipRepository.save(oldMembership);
	}

	public MembershipDto findByMembershipId(String brandId, String id) throws MemberException {
		Optional<Membership> entity = membershipRepository.findByBrandIdAndIdAndIsDeletedIsFalse(brandId, id);
		if (entity.isEmpty()) {
			throw new MemberException(MemberException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}

		return memberMapper.toDto(entity.get());
	}

	public Page<MembershipDto> list(String brandId, int page, int pageSize, boolean includeInactive) throws MemberException {
		if (includeInactive) {
			return membershipRepository.findAllByBrandIdAndIsDeletedIsFalse(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname")).map(m -> memberMapper.toDto(m));
		}

		return membershipRepository.findAllByBrandIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname")).map(m -> memberMapper.toDto(m));
	}
	
	public MembershipDto activate(String brandId, String id) throws MemberException {
		
		Optional<Membership> oldMembership = membershipRepository.activate(brandId, id);
		if (oldMembership.isEmpty()) {
			throw new MemberException(MemberException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}
		
		return memberMapper.toDto(oldMembership.get());
	}
	
	public MembershipDto deactivate(String brandId, String id) throws MemberException {
		
		Optional<Membership> oldMembership = membershipRepository.deactivate(brandId, id);
		if (oldMembership.isEmpty()) {
			throw new MemberException(MemberException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}
		
		return memberMapper.toDto(oldMembership.get());
	}
	
	private ModelMapper initMembershipMappings(ModelMapper mapper) {
		PropertyMap<Membership, Membership> membershipPropertyMap = new PropertyMap<Membership, Membership>()
	    {
	        protected void configure()
	        {
	            // Do not allow these fields to be overwritten by incoming DTOs
	            skip().setId(null);
	            skip().setActive(false);
	            skip().setActivatedOn(null);
	            skip().setDeactivatedOn(null);
	            // Don't replace DBRefs (references) during mapping; updates should manage references explicitly
	           // skip().setMember(null);
	        }
	    };

		mapper.addMappings(membershipPropertyMap);
		return mapper;
	}
}