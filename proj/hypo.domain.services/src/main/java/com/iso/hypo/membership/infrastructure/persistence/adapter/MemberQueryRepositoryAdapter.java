package com.iso.hypo.membership.infrastructure.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.brand.application.repository.BrandQueryRepository;
import com.iso.hypo.brand.infrastructure.persistence.adapter.BrandRepositoryAdapter;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;
import com.iso.hypo.membership.application.dto.search.MemberSearchDto;
import com.iso.hypo.membership.application.repository.MemberQueryRepository;
import com.iso.hypo.membership.infrastructure.persistence.repository.MemberMongoRepository;

/**
 * Infrastructure adapter implementing the application-layer output port
 * {@link BrandQueryRepository}. Kept separate from {@link BrandRepositoryAdapter}
 * because it deals with application-specific projections (DTOs), not domain
 * aggregates.
 */
@Repository
public class MemberQueryRepositoryAdapter extends BaseAdapter implements MemberQueryRepository {

    private final MemberMongoRepository mongoRepository;

    public MemberQueryRepositoryAdapter(MemberMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public PageResult<MemberSearchDto> searchAutocomplete(String criteria, PageRequest pageRequest,
            boolean includeInactive) {
        Page<MemberSearchDto> page = mongoRepository.searchAutocomplete(criteria,
                toSpringPageable(pageRequest, Sort.by("person.lastname").ascending()), includeInactive);
        return toPageResult(page, pageRequest);
    }
}
