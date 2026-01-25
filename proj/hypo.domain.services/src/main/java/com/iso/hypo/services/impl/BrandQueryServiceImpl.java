package com.iso.hypo.services.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.dto.BrandDto;
import com.iso.hypo.domain.dto.BrandSearchDto;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.services.BrandQueryService;
import com.iso.hypo.services.exception.BrandException;
import com.iso.hypo.services.mappers.BrandMapper;

@Service
public class BrandQueryServiceImpl implements BrandQueryService {

    private final BrandRepository brandRepository;

    private final BrandMapper brandMapper;

    private static final Logger logger = LoggerFactory.getLogger(BrandQueryServiceImpl.class);

    private final RequestContext requestContext;

    public BrandQueryServiceImpl(BrandMapper brandMapper, BrandRepository brandRepository, RequestContext requestContext) {
        this.brandMapper = brandMapper;
        this.brandRepository = brandRepository;
        this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
    }

    @Override
    public void assertExists(String brandUuid) throws BrandException {
        try {
            Optional<Brand> entity = brandRepository.findByUuidAndIsDeletedIsFalse(brandUuid);
            if (entity.isEmpty()) {
                throw new BrandException(requestContext.getTrackingNumber(), BrandException.BRAND_NOT_FOUND, "Brand not found");
            }
        } catch (Exception e) {
            logger.error("Error - brandUuid={}", brandUuid, e);
            if (e instanceof BrandException) {
                throw (BrandException) e;
            }
            throw new BrandException(requestContext.getTrackingNumber(), BrandException.FIND_FAILED, e);
        }
    }

    @Override
    public BrandDto find(String brandUuid)  throws BrandException {
        try {        	
            Optional<Brand> entity = brandRepository.findByUuidAndIsDeletedIsFalse(brandUuid);
            if (entity.isEmpty()) {
                throw new BrandException(requestContext.getTrackingNumber(), BrandException.BRAND_NOT_FOUND, "Brand not found");
            }

            return brandMapper.toDto(entity.get());
        } catch (Exception e) {
            logger.error("Error - brandUuid={}", brandUuid, e);
            if (e instanceof BrandException) {
                throw (BrandException) e;
            }
            throw new BrandException(requestContext.getTrackingNumber(), BrandException.FIND_FAILED, e);
        }
    }
    
    @Override
    public Page<BrandSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive)
            throws BrandException {
        try {
            return brandRepository.searchAutocomplete(criteria, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"),
                        includeInactive);
        } catch (Exception e) {
            logger.error("Error - criteria={}", criteria, e);
            throw new BrandException(requestContext.getTrackingNumber(), BrandException.FIND_FAILED, e);
        }
    }
    
    @Override
    public Page<BrandDto> list(int page, int pageSize, boolean includeInactive) throws BrandException {
        try {
            if (includeInactive) {
                return brandRepository.findAllByIsDeletedIsFalse(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
                        .map(b -> brandMapper.toDto(b));
            }

            return brandRepository
                    .findAllByIsDeletedIsFalseAndIsActiveIsTrue(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
                    .map(b -> brandMapper.toDto(b));
        } catch (Exception e) {
            logger.error("Error - page={}, pageSize={}, includeInactive={}", page, pageSize, includeInactive, e);
            throw new BrandException(requestContext.getTrackingNumber(), BrandException.FIND_FAILED, e);
        }
      }
 }