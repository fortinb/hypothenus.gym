package com.iso.hypo.finance.infrastructure.port.adapter;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iso.hypo.common.application.port.PaymentProviderConfigurationPort;
import com.iso.hypo.common.infrastructure.config.adapter.JsonPaymentProviderConfigurationAdapter;

/**
 * Minimal Spring test configuration that registers {@link FinanceMonerisPortAdapter}
 * and {@link JsonPaymentProviderConfigurationAdapter} as beans so that {@code @Value}
 * fields (e.g. {@code moneris.url}) are resolved from
 * {@code src/test/resources/application.properties} when running JUnit tests.
 */
@TestConfiguration
public class MonerisTestConfig {

    @Bean
    PaymentProviderConfigurationPort paymentProviderConfigurationPort() {
        return new JsonPaymentProviderConfigurationAdapter();
    }

    @Bean
    FinanceMonerisPortAdapter financeMonerisPortAdapter() {
    	JsonMapper mapper = JsonMapper.builder()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
				.addModule(new JavaTimeModule())
				.build();
        return new FinanceMonerisPortAdapter(mapper);
    }
}
