package com.iso.hypo.common.application.port;

import java.util.List;

public interface TaxConfigurationPort {

    /**
     * Returns all tax configuration entries applicable to a given country and state.
     *
     * @param country ISO country code (e.g. "CA")
     * @param state   state/province code (e.g. "QC")
     * @return list of applicable tax configuration entries
     */
    List<TaxConfigurationEntry> getTaxConfigurations(String country, String state);
}
