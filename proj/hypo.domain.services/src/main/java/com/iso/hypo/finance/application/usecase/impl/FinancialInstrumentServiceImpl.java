package com.iso.hypo.finance.application.usecase.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.finance.application.dto.FinancialInstrumentDto;
import com.iso.hypo.finance.application.exception.FinancialInstrumentException;
import com.iso.hypo.finance.application.mapper.FinancialInstrumentDtoMapper;
import com.iso.hypo.finance.application.port.BrandServicePort;
import com.iso.hypo.finance.application.usecase.FinancialInstrumentService;
import com.iso.hypo.finance.domain.model.FinancialInstrument;
import com.iso.hypo.finance.domain.repository.FinancialInstrumentRepository;

@Service
public class FinancialInstrumentServiceImpl implements FinancialInstrumentService {

	private final BrandServicePort brandValidationPort;

	private final FinancialInstrumentDtoMapper financialInstrumentMapper;

	private final FinancialInstrumentRepository financialInstrumentRepository;

	@Value("${app.test.run:false}")
	private boolean testRun;

	private static final Logger logger = LoggerFactory.getLogger(FinancialInstrumentServiceImpl.class);

	private final RequestContext requestContext;

	public FinancialInstrumentServiceImpl(
			FinancialInstrumentRepository financialInstrumentRepository,
			BrandServicePort brandValidationPort,
			FinancialInstrumentDtoMapper financialInstrumentMapper,
			RequestContext requestContext) {
	
		this.financialInstrumentRepository = financialInstrumentRepository;
		this.brandValidationPort = brandValidationPort;
		this.financialInstrumentMapper = financialInstrumentMapper;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	@Transactional
	public FinancialInstrumentDto create(FinancialInstrumentDto financialInstrumentDto) throws FinancialInstrumentException {
		try {
			Assert.notNull(financialInstrumentDto, "financialInstrumentDto must not be null");

			FinancialInstrument financialInstrument = financialInstrumentMapper.toEntity(financialInstrumentDto);
			
			if (!brandValidationPort.brandExists(financialInstrument.getBrandUuid())) {
				throw new FinancialInstrumentException(requestContext.getTrackingNumber(),
						FinancialInstrumentException.BRAND_NOT_FOUND, "Brand not found");
			}

			// TODO: Call CreditCard provider service to get permanent token and set it to credit card entity. This is to avoid storing actual card number in database and use permanent token for future reference.
			financialInstrument.setUuid(UUID.randomUUID().toString());
			financialInstrument.getCreditCard().setCardNumber("********" + financialInstrument.getCreditCard().getCardNumber().substring(financialInstrument.getCreditCard().getCardNumber().length() - 4));
			financialInstrument.getCreditCard().setPermanentToken("permanentToken");

			financialInstrument.setCreatedOn(Instant.now());
			financialInstrument.setCreatedBy(requestContext.getUsername());

			FinancialInstrument saved = financialInstrumentRepository.save(financialInstrument);
			
			return financialInstrumentMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", financialInstrumentDto != null ? financialInstrumentDto.getBrandUuid() : null, e);

			if (e instanceof FinancialInstrumentException) {
				throw (FinancialInstrumentException) e;
			}
			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.CREATION_FAILED, e);
		}
	}
	
	@Override
	@Transactional
	public FinancialInstrumentDto activate(String brandUuid, String memberUuid, String financialInstrumentUuid) throws FinancialInstrumentException {
		try {
			FinancialInstrument entity = this.readByFinancialInstrumentUuid(brandUuid, memberUuid, financialInstrumentUuid);
			entity.activate(requestContext.getUsername());
			financialInstrumentRepository.save(entity);
			
			return financialInstrumentMapper.toDto(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, financialInstrumentUuid={}", brandUuid, financialInstrumentUuid, e);

			if (e instanceof FinancialInstrumentException) {
				throw (FinancialInstrumentException) e;
			}
			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public FinancialInstrumentDto deactivate(String brandUuid, String memberUuid, String financialInstrumentUuid) throws FinancialInstrumentException {
		try {
			FinancialInstrument entity = this.readByFinancialInstrumentUuid(brandUuid, memberUuid, financialInstrumentUuid);
			entity.deactivate(requestContext.getUsername());
			financialInstrumentRepository.save(entity);
			
			return financialInstrumentMapper.toDto(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, financialInstrumentUuid={}", brandUuid, financialInstrumentUuid, e);

			if (e instanceof FinancialInstrumentException) {
				throw (FinancialInstrumentException) e;
			}
			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.DEACTIVATION_FAILED, e);
		}
	}
	
	@Override
	@Transactional
	public void delete(String brandUuid, String memberUuid, String financialInstrumentUuid) throws FinancialInstrumentException {
		try {
			if (!brandValidationPort.brandExists(brandUuid)) {
				throw new FinancialInstrumentException(requestContext.getTrackingNumber(),
						FinancialInstrumentException.BRAND_NOT_FOUND, "Brand not found");
			}
			FinancialInstrument entity = this.readByFinancialInstrumentUuid(brandUuid, memberUuid, financialInstrumentUuid);
			entity.delete(requestContext.getUsername());
			
			// TODO: Call CreditCard provider service to delete permanent token

			financialInstrumentRepository.save(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, financialInstrumentUuid={}, uuid={}", brandUuid, financialInstrumentUuid, financialInstrumentUuid, e);

			if (e instanceof FinancialInstrumentException) {
				throw (FinancialInstrumentException) e;
			}
			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.DELETE_FAILED, e);
		}
	}

	@Override
	public void deleteAllByBrandUuid(String brandUuid) throws FinancialInstrumentException {
		try {
			long deletedCount = financialInstrumentRepository.deleteAllByBrandUuid(brandUuid, requestContext.getUsername());

			logger.info("FinancialInstrument deleted for brand - brandUuid={} deletedCount={} ", brandUuid, deletedCount);
		} catch (Exception e) {
			logger.error("Error - brandId={}", brandUuid, e);

			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.DELETE_FAILED, e);
		}
	}

	@Override
	public void deleteAllByMemberUuid(String brandUuid, String memberUuid) throws FinancialInstrumentException {
		try {
			long deletedCount = financialInstrumentRepository.deleteAllByMemberUuid(brandUuid, memberUuid, requestContext.getUsername());

			logger.info("FinancialInstrument deleted for member - brandUuid={} memberUuid={} deletedCount={} ", brandUuid, memberUuid, deletedCount);
		} catch (Exception e) {
			logger.error("Error - brandId={}", brandUuid, e);

			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.DELETE_FAILED, e);
		}
	}

	private FinancialInstrument readByFinancialInstrumentUuid(String brandUuid, String memberUuid, String financialInstrumentUuid) throws FinancialInstrumentException {
		Optional<FinancialInstrument> entity = financialInstrumentRepository.findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid, financialInstrumentUuid);
		if (entity.isEmpty()) {
			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.FINANCIAL_INSTRUMENT_NOT_FOUND,
					"FinancialInstrument not found");
		}

		return entity.get();
	}

}