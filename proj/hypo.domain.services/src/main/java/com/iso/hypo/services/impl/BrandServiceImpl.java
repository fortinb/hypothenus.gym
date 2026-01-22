package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.services.exception.BrandException;
import com.iso.hypo.domain.dto.BrandDto;
import com.iso.hypo.domain.dto.BrandSearchDto;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.contact.Contact;
import com.iso.hypo.domain.contact.PhoneNumber;
import com.iso.hypo.domain.location.Address;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.services.mappers.BrandMapper;
import com.iso.hypo.services.BrandService;

@Service
public class BrandServiceImpl implements BrandService {

	private BrandRepository brandRepository;

	private BrandMapper brandMapper;

	@Autowired
	private RequestContext requestContext;

	public BrandServiceImpl(BrandRepository brandRepository, BrandMapper brandMapper) {
		this.brandRepository = brandRepository;
		this.brandMapper = brandMapper;
	}

	@Override
	public BrandDto create(BrandDto brandDto) throws BrandException {
		Brand brand = brandMapper.toEntity(brandDto);
		Optional<Brand> existingBrand = brandRepository.findByCode(brand.getCode());
		if (existingBrand.isPresent()) {
			throw new BrandException(BrandException.BRAND_CODE_ALREADY_EXIST, "Duplicate brand code");
		}

		brand.setCreatedOn(Instant.now());
		brand.setCreatedBy(requestContext.getUsername());
		brand.setUuid(UUID.randomUUID().toString());
		
		Brand saved = brandRepository.save(brand);
		return brandMapper.toDto(saved);
	}

	@Override
	public BrandDto update(BrandDto brandDto) throws BrandException {
		Brand brand = brandMapper.toEntity(brandDto);
		Brand oldBrand = this.readByBrandUuid(brand.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration()
			.setSkipNullEnabled(false)
			.setCollectionsMergeEnabled(false);
		
		PropertyMap<Brand, Brand> brandPropertyMap = new PropertyMap<Brand, Brand>() {
			protected void configure() {
				skip().setId(null);
				skip().setActive(false);
			}
		};
		
		mapper.addMappings(brandPropertyMap);
		mapper = initBrandMappings(mapper);
		
		mapper.map(brand, oldBrand);

		oldBrand.setModifiedOn(Instant.now());
		oldBrand.setModifiedBy(requestContext.getUsername());

		Brand saved = brandRepository.save(oldBrand);
		return brandMapper.toDto(saved);
	}

	@Override
	public BrandDto patch(BrandDto brandDto) throws BrandException {
		Brand brand = brandMapper.toEntity(brandDto);
		Brand oldBrand = this.readByBrandUuid(brand.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);

		PropertyMap<Brand, Brand> brandPropertyMap = new PropertyMap<Brand, Brand>() {
			protected void configure() {
				skip().setId(null);
				skip().setContacts(null);
				skip().setPhoneNumbers(null);
			}
		};
		
		mapper.addMappings(brandPropertyMap);
		mapper = initBrandMappings(mapper);
		
		mapper.map(brand, oldBrand);

		oldBrand.setModifiedOn(Instant.now());
		oldBrand.setModifiedBy(requestContext.getUsername());

		Brand saved = brandRepository.save(oldBrand);
		return brandMapper.toDto(saved);
	}

	@Override
	public void delete(String brandUuid) throws BrandException {
		Brand entity = this.readByBrandUuid(brandUuid);
		entity.setDeleted(true);

		entity.setDeletedOn(Instant.now());
		entity.setDeletedBy(requestContext.getUsername());

		brandRepository.save(entity);
	}

	@Override
	public BrandDto findByBrandUuid(String id) throws BrandException {
		return brandMapper.toDto(this.readByBrandUuid(id));
	}

	@Override
	public Page<BrandSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws BrandException {
		return brandRepository.searchAutocomplete(criteria, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"),
				includeInactive);
	}

	@Override
	public Page<BrandDto> list(int page, int pageSize, boolean includeInactive) throws BrandException {
		if (includeInactive) {
			return brandRepository.findAllByIsDeletedIsFalse(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
				.map(b -> brandMapper.toDto(b));
		}

		return brandRepository
				.findAllByIsDeletedIsFalseAndIsActiveIsTrue(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
				.map(b -> brandMapper.toDto(b));
	}

	@Override
	public BrandDto activate(String brandUuid) throws BrandException {
		Optional<Brand> entity = brandRepository.activate(brandUuid);
		if (entity.isEmpty()) {
			throw new BrandException(BrandException.BRAND_NOT_FOUND, "Brand not found");
		}

		return brandMapper.toDto(entity.get());
	}

	@Override
	public BrandDto deactivate(String brandUuid) throws BrandException {
		Optional<Brand> entity = brandRepository.deactivate(brandUuid);
		if (entity.isEmpty()) {
			throw new BrandException(BrandException.BRAND_NOT_FOUND, "Brand not found");
		}

		return brandMapper.toDto(entity.get());
	}
	
	private Brand readByBrandUuid(String brandUuid) throws BrandException {
		Optional<Brand> entity = brandRepository.findByUuidAndIsDeletedIsFalse(brandUuid);
		if (entity.isEmpty()) {
			throw new BrandException(BrandException.BRAND_NOT_FOUND, "Brand not found");
		}

		return entity.get();
	}
	
	private ModelMapper initBrandMappings(ModelMapper mapper) {
		PropertyMap<Address, Address> addressPropertyMap = new PropertyMap<Address, Address>() {
			@Override
			protected void configure() {
			}
		};
		
		PropertyMap<PhoneNumber, PhoneNumber> phoneNumberPropertyMap = new PropertyMap<PhoneNumber, PhoneNumber>() {
			@Override
			protected void configure() {
			}
		};
		
		PropertyMap<Contact, Contact> contactPropertyMap = new PropertyMap<Contact, Contact>() {
			@Override
			protected void configure() {
			}
		};
		
		mapper.addMappings(addressPropertyMap);
		mapper.addMappings(phoneNumberPropertyMap);
		mapper.addMappings(contactPropertyMap);
		
		return mapper;
	}
}


