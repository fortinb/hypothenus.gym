package com.iso.hypo.finance.infrastructure.persistence.adapter;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;
import com.iso.hypo.finance.domain.model.Payment;
import com.iso.hypo.finance.domain.repository.PaymentRepository;
import com.iso.hypo.finance.infrastructure.persistence.entity.PaymentDocument;
import com.iso.hypo.finance.infrastructure.persistence.mapper.PaymentDocumentMapper;
import com.iso.hypo.finance.infrastructure.persistence.repository.PaymentMongoRepository;

@Repository
public class PaymentRepositoryAdapter extends BaseAdapter implements PaymentRepository {

    private final PaymentMongoRepository mongoRepository;
    private final PaymentDocumentMapper paymentMapper;

    public PaymentRepositoryAdapter(PaymentMongoRepository mongoRepository, PaymentDocumentMapper coachMapper) {
        this.mongoRepository = mongoRepository;
        this.paymentMapper = coachMapper;
    }

    @Override
    public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
        return mongoRepository.deleteAllByBrandUuid(brandUuid, deletedBy);
    }

	@Override
	public Optional<Payment> findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(String brandUuid,
			String memberUuid, String paymentUuid) {
		  return mongoRepository.findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid, paymentUuid)
	                .map(paymentMapper::toEntity);
	}

	@Override
	public PageResult<Payment> findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(String brandUuid,
			String memberUuid, PageRequest pageRequest) {
		 Page<PaymentDocument> page = mongoRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(brandUuid, memberUuid,toSpringPageable(pageRequest, Sort.by("type").ascending()));
	        return toPageResult(page, pageRequest).map(paymentMapper::toEntity);
	}

	@Override
	public PageResult<Payment> findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(
			String brandUuid, String memberUuid, PageRequest pageRequest) {
		 Page<PaymentDocument> page = mongoRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, memberUuid,toSpringPageable(pageRequest, Sort.by("type").ascending()));
	        return toPageResult(page, pageRequest).map(paymentMapper::toEntity);

	}

    @Override
    public PageResult<Payment> findByDates(String brandUuid, Date startDate, Date endDate, PageRequest pageRequest) {
        Page<PaymentDocument> page = mongoRepository.findByDates(brandUuid, startDate, endDate, toSpringPageable(pageRequest, Sort.by("paidOn").ascending()));
        return toPageResult(page, pageRequest).map(paymentMapper::toEntity);
    }
    
	@Override
	public long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy) {
		return mongoRepository.deleteAllByMemberUuid(brandUuid, memberUuid, deletedBy);
	}

    @Override
    public Payment save(Payment payment) {
    	PaymentDocument document = paymentMapper.toDocument(payment);
    	PaymentDocument saved = mongoRepository.save(document);
        return paymentMapper.toEntity(saved);
    }
    
    @Override
    public void delete(Payment payment) {
        mongoRepository.delete(paymentMapper.toDocument(payment));
    }

    @Override
    public void deleteAll() {
        mongoRepository.deleteAll();
    }

}
