package com.iso.hypo.brand.infrastructure.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.brand.application.dto.search.BrandSearchDto;
import com.iso.hypo.brand.application.repository.BrandQueryRepository;
import com.iso.hypo.brand.infrastructure.persistence.repository.BrandMongoRepository;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;

/**
 * Infrastructure adapter implementing the application-layer output port
 * {@link BrandQueryRepository}. Kept separate from
 * {@link BrandRepositoryAdapter} because it deals with application-specific
 * projections (DTOs), not domain aggregates.
 */
@Repository
public class BrandQueryRepositoryAdapter extends BaseAdapter implements BrandQueryRepository {

	private final BrandMongoRepository mongoRepository;

	public BrandQueryRepositoryAdapter(BrandMongoRepository mongoRepository) {
		this.mongoRepository = mongoRepository;
	}

	@Override
	public PageResult<BrandSearchDto> searchAutocomplete(String criteria, PageRequest pageRequest,
			boolean includeInactive) {
		Page<BrandSearchDto> page = mongoRepository.searchAutocomplete(criteria,
				toSpringPageable(pageRequest, Sort.by("name").ascending()), includeInactive);
		return toPageResult(page, pageRequest);
	}
}
