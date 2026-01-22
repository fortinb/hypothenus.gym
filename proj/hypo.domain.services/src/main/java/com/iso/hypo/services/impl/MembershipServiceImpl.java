package com.iso.hypo.services.impl;

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
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.aggregate.Membership;
import com.iso.hypo.domain.dto.MembershipDto;
import com.iso.hypo.services.exception.MemberException;
import com.iso.hypo.services.mappers.MemberMapper;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.MembershipRepository;
import com.iso.hypo.services.MembershipService;

@Service
public class MembershipServiceImpl implements MembershipService {

	private BrandRepository brandRepository;
	
	private MembershipRepository membershipRepository;

	private MemberMapper memberMapper;

	@Autowired
	private RequestContext requestContext;
	
	public MembershipServiceImpl(BrandRepository brandRepository, MembershipRepository membershipRepository, MemberMapper memberMapper) {
		this.brandRepository = brandRepository;
		this.membershipRepository = membershipRepository;
		this.memberMapper = memberMapper;
	}

	@Override
	public MembershipDto create(String brandUuid, MembershipDto membershipDto) throws MemberException {
		Optional<Brand> existingBrand = brandRepository.findByUuidAndIsDeletedIsFalse(membershipDto.getBrandUuid());
		if (!existingBrand.isPresent()) {
			throw new MemberException(MemberException.BRAND_NOT_FOUND, "Brand not found");
		}
		
		Membership membership = memberMapper.toEntity(membershipDto);
		if (!membership.getBrandUuid().equals(brandUuid)) {
			throw new MemberException(MemberException.INVALID_BRAND, "Invalid brand");
		}
		
		membership.setCreatedOn(Instant.now());
		membership.setCreatedBy(requestContext.getUsername());
		
		Membership saved = membershipRepository.save(membership);
		return memberMapper.toDto(saved);
	}

	@Override
	public MembershipDto update(String brandUuid, MembershipDto membershipDto) throws MemberException {
		Membership membership = memberMapper.toEntity(membershipDto);
		if (!membership.getBrandUuid().equals(brandUuid)) {
			throw new MemberException(MemberException.INVALID_BRAND, "Invalid brand");
		}
		
		Membership oldMembership = this.readByMembershipUuid(brandUuid, membership.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = initMembershipMappings(mapper);
		mapper.map(membership, oldMembership);

		oldMembership.setModifiedOn(Instant.now());
		oldMembership.setModifiedBy(requestContext.getUsername());
		
		Membership saved = membershipRepository.save(oldMembership);
		return memberMapper.toDto(saved);
	}

	@Override
	public MembershipDto patch(String brandUuid, MembershipDto membershipDto) throws MemberException {
		Membership membership = memberMapper.toEntity(membershipDto);
		if (!membership.getBrandUuid().equals(brandUuid)) {
			throw new MemberException(MemberException.INVALID_BRAND, "Invalid brand");
		}
		
		Membership oldMembership = this.readByMembershipUuid(brandUuid, membership.getUuid());
	
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = initMembershipMappings(mapper);
		mapper.map(membership, oldMembership);
		
		oldMembership.setModifiedOn(Instant.now());
		oldMembership.setModifiedBy(requestContext.getUsername());
		
		Membership saved = membershipRepository.save(oldMembership);
		return memberMapper.toDto(saved);
	}

	@Override
	public void delete(String brandUuid, String membershipUuid) throws MemberException {
		Membership entity = this.readByMembershipUuid(brandUuid,  membershipUuid);
		entity.setDeleted(true);

		entity.setDeletedOn(Instant.now());
		entity.setDeletedBy(requestContext.getUsername());
		
		membershipRepository.save(entity);
	}

	@Override
	public MembershipDto findByMembershipUuid(String brandUuid, String membershipUuid) throws MemberException {
		Optional<Membership> entity = membershipRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, membershipUuid);
		if (entity.isEmpty()) {
			throw new MemberException(MemberException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}

		return memberMapper.toDto(entity.get());
	}

	@Override
	public Page<MembershipDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MemberException {
		if (includeInactive) {
			return membershipRepository.findAllByBrandUuidAndIsDeletedIsFalse(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname")).map(m -> memberMapper.toDto(m));
		}

		return membershipRepository.findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname")).map(m -> memberMapper.toDto(m));
	}
	
	@Override
	public MembershipDto activate(String brandUuid, String membershipUuid) throws MemberException {
		Optional<Membership> entity = membershipRepository.activate(brandUuid, membershipUuid);
		if (entity.isEmpty()) {
			throw new MemberException(MemberException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}
		
		return memberMapper.toDto(entity.get());
	}
	
	@Override
	public MembershipDto deactivate(String brandUuid, String membershipUuid) throws MemberException {
			Optional<Membership> entity = membershipRepository.deactivate(brandUuid, membershipUuid);
		if (entity.isEmpty()) {
			throw new MemberException(MemberException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}
		
		return memberMapper.toDto(entity.get());
	}
	
	private Membership readByMembershipUuid(String brandUuid, String membershipUuid) throws MemberException {
		Optional<Membership> entity = membershipRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, membershipUuid);
		if (entity.isEmpty()) {
			throw new MemberException(MemberException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}

		return entity.get();
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


