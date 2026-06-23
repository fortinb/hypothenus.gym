package com.iso.hypo.finance.infrastructure.port.adapter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.iso.hypo.common.application.port.PaymentProviderConfigurationEntry;
import com.iso.hypo.finance.application.port.dto.paymentprovider.CreditCardRef;
import com.iso.hypo.finance.application.port.dto.paymentprovider.ReceiptRef;

class MonerisPortAdapterTests {

    // Moneris test environment credentials (from payment-provider-config.json)
    private static final String TEST_STORE_ID = "store5";
    private static final String TEST_API_KEY  = "yesguy";
    private static final String TEST_PROVIDER  = "moneris";
    private static final String TEST_BRAND    = "default";

    // Moneris sandbox test card (Visa – approved in test mode)
    private static final String VISA_APPROVED_PAN    = "4242424242424242";
    private static final String VISA_CVD_AVS_APPROVED_PAN    = "4761739012345728";
    private static final String MASTERCARD_APPROVED_PAN    = "5454545454545454";
    // Moneris sandbox test card (Visa – will be declined in test mode)
    private static final String VISA_DECLINED_PAN    = "4242424242424243";
    
    private static final String EXPIRY_DATE          = "1227"; // MMYY – December 2027
    private static final String CVD                  = "123";
    private static final String ZIP_CODE             = "H3Z2Y7";
    private static final String COUNTRY_CODE         = "CA";
    private static final String CARD_HOLDER_NAME     = "John Doe";

    private FinanceMonerisPortAdapter adapter;
    private PaymentProviderConfigurationEntry config;
    private CreditCardRef approvedCard;

    @BeforeEach
    void setUp() {
        adapter = new FinanceMonerisPortAdapter();
   
        config = new PaymentProviderConfigurationEntry(
        		TEST_BRAND, TEST_PROVIDER, TEST_STORE_ID, "MerchandId", "SubscriptionId",
        		TEST_API_KEY, "ClientId", "ClientSecret", true);

        approvedCard = new CreditCardRef();
        approvedCard.setCardNumber(VISA_APPROVED_PAN);
        approvedCard.setExpirationDate(EXPIRY_DATE);
        approvedCard.setCvd(CVD);
        approvedCard.setZipCode(ZIP_CODE);
        approvedCard.setCountryCode(COUNTRY_CODE);
        approvedCard.setCardHolderName(CARD_HOLDER_NAME);
    }

    // =========================================================================
    // verify() – integration tests against the Moneris test gateway
    // =========================================================================

    @Nested
    class VerifyTests {

        @Test
        void verify_VISA_success() {
            assertDoesNotThrow(() -> { 
            	CreditCardRef creditCard = buildCard(VISA_APPROVED_PAN);
            	ReceiptRef receipt =  adapter.verify(config, creditCard);
            	assertNotNull(receipt);	
				assertTrue(receipt.isApproved());
            });
        }
        
        @Test
        void verify_VISA_CVD_AVS_success() {
        	assertDoesNotThrow(() -> { 
            	CreditCardRef creditCard = buildCard(VISA_CVD_AVS_APPROVED_PAN);
            	ReceiptRef receipt =  adapter.verify(config, creditCard);
            	assertNotNull(receipt);	
				assertTrue(receipt.isApproved());
				assertTrue(receipt.isAvsResultCode()); // M = Match (AVS)
				assertTrue(receipt.isCvdResultCode()); // M = Match (CVD)
            });
        	
            assertDoesNotThrow(() -> adapter.verify(config, buildCard(VISA_CVD_AVS_APPROVED_PAN)));
        }
        
        @Test
        void verify_MASTERCARD_success() {
            assertDoesNotThrow(() -> { 
            	CreditCardRef creditCard = buildCard(MASTERCARD_APPROVED_PAN);
            	ReceiptRef receipt =  adapter.verify(config, creditCard);
            	assertNotNull(receipt);	
				assertTrue(receipt.isApproved());
            });
        }

        @Test
         void verify_withDeclinedTestCard_declined() {
        	assertDoesNotThrow(() -> { 
            	CreditCardRef creditCard = buildCard(VISA_DECLINED_PAN);
            	ReceiptRef receipt =  adapter.verify(config, creditCard);
            	assertNotNull(receipt);	
				assertFalse(receipt.isApproved());
            });
        }
    }

    // =========================================================================
    // register() –
    // =========================================================================

    @Nested
    class RegisterTests {

        @Test
        void register_success() {
        	assertDoesNotThrow(() -> { 
            	CreditCardRef creditCard = buildCard(VISA_CVD_AVS_APPROVED_PAN);
            	ReceiptRef receipt =  adapter.verify(config, creditCard);
            	
            	creditCard.setCardType(receipt.getCardType());
            	creditCard.setIssuerId(receipt.getIssuerId());
            	receipt =  adapter.register(config, creditCard);
				assertNotNull(receipt);	
				assertTrue(receipt.isApproved());
				assertNotNull(receipt.getPermanentToken());
            });
        }
    }

    // =========================================================================
    // purchase() – stub, not yet implemented
    // =========================================================================

    @Nested
    class PurchaseTests {

    	 @Test
         void purchase_success() {
         	assertDoesNotThrow(() -> { 
             	CreditCardRef creditCard = buildCard(VISA_CVD_AVS_APPROVED_PAN);
             	
             	ReceiptRef receipt =  adapter.verify(config, creditCard);
             	creditCard.setCardType(receipt.getCardType());
             	creditCard.setIssuerId(receipt.getIssuerId());
             	receipt =  adapter.register(config, creditCard);
             	
 				assertNotNull(receipt);	
 				assertTrue(receipt.isApproved());
 				assertNotNull(receipt.getPermanentToken());
 				
 				creditCard.setPermanentToken(receipt.getPermanentToken());
 				
             	receipt =  adapter.purchase(config, creditCard, "ORDER-001", "CUSTOMER-001", "10.01");
            	
             });
         }
    	 
    }

    // =========================================================================
    // refund() – stub, not yet implemented
    // =========================================================================

    @Nested
    class RefundTests {

    	@Test
    	void refund_success() {
          	assertDoesNotThrow(() -> { 
              	CreditCardRef creditCard = buildCard(VISA_CVD_AVS_APPROVED_PAN);
              	
              	ReceiptRef receipt =  adapter.verify(config, creditCard);
              	creditCard.setCardType(receipt.getCardType());
              	creditCard.setIssuerId(receipt.getIssuerId());
              	receipt =  adapter.register(config, creditCard);
              	
  				assertNotNull(receipt);	
  				assertNotNull(receipt.getPermanentToken());
  				
  				creditCard.setPermanentToken(receipt.getPermanentToken());
  				
              	receipt =  adapter.purchase(config, creditCard, "ORDER-002", "CUSTOMER-002", "10.01");
              	assertNotNull(receipt);
              	
              	receipt =  adapter.refund(config, creditCard, "ORDER-002", "CUSTOMER-002", receipt.getTxnNumber(), receipt.getTransAmount());
             	
              });
          }
    }
    
    @Nested
    class DeleteTests {

    	@Test
    	void delete_success() {
          	assertDoesNotThrow(() -> { 
              	CreditCardRef creditCard = buildCard(VISA_APPROVED_PAN);
              	
              	ReceiptRef receipt =  adapter.verify(config, creditCard);
              	creditCard.setCardType(receipt.getCardType());
              	creditCard.setIssuerId(receipt.getIssuerId());
              	receipt =  adapter.register(config, creditCard);
              	
  				assertNotNull(receipt);	
  				assertNotNull(receipt.getPermanentToken());
  				
  				creditCard.setPermanentToken(receipt.getPermanentToken());
  				
              	receipt =  adapter.delete(config, creditCard);
              	assertNotNull(receipt);             	
              });
          }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CreditCardRef buildCard(String cardNumber) {
        CreditCardRef card = new CreditCardRef();
        card.setCardNumber(cardNumber);
        card.setCustomerId("CUSTOMER-001");
        card.setPhoneNumber("514-555-1234");
        card.setEmail("test@test.com");        
        card.setExpirationDate(EXPIRY_DATE);
        card.setCvd(CVD);
        card.setZipCode(ZIP_CODE);
        card.setCountryCode(COUNTRY_CODE);
        card.setCardHolderName(CARD_HOLDER_NAME);
        return card;
    }
}
