package com.iso.hypo.admin.papi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.iso.hypo.services.clients.AzureGraphClientService;
import com.iso.hypo.services.clients.impl.AzureGraphClientServiceImpl;

@Configuration
public class AzureGraphClientConfig {

    @Bean
    AzureGraphClientService azureGraphClientService(
            @Value("${azure.entra.app.clientid}") String clientId,
            @Value("${azure.entra.app.clientsecret}") String clientSecret,
            @Value("${azure.entra.app.tenantid}") String tenantId,
            @Value("${azure.entra.app.domain.name}") String domainName) {
        return new AzureGraphClientServiceImpl(clientId, clientSecret, tenantId, domainName);
    }
}
