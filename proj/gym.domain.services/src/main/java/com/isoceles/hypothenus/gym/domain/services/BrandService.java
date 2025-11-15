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
import com.isoceles.hypothenus.gym.domain.model.BrandSearchResult;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Brand;
import com.isoceles.hypothenus.gym.domain.model.contact.Contact;
import com.isoceles.hypothenus.gym.domain.model.contact.PhoneNumber;
import com.isoceles.hypothenus.gym.domain.model.location.Address;
import com.isoceles.hypothenus.gym.domain.repository.BrandRepository;

@Service
public class BrandService {

	private BrandRepository brandRepository;

	@Autowired
	private RequestContext requestContext;

	public BrandService(BrandRepository brandRepository) {
		this.brandRepository = brandRepository;
	}

	public Brand create(Brand brand) throws DomainException {
		Optional<Brand> existingBrand = brandRepository.findByBrandId(brand.getBrandId());
		if (existingBrand.isPresent()) {
			throw new DomainException(DomainException.BRAND_CODE_ALREADY_EXIST, "Duplicate brand code");
		}

		brand.setCreatedOn(Instant.now());
		brand.setCreatedBy(requestContext.getUsername());

		return brandRepository.save(brand);
	}

	public Brand update(Brand brand) throws DomainException {
		Brand oldBrand = this.findByBrandId(brand.getBrandId());

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

		return brandRepository.save(oldBrand);
	}

	public Brand patch(Brand brand) throws DomainException {
		Brand oldBrand = this.findByBrandId(brand.getBrandId());

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

		return brandRepository.save(oldBrand);
	}

	public void delete(String brandId) throws DomainException {
		Brand oldBrand = this.findByBrandId(brandId);
		oldBrand.setDeleted(true);

		oldBrand.setDeletedOn(Instant.now());
		oldBrand.setDeletedBy(requestContext.getUsername());

		brandRepository.save(oldBrand);
	}

	public Brand findByBrandId(String id) throws DomainException {
		Optional<Brand> entity = brandRepository.findByBrandIdAndIsDeletedIsFalse(id);
		if (entity.isEmpty()) {
			throw new DomainException(DomainException.BRAND_NOT_FOUND, "Brand not found");
		}

		return entity.get();
	}

	public Page<BrandSearchResult> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws DomainException {
		return brandRepository.searchAutocomplete(criteria, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"),
				includeInactive);
	}

	public Page<Brand> list(int page, int pageSize, boolean includeInactive) throws DomainException {
		if (includeInactive) {
			return brandRepository.findAllByIsDeletedIsFalse(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"));
		}

		return brandRepository
				.findAllByIsDeletedIsFalseAndIsActiveIsTrue(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"));
	}

	public Brand activate(String brandId) throws DomainException {

		Optional<Brand> oldBrand = brandRepository.activate(brandId);
		if (oldBrand.isEmpty()) {
			throw new DomainException(DomainException.BRAND_NOT_FOUND, "Brand not found");
		}

		return oldBrand.get();
	}

	public Brand deactivate(String brandId) throws DomainException {

		Optional<Brand> oldBrand = brandRepository.deactivate(brandId);
		if (oldBrand.isEmpty()) {
			throw new DomainException(DomainException.BRAND_NOT_FOUND, "Brand not found");
		}

		return oldBrand.get();
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
