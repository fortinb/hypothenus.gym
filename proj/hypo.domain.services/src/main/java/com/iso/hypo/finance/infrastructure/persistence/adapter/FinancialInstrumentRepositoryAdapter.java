package com.iso.hypo.finance.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;
import com.iso.hypo.finance.domain.model.FinancialInstrument;
import com.iso.hypo.finance.domain.repository.FinancialInstrumentRepository;
import com.iso.hypo.finance.infrastructure.persistence.entity.FinancialInstrumentDocument;
import com.iso.hypo.finance.infrastructure.persistence.mapper.FinancialInstrumentDocumentMapper;
import com.iso.hypo.finance.infrastructure.persistence.repository.FinancialInstrumentMongoRepository;

@Repository
public class FinancialInstrumentRepositoryAdapter extends BaseAdapter implements FinancialInstrumentRepository {

    private final FinancialInstrumentMongoRepository mongoRepository;
    private final FinancialInstrumentDocumentMapper financialInstrumentMapper;

    public FinancialInstrumentRepositoryAdapter(FinancialInstrumentMongoRepository mongoRepository, FinancialInstrumentDocumentMapper coachMapper) {
        this.mongoRepository = mongoRepository;
        this.financialInstrumentMapper = coachMapper;
    }

    @Override
    public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
        return mongoRepository.deleteAllByBrandUuid(brandUuid, deletedBy);
    }

	@Override
	public Optional<FinancialInstrument> findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(String brandUuid,
			String memberUuid, String financialInstrumentUuid) {
		  return mongoRepository.findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid, financialInstrumentUuid)
	                .map(financialInstrumentMapper::toEntity);
	}

	@Override
	public PageResult<FinancialInstrument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(String brandUuid,
			String memberUuid, PageRequest pageRequest) {
		 Page<FinancialInstrumentDocument> page = mongoRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(brandUuid, memberUuid,toSpringPageable(pageRequest, Sort.by("type").ascending()));
	        return toPageResult(page, pageRequest).map(financialInstrumentMapper::toEntity);
	}

	@Override
	public PageResult<FinancialInstrument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(
			String brandUuid, String memberUuid, PageRequest pageRequest) {
		 Page<FinancialInstrumentDocument> page = mongoRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, memberUuid,toSpringPageable(pageRequest, Sort.by("type").ascending()));
	        return toPageResult(page, pageRequest).map(financialInstrumentMapper::toEntity);

	}

	@Override
	public long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy) {
		return mongoRepository.deleteAllByMemberUuid(brandUuid, memberUuid, deletedBy);
	}

    @Override
    public FinancialInstrument save(FinancialInstrument financialInstrument) {
    	FinancialInstrumentDocument document = financialInstrumentMapper.toDocument(financialInstrument);
    	FinancialInstrumentDocument saved = mongoRepository.save(document);
        return financialInstrumentMapper.toEntity(saved);
    }
    
    @Override
    public void delete(FinancialInstrument financialInstrument) {
        mongoRepository.delete(financialInstrumentMapper.toDocument(financialInstrument));
    }

    @Override
    public void deleteAll() {
        mongoRepository.deleteAll();
    }

}
