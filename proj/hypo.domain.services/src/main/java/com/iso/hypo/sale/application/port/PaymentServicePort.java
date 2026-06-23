package com.iso.hypo.sale.application.port;

import java.util.Optional;

import com.iso.hypo.sale.application.port.dto.PaymentRef;

public interface PaymentServicePort {

	 Optional<PaymentRef> purchase(PaymentRef paymentRef);
}
