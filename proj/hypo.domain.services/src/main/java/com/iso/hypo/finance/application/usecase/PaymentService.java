package com.iso.hypo.finance.application.usecase;

import com.iso.hypo.finance.application.dto.PaymentDto;
import com.iso.hypo.finance.application.exception.PaymentException;

public interface PaymentService {

    PaymentDto purchase(PaymentDto paymentDto) throws PaymentException;

    void refund(String brandUuid, String memberUuid, String paymentUuid) throws PaymentException;

    void deleteAllByBrandUuid(String brandUuid) throws PaymentException;
    
    void deleteAllByMemberUuid(String brandUuid, String memberUuid) throws PaymentException;
}