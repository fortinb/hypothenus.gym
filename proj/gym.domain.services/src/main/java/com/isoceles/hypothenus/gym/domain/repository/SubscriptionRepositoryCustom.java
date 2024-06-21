package com.isoceles.hypothenus.gym.domain.repository;

import java.util.Optional;

import com.isoceles.hypothenus.gym.domain.model.aggregate.Subscription;

public interface SubscriptionRepositoryCustom {

	Optional<Subscription> activate(String gymId, String id);
	
	Optional<Subscription> deactivate(String gymId, String id);
}
