package com.iso.hypo.finance.infrastructure.port.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Integration-style tests for {@link FinanceMonerisPortAdapter}.
 *
 * <p>The Spring context is bootstrapped from {@link MonerisTestConfig} and
 * properties are loaded from {@code src/test/resources/application.properties},
 * which satisfies the {@code @Value("${moneris.url}")} injection inside the adapter.
 */
@SpringJUnitConfig(MonerisTestConfig.class)
@TestPropertySource(locations = "classpath:application.properties")
class FinanceMonerisPortAdapterTest {

    @Autowired
    private FinanceMonerisPortAdapter adapter;
}

