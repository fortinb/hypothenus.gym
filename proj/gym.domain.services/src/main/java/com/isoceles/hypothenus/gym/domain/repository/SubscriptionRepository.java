package com.isoceles.hypothenus.gym.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.isoceles.hypothenus.gym.domain.model.aggregate.Subscription;

public interface SubscriptionRepository extends PagingAndSortingRepository<Subscription, String>, CrudRepository<Subscription, String>, SubscriptionRepositoryCustom {
	
	Optional<Subscription> findByGymIdAndIdAndIsDeletedIsFalse(String gymId, String id);
	
	Page<Subscription> findAllByGymIdAndIsDeletedIsFalse(String gymId, Pageable pageable);
	
	Page<Subscription> findAllByGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(String gymId, Pageable pageable);
	
	
}