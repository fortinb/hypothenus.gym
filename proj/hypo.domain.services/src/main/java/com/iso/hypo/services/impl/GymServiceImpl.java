package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.Message;
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.domain.dto.GymDto;
import com.iso.hypo.domain.enumeration.MessageSeverityEnum;
import com.iso.hypo.events.event.OperationEnum;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.services.BrandQueryService;
import com.iso.hypo.services.GymService;
import com.iso.hypo.services.event.GymEvent;
import com.iso.hypo.services.exception.BrandException;
import com.iso.hypo.services.exception.GymException;
import com.iso.hypo.services.mappers.GymMapper;

@Service
public class GymServiceImpl implements GymService {

	private final BrandQueryService brandQueryService;
	
	private final GymRepository gymRepository;

	private final GymMapper gymMapper;
	
	private final ApplicationEventPublisher eventPublisher;
	
	private static final Logger logger = LoggerFactory.getLogger(GymServiceImpl.class);

	private final RequestContext requestContext;

	public GymServiceImpl(GymMapper gymMapper, 
						  BrandQueryService brandQueryService, 
						  GymRepository gymRepository, 
						  ApplicationEventPublisher eventPublisher, 
						  RequestContext requestContext) {
        this.gymMapper = gymMapper;
        this.brandQueryService = brandQueryService;
        this.gymRepository = gymRepository;
        this.eventPublisher = eventPublisher;
        this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
    }

	@Override
	@Transactional
	public GymDto create(GymDto gymDto) throws GymException {
		try {
			Assert.notNull(gymDto, "gymDto must not be null");
			
			Gym gym = gymMapper.toEntity(gymDto);
			
			brandQueryService.assertExists(gym.getBrandUuid());

			Optional<Gym> existingGym = gymRepository.findByBrandUuidAndCode(gym.getBrandUuid(), gym.getCode());
			if (existingGym.isPresent()) {
				Message message = new Message();
				message.setCode(GymException.GYM_CODE_ALREADY_EXIST);
				message.setDescription("Duplicate gym code");
				message.setSeverity(MessageSeverityEnum.warning);
				existingGym.get().getMessages().add(message);
				
				throw new GymException(requestContext.getTrackingNumber(), GymException.GYM_CODE_ALREADY_EXIST, "Duplicate gym code", gymMapper.toDto(existingGym.get()));
			}
	
			gym.setCreatedOn(Instant.now());
			gym.setCreatedBy(requestContext.getUsername());
			gym.setUuid(UUID.randomUUID().toString());
			
			Gym saved = gymRepository.save(gym);
			return gymMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", gymDto != null ? gymDto.getBrandUuid() : null, e);
			
			if (e instanceof BrandException) {
				throw new GymException(requestContext.getTrackingNumber(), GymException.GYM_NOT_FOUND, "Gym not found");
			}
			if (e instanceof GymException) {
				throw (GymException) e;
			}
			throw new GymException(requestContext.getTrackingNumber(), GymException.CREATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public GymDto update(GymDto gymDto) throws GymException {
		try {
			Assert.notNull(gymDto, "gymDto must not be null");
			Gym gym = gymMapper.toEntity(gymDto);
			
			Gym oldGym = this.readByGymUuid(gym.getBrandUuid(),gym.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration()
				.setSkipNullEnabled(false)
				.setCollectionsMergeEnabled(false);
			
			PropertyMap<Gym, Gym> gymPropertyMap = new PropertyMap<Gym, Gym>() {
				protected void configure() {
					skip().setId(null);
					skip().setActive(false);
				}
			};
			
			mapper.addMappings(gymPropertyMap);
			mapper = gymMapper.initGymMappings(mapper);
			
			mapper.map(gym, oldGym);

			oldGym.setModifiedOn(Instant.now());
			oldGym.setModifiedBy(requestContext.getUsername());

			Gym saved = gymRepository.save(oldGym);
			return gymMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}", gymDto.getBrandUuid(), gymDto.getUuid(), e);
			
			if (e instanceof GymException) {
				throw (GymException) e;
			}
			throw new GymException(requestContext.getTrackingNumber(), GymException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public GymDto patch(GymDto gymDto) throws GymException {
		try {
			Assert.notNull(gymDto, "gymDto must not be null");
			Gym gym = gymMapper.toEntity(gymDto);
			
			Gym oldGym = this.readByGymUuid(gym.getBrandUuid(), gym.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(true);

			PropertyMap<Gym, Gym> gymPropertyMap = new PropertyMap<Gym, Gym>() {
				protected void configure() {
					skip().setId(null);
					skip().setContacts(null);
					skip().setPhoneNumbers(null);
				}
			};
			
			mapper.addMappings(gymPropertyMap);
			mapper = gymMapper.initGymMappings(mapper);
			mapper.map(gym, oldGym);

			oldGym.setModifiedOn(Instant.now());
			oldGym.setModifiedBy(requestContext.getUsername());

			Gym saved = gymRepository.save(oldGym);
			return gymMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}", gymDto.getBrandUuid(), gymDto.getUuid(), e);
			
			if (e instanceof GymException) {
				throw (GymException) e;
			}
			throw new GymException(requestContext.getTrackingNumber(), GymException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public GymDto activate(String brandUuid, String gymUuid) throws GymException {
		try {
			Optional<Gym> entity = gymRepository.activate(brandUuid, gymUuid);
			if (entity.isEmpty()) {
				throw new GymException(requestContext.getTrackingNumber(), GymException.GYM_NOT_FOUND, "Gym not found");
			}

			return gymMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}", brandUuid, gymUuid, e);
			
			if (e instanceof GymException) {
				throw (GymException) e;
			}
			throw new GymException(requestContext.getTrackingNumber(), GymException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public GymDto deactivate(String brandUuid, String gymUuid) throws GymException {
		try {
			Optional<Gym> entity = gymRepository.deactivate(brandUuid, gymUuid);
			if (entity.isEmpty()) {
				throw new GymException(requestContext.getTrackingNumber(), GymException.GYM_NOT_FOUND, "Gym not found");
			}

			return gymMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}", brandUuid, gymUuid, e);
			
			if (e instanceof GymException) {
				throw (GymException) e;
			}
			throw new GymException(requestContext.getTrackingNumber(), GymException.DEACTIVATION_FAILED, e);
		}
	}
	
	@Override
	@Transactional
	public void delete(String brandUuid, String gymUuid) throws GymException {
		try {
			Gym entity = this.readByGymUuid(brandUuid, gymUuid);
			
			gymRepository.delete(entity.getBrandUuid(), entity.getUuid(), requestContext.getUsername());
			
			eventPublisher.publishEvent(new GymEvent(this, entity, OperationEnum.delete));
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}", brandUuid, gymUuid, e);
			
			if (e instanceof GymException) {
				throw (GymException) e;
			}
			throw new GymException(requestContext.getTrackingNumber(), GymException.DELETE_FAILED, e);
		}
	}
	
	@Override
	public void deleteAllByBrandUuid(String brandUuid) throws GymException {
		try {
			long deletedCount = gymRepository.deleteAllByBrandUuid(brandUuid, requestContext.getUsername());
			
			logger.info("Gym deleted for brand - brandUuid={} deletedCount={} ", brandUuid, deletedCount);
		} catch (Exception e) {
			logger.error("Error - brandId={}", brandUuid, e);
			
			throw new GymException(requestContext.getTrackingNumber(), GymException.DELETE_FAILED, e);
		}
	}
	
	private Gym readByGymUuid(String brandUuid, String gymUuid) throws GymException {
		Optional<Gym> entity = gymRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid);
		if (entity.isEmpty()) {
			throw new GymException(requestContext.getTrackingNumber(), GymException.GYM_NOT_FOUND, "Gym not found");
		}

		return entity.get();
	}
}