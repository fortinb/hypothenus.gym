package com.iso.hypo.common.application.port;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentProviderConfigurationEntry {

    private String brandCode;
	private String provider;
	private String storeId;
	private String merchandId;
	private String subscriptionId;
	private String apiKey;
	private String clientId;
	private String clientSecret;
	private boolean testMode;

    public PaymentProviderConfigurationEntry() {
    }

    public PaymentProviderConfigurationEntry(
    		String brandCode, 
    		String provider, 
    		String storeId,  
    		String merchandId, 
    		String subscriptionId, 
    		String apiKey, 
    		String clientId, 
    		String clientSecret,     		
    		boolean testMode) {
       this.brandCode = brandCode;
       this.provider = provider;
       this.storeId = storeId;
	   this.merchandId = merchandId;
	   this.subscriptionId = subscriptionId;
	   this.apiKey = apiKey;
	   this.clientId = clientId;
	   this.clientSecret = clientSecret;
       this.testMode = testMode;
    }
}
