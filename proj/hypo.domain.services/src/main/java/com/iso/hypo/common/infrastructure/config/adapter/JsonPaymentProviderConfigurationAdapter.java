package com.iso.hypo.common.infrastructure.config.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.common.application.port.PaymentProviderConfigurationEntry;
import com.iso.hypo.common.application.port.PaymentProviderConfigurationPort;

import jakarta.annotation.PostConstruct;

@Component
public class JsonPaymentProviderConfigurationAdapter implements PaymentProviderConfigurationPort {

    private static final String PAYMENT_PROVIDER_CONFIG_FILE = "payment-provider-config.json";

    private List<PaymentProviderConfigurationEntry> allEntries = Collections.emptyList();

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getClassLoader().getResourceAsStream(PAYMENT_PROVIDER_CONFIG_FILE);
            if (is == null) {
                throw new IllegalStateException("Moneris configuration file not found: " + PAYMENT_PROVIDER_CONFIG_FILE);
            }
            JsonNode root = mapper.readTree(is);
            allEntries = mapper.convertValue(root.get("moneris"), new TypeReference<List<PaymentProviderConfigurationEntry>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load payment provider configuration from " + PAYMENT_PROVIDER_CONFIG_FILE, e);
        }
    }

    @Override
    public PaymentProviderConfigurationEntry getPaymentProviderConfiguration(String brandCode) {
        if (brandCode == null) {
            throw new IllegalArgumentException("Brand code cannot be null");
        }
        
        Optional<PaymentProviderConfigurationEntry> config =  allEntries.stream()
				.filter(entry -> brandCode.equalsIgnoreCase(entry.getBrandCode()))
				.findFirst();
			
        if (config.isEmpty()) {
        	config =  allEntries.stream()
    				.filter(entry -> entry.getBrandCode().equalsIgnoreCase("default"))
    				.findFirst();
			if (config.isEmpty()) {
				throw new IllegalStateException("No payment provider configuration found for brand code: " + brandCode);
			}
 		}
        
        return config.get();
    }
}
