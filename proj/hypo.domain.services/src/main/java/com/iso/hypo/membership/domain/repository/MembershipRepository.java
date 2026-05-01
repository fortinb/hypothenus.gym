package com.iso.hypo.membership.domain.repository;

import java.util.Optional;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.membership.domain.model.Membership;

public interface MembershipRepository  {
	
	Optional<Membership> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String membershipUuid);
	
	PageResult<Membership> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, PageRequest pageRequest);
	
	PageResult<Membership> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, PageRequest pageRequest);
	
	Membership save(Membership membership);

    void delete(Membership membership);

    void deleteAll();
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}

