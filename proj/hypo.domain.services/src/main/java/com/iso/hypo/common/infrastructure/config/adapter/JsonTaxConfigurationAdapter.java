package com.iso.hypo.common.infrastructure.config.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.common.application.port.TaxConfigurationEntry;
import com.iso.hypo.common.application.port.TaxConfigurationPort;

import jakarta.annotation.PostConstruct;

@Component
public class JsonTaxConfigurationAdapter implements TaxConfigurationPort {

    private static final String TAX_CONFIG_FILE = "taxes-config.json";

    private List<TaxConfigurationEntry> allEntries = Collections.emptyList();

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getClassLoader().getResourceAsStream(TAX_CONFIG_FILE);
            if (is == null) {
                throw new IllegalStateException("Tax configuration file not found: " + TAX_CONFIG_FILE);
            }
            JsonNode root = mapper.readTree(is);
            allEntries = mapper.convertValue(root.get("taxes"), new TypeReference<List<TaxConfigurationEntry>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load tax configuration from " + TAX_CONFIG_FILE, e);
        }
    }

    @Override
    public List<TaxConfigurationEntry> getTaxConfigurations(String country, String state) {
        if (country == null || state == null) {
            return Collections.emptyList();
        }
        return allEntries.stream()
                .filter(entry -> country.equalsIgnoreCase(entry.getCountry())
                        && state.equalsIgnoreCase(entry.getState()))
                .collect(Collectors.toList());
    }
}
