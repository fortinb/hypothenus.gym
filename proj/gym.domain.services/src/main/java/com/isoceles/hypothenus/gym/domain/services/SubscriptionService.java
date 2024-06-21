package com.isoceles.hypothenus.gym.domain.services;

import java.time.Instant;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.isoceles.hypothenus.gym.domain.context.RequestContext;
import com.isoceles.hypothenus.gym.domain.exception.DomainException;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Subscription;
import com.isoceles.hypothenus.gym.domain.repository.SubscriptionRepository;

@Service
public class SubscriptionService {

	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private RequestContext requestContext;
	
	public SubscriptionService(SubscriptionRepository subscriptionRepository) {
		this.subscriptionRepository = subscriptionRepository;
	}

	public Subscription create(String gymId, Subscription subscription) throws DomainException {
		if (!subscription.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		subscription.setCreatedOn(Instant.now());
		subscription.setCreatedBy(requestContext.getUsername());
		
		return subscriptionRepository.save(subscription);
	}

	public Subscription update(String gymId, Subscription subscription) throws DomainException {
		if (!subscription.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		Subscription oldSubscription = this.findBySubscriptionId(gymId, subscription.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		PropertyMap<Subscription, Subscription> subscriptionPropertyMap = new PropertyMap<Subscription, Subscription>()
	    {
	        protected void configure()
	        {
	            skip().setId(null);
	            skip().setActive(false);
	            skip().setActivatedOn(null);
	            skip().setDeactivatedOn(null);
	        }
	    };
	    
		mapper.addMappings(subscriptionPropertyMap);
		mapper.map(subscription, oldSubscription);

		oldSubscription.setModifiedOn(Instant.now());
		oldSubscription.setModifiedBy(requestContext.getUsername());
		
		return subscriptionRepository.save(oldSubscription);
	}

	public Subscription patch(String gymId, Subscription subscription) throws DomainException {
		if (!subscription.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		Subscription oldSubscription = this.findBySubscriptionId(gymId, subscription.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		PropertyMap<Subscription, Subscription> subscriptionPropertyMap = new PropertyMap<Subscription, Subscription>()
	    {
	        protected void configure()
	        {
	            skip().setId(null);
	            skip().setActive(false);
	            skip().setActivatedOn(null);
	            skip().setDeactivatedOn(null);
	        }
	    };
	    
		mapper.addMappings(subscriptionPropertyMap);
		mapper.map(subscription, oldSubscription);
		
		oldSubscription.setModifiedOn(Instant.now());
		oldSubscription.setModifiedBy(requestContext.getUsername());
		
		return subscriptionRepository.save(oldSubscription);
	}

	public void delete(String gymId, String subscriptionId) throws DomainException {
		Subscription oldSubscription = this.findBySubscriptionId(gymId,  subscriptionId);
		oldSubscription.setDeleted(true);

		oldSubscription.setDeletedOn(Instant.now());
		oldSubscription.setDeletedBy(requestContext.getUsername());
		
		subscriptionRepository.save(oldSubscription);
	}

	public Subscription findBySubscriptionId(String gymId, String id) throws DomainException {
		Optional<Subscription> entity = subscriptionRepository.findByGymIdAndIdAndIsDeletedIsFalse(gymId, id);
		if (entity.isEmpty()) {
			throw new DomainException(DomainException.SUBSCRIPTION_NOT_FOUND, "Subscription not found");
		}

		return entity.get();
	}

	public Page<Subscription> list(String gymId, int page, int pageSize) throws DomainException {
		return subscriptionRepository.findAllByGymIdAndIsDeletedIsFalse(gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
	}
	
	public Page<Subscription> listActive(String gymId, int page, int pageSize) throws DomainException {
		return subscriptionRepository.findAllByGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
	}
	
	public Subscription activate(String gymId, String id) throws DomainException {
		
		Optional<Subscription> oldSubscription = subscriptionRepository.activate(gymId, id);
		if (oldSubscription.isEmpty()) {
			throw new DomainException(DomainException.SUBSCRIPTION_NOT_FOUND, "Subscription not found");
		}
		
		return oldSubscription.get();
	}
	
	public Subscription deactivate(String gymId, String id) throws DomainException {
		
		Optional<Subscription> oldSubscription = subscriptionRepository.deactivate(gymId, id);
		if (oldSubscription.isEmpty()) {
			throw new DomainException(DomainException.SUBSCRIPTION_NOT_FOUND, "Subscription not found");
		}
		
		return oldSubscription.get();
	}
}
