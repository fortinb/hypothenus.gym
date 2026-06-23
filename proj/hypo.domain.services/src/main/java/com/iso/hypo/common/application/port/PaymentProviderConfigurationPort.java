package com.iso.hypo.common.application.port;

public interface PaymentProviderConfigurationPort {


    PaymentProviderConfigurationEntry getPaymentProviderConfiguration(String brandCode);
}
