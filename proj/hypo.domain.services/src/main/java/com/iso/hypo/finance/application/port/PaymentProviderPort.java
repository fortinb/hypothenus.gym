package com.iso.hypo.finance.application.port;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.port.PaymentProviderConfigurationEntry;
import com.iso.hypo.finance.application.port.dto.paymentprovider.CreditCardRef;
import com.iso.hypo.finance.application.port.dto.paymentprovider.ReceiptRef;

public interface PaymentProviderPort {

	// Verifies the credit card information without performing a transaction. This can be used to check if the card is valid.
    ReceiptRef verify(PaymentProviderConfigurationEntry config, RequestContext requestContext, CreditCardRef creditCard);
      
    // Performs a purchase transaction using the provided credit card information and order details. This will charge the card for the specified amount.
    ReceiptRef purchase(PaymentProviderConfigurationEntry config, RequestContext requestContext, CreditCardRef creditCard, String orderId, String customerId, int amount, String currency);
    
    // Delete a payment method (credit card) from the payment provider's system. This is typically used to remove a stored credit card from the user's account.
    ReceiptRef delete(PaymentProviderConfigurationEntry config, RequestContext requestContext, String paymentMethodId);
}
