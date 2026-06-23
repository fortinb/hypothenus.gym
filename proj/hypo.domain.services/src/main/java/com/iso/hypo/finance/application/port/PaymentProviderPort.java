package com.iso.hypo.finance.application.port;

import com.iso.hypo.common.application.port.PaymentProviderConfigurationEntry;
import com.iso.hypo.finance.application.port.dto.paymentprovider.CreditCardRef;
import com.iso.hypo.finance.application.port.dto.paymentprovider.ReceiptRef;

public interface PaymentProviderPort {

	// Verifies the credit card information without performing a transaction. This can be used to check if the card is valid.
    ReceiptRef verify(PaymentProviderConfigurationEntry config, CreditCardRef creditCard);
    
    // Registers the credit card information for future transactions.
    ReceiptRef register(PaymentProviderConfigurationEntry config, CreditCardRef creditCard);
    
    // Performs a purchase transaction using the provided credit card information and order details. This will charge the card for the specified amount.
    ReceiptRef purchase(PaymentProviderConfigurationEntry config, CreditCardRef creditCard, String orderId, String customerId, String amount);
    
    // Issues a refund for a previous transaction. This will credit the specified amount back to the card used in the original purchase.
    ReceiptRef refund(PaymentProviderConfigurationEntry config, CreditCardRef creditCard, String orderId, String customerId, String txnNumber, String amount);
    
    ReceiptRef delete(PaymentProviderConfigurationEntry config, CreditCardRef creditCard);
}
