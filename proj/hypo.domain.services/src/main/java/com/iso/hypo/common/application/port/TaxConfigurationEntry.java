package com.iso.hypo.common.application.port;

import java.util.List;

import com.iso.hypo.common.domain.model.LocalizedString;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaxConfigurationEntry {

    private String country;
    private String state;
    List<LocalizedString> code;
    List<LocalizedString> name;
    private String authority;
    private String jurisdiction;
    private String registrationNumber;
    private Double rate;
    private Boolean isIncludedInPrice;

    public TaxConfigurationEntry() {
    }

    public TaxConfigurationEntry(String country, String state, List<LocalizedString> code, List<LocalizedString> name,
            String authority, String jurisdiction, String registrationNumber,
            Double rate, Boolean isIncludedInPrice) {
        this.country = country;
        this.state = state;
        this.code = code;
        this.name = name;
        this.authority = authority;
        this.jurisdiction = jurisdiction;
        this.registrationNumber = registrationNumber;
        this.rate = rate;
        this.isIncludedInPrice = isIncludedInPrice;
    }
}
