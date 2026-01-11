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

@Service
public class MembershipService {

	private MembershipRepository membershipRepository;

	@Autowired
	private RequestContext requestContext;
	
	public MembershipService(MembershipRepository membershipRepository) {
		this.membershipRepository = membershipRepository;
	}

	public Membership create(String brandId, Membership membership) throws MemberException {
		if (!membership.getBrandId().equals(brandId)) {
			throw new MemberException(MemberException.INVALID_BRAND, "Invalid brand");
		}
		
		membership.setCreatedOn(Instant.now());
		membership.setCreatedBy(requestContext.getUsername());
		
		return membershipRepository.save(membership);
	}

	public Membership update(String brandId, Membership membership) throws MemberException {
		if (!membership.getBrandId().equals(brandId)) {
			throw new MemberException(MemberException.INVALID_BRAND, "Invalid brand");
		}
		
		Membership oldMembership = this.findByMembershipId(brandId, membership.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = initMembershipMappings(mapper);
		mapper.map(membership, oldMembership);

		oldMembership.setModifiedOn(Instant.now());
		oldMembership.setModifiedBy(requestContext.getUsername());
		
		return membershipRepository.save(oldMembership);
	}

	public Membership patch(String brandId, Membership membership) throws MemberException {
		if (!membership.getBrandId().equals(brandId)) {
			throw new MemberException(MemberException.INVALID_BRAND, "Invalid brand");
		}
		
		Membership oldMembership = this.findByMembershipId(brandId, membership.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = initMembershipMappings(mapper);
		mapper.map(membership, oldMembership);
		
		oldMembership.setModifiedOn(Instant.now());
		oldMembership.setModifiedBy(requestContext.getUsername());
		
		return membershipRepository.save(oldMembership);
	}

	public void delete(String brandId, String membershipId) throws MemberException {
		Membership oldMembership = this.findByMembershipId(brandId,  membershipId);
		oldMembership.setDeleted(true);

		oldMembership.setDeletedOn(Instant.now());
		oldMembership.setDeletedBy(requestContext.getUsername());
		
		membershipRepository.save(oldMembership);
	}

	public Membership findByMembershipId(String brandId, String id) throws MemberException {
		Optional<Membership> entity = membershipRepository.findByBrandIdAndIdAndIsDeletedIsFalse(brandId, id);
		if (entity.isEmpty()) {
			throw new MemberException(MemberException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}

		return entity.get();
	}

	public Page<Membership> list(String brandId, int page, int pageSize, boolean includeInactive) throws MemberException {
		if (includeInactive) {
			return membershipRepository.findAllByBrandIdAndIsDeletedIsFalse(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
		}

		return membershipRepository.findAllByBrandIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
	}
	
	public Membership activate(String brandId, String id) throws MemberException {
		
		Optional<Membership> oldMembership = membershipRepository.activate(brandId, id);
		if (oldMembership.isEmpty()) {
			throw new MemberException(MemberException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}
		
		return oldMembership.get();
	}
	
	public Membership deactivate(String brandId, String id) throws MemberException {
		
		Optional<Membership> oldMembership = membershipRepository.deactivate(brandId, id);
		if (oldMembership.isEmpty()) {
			throw new MemberException(MemberException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}
		
		return oldMembership.get();
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