package com.iso.hypo.finance.infrastructure.port.adapter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.port.PaymentProviderConfigurationEntry;
import com.iso.hypo.finance.application.port.PaymentProviderPort;
import com.iso.hypo.finance.application.port.dto.paymentprovider.CreditCardRef;
import com.iso.hypo.finance.application.port.dto.paymentprovider.ReceiptRef;
import com.iso.hypo.finance.infrastructure.moneris.api.PaymentMethodsApi;
import com.iso.hypo.finance.infrastructure.moneris.api.PaymentsApi;
import com.iso.hypo.finance.infrastructure.moneris.api.ValidationsApi;
import com.iso.hypo.finance.infrastructure.moneris.invoker.ApiClient;
import com.iso.hypo.finance.infrastructure.moneris.model.AddressVerificationServiceResultCode;
import com.iso.hypo.finance.infrastructure.moneris.model.Card;
import com.iso.hypo.finance.infrastructure.moneris.model.CardSecurityCodeResult;
import com.iso.hypo.finance.infrastructure.moneris.model.CardholderInformation;
import com.iso.hypo.finance.infrastructure.moneris.model.ConvenienceFeeDetailsAmount;
import com.iso.hypo.finance.infrastructure.moneris.model.CreatePaymentRequest;
import com.iso.hypo.finance.infrastructure.moneris.model.CreateValidationRequest;
import com.iso.hypo.finance.infrastructure.moneris.model.CreateValidationRequest.EcommerceIndicatorEnum;
import com.iso.hypo.finance.infrastructure.moneris.model.Payment;
import com.iso.hypo.finance.infrastructure.moneris.model.PaymentIndicator;
import com.iso.hypo.finance.infrastructure.moneris.model.PaymentInformation;
import com.iso.hypo.finance.infrastructure.moneris.model.PaymentMethodBillingAddress;
import com.iso.hypo.finance.infrastructure.moneris.model.PaymentMethodRequestSource;
import com.iso.hypo.finance.infrastructure.moneris.model.PaymentStatus;
import com.iso.hypo.finance.infrastructure.moneris.model.StorePaymentMethod;
import com.iso.hypo.finance.infrastructure.moneris.model.StorePaymentMethodIdRequestAllOfCredentialOnFileInformation;
import com.iso.hypo.finance.infrastructure.moneris.model.StorePaymentMethodRequest;
import com.iso.hypo.finance.infrastructure.moneris.model.Validation;
import com.iso.hypo.finance.infrastructure.moneris.model.ValidationStatus;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
@Primary
public class FinanceMonerisPortAdapter implements PaymentProviderPort {

	private static final Logger logger = LoggerFactory.getLogger(FinanceMonerisPortAdapter.class);

	@Value("${moneris.url}")
	private String monerisUrl;
	
	@Value("${moneris.api.version}")
	private String monerisApiVersion;
	
	@Value("${app.payment.provider.test:true}")
	private boolean paymentProviderTest;
	
	public FinanceMonerisPortAdapter() {
	}

	@Override
	public ReceiptRef verify(PaymentProviderConfigurationEntry config, RequestContext requestContext, CreditCardRef creditCard) {
		java.util.Date createDate = new java.util.Date();

		String xCorrelationId = requestContext.getTrackingNumber();
		String order_id = config.getBrandCode() + "_verify_" + createDate.getTime();
		String access_token = getOAuthToken(config);
		
		ReceiptRef receiptRef = new ReceiptRef();
		
		try {
			ApiClient apiClient = new ApiClient();
			apiClient.setBasePath(monerisUrl);
			apiClient.setAccessToken(access_token);
			apiClient.addDefaultHeader("Accept", "application/json");
			apiClient.addDefaultHeader("Content-Type", "application/json");
			
			CreateValidationRequest validationRequest = new CreateValidationRequest();
			
			validationRequest.setOrderId(order_id);
			validationRequest.setIdempotencyKey(order_id);
			validationRequest.setCustomerReference(creditCard.getCustomerId());
			validationRequest.setEcommerceIndicator(EcommerceIndicatorEnum.AUTHENTICATED_ECOMMERCE);
			
			StorePaymentMethodRequest storePaymentMethodRequest = new StorePaymentMethodRequest();
			storePaymentMethodRequest.setPaymentMethodSource(PaymentMethodRequestSource.CARD);
			
			Card card = new Card();
			card.setCardNumber(creditCard.getCardNumber());
			card.setCardSecurityCode(creditCard.getCvd());
			card.setExpiryMonth(Integer.valueOf(creditCard.getExpirationDate().substring(0, 2)));
			card.setExpiryYear(Integer.valueOf("20" + creditCard.getExpirationDate().substring(2, 4)));
			storePaymentMethodRequest.setCard(card);
			
			CardholderInformation cardHolderInfo = new CardholderInformation();
			cardHolderInfo.setCardholderName(creditCard.getCardHolderName());
			storePaymentMethodRequest.setCardholderInformation(cardHolderInfo);
			
			if (creditCard.getZipCode() != null) {
				PaymentMethodBillingAddress billingAddress = new PaymentMethodBillingAddress();
				billingAddress.setPostalCode(creditCard.getZipCode());
				storePaymentMethodRequest.setBillingAddress(billingAddress);
			}
		
			storePaymentMethodRequest.setStorePaymentMethod(StorePaymentMethod.MERCHANT_INITIATED);
			
			StorePaymentMethodIdRequestAllOfCredentialOnFileInformation cof = new StorePaymentMethodIdRequestAllOfCredentialOnFileInformation();
			cof.setPaymentIndicator(PaymentIndicator.UNSCHEDULED_CREDENTIAL_ON_FILE); // unscheduled Credential on File (first transactions only)
			cof.setPaymentInformation(PaymentInformation.FIRST); // first transaction
			storePaymentMethodRequest.setCredentialOnFileInformation(cof);
		
			validationRequest.setPaymentMethod(storePaymentMethodRequest);
			
			if (paymentProviderTest) {
				try {
					ObjectMapper mapper = new ObjectMapper();
					System.out.println(
					    mapper.writerWithDefaultPrettyPrinter()
					          .writeValueAsString(validationRequest));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			ValidationsApi validationsApi = new ValidationsApi(apiClient);
	
			Validation validation = validationsApi.createValidation(monerisApiVersion, config.getMerchandId(), validationRequest, xCorrelationId);
	
			ValidationStatus validationStatus = validation.getValidationStatus();
			
			// Save response as log.
			receiptRef.setProviderRawResponse(validation);
			
			if (validationStatus == ValidationStatus.SUCCEEDED) {
				receiptRef.setApproved(true);
				receiptRef.setError(false);
				
				receiptRef.setTxnNumber(validation.getTransactionDetails().getTransactionUniqueId());
				receiptRef.setProviderResponseCode(validation.getTransactionDetails().getResponseCode());
				receiptRef.setISO(validation.getTransactionDetails().getIsoResponseCode());
				receiptRef.setAuthCode(validation.getTransactionDetails().getAuthorizationCode());
				receiptRef.setMessage(validation.getTransactionDetails().getMessage());
				receiptRef.setCardType(validation.getPaymentMethod().getPaymentMethodInformation().getCardInformation().getCardBrand().getValue());
				receiptRef.setPaymentMethodId(validation.getPaymentMethod().getPaymentMethodId());
				receiptRef.setCardNumberMasked(validation.getPaymentMethod().getPaymentMethodInformation().getCardInformation().getLastFour());
				receiptRef.setReferenceNum(validation.getPaymentMethod().getPaymentMethodInformation().getPaymentAccountReference());
				receiptRef.setIssuerId(validation.getCredentialOnFileResponse().getIssuerId());
				receiptRef.setTransDate(validation.getCreatedAt());
				
				receiptRef.setCvdResultCode(CardSecurityCodeResult.MATCH.equals(validation.getVerificationDetails().getCardSecurityCodeResultCode()));
				receiptRef.setAvsResultCode(AddressVerificationServiceResultCode.PARTIAL_MATCH.equals(validation.getVerificationDetails().getAddressVerificationServiceResultCode()));
				
			} else {
				receiptRef.setApproved(false);
				receiptRef.setError(true);
				receiptRef.setTxnNumber(validation.getTransactionDetails().getTransactionUniqueId());
				receiptRef.setProviderResponseCode(validation.getTransactionDetails().getResponseCode());
				receiptRef.setISO(validation.getTransactionDetails().getIsoResponseCode());
				receiptRef.setMessage(validation.getTransactionDetails().getMessage());
			}
		} catch (Exception e) {
			receiptRef.setApproved(false);
			receiptRef.setError(true);
			receiptRef.setMessage(e.getMessage());
	
			logger.error("Moneris verify failed, error: {}", e.getMessage());
		}
		return receiptRef;
	}
	
	@Override
	public ReceiptRef purchase(
			PaymentProviderConfigurationEntry config, 
			RequestContext requestContext, 
			CreditCardRef creditCard, 
			String orderId,
			String customerId, 
			int amount,
			String currency) {
		java.util.Date createDate = new java.util.Date();

		String xCorrelationId = requestContext.getTrackingNumber();
		String order_id = config.getBrandCode() + "_verify_" + createDate.getTime();
		String access_token = getOAuthToken(config);
		
		ReceiptRef receiptRef = new ReceiptRef();
		
		try {
			ApiClient apiClient = new ApiClient();
			apiClient.setBasePath(monerisUrl);
			apiClient.setAccessToken(access_token);
			apiClient.addDefaultHeader("Accept", "application/json");
			apiClient.addDefaultHeader("Content-Type", "application/json");
			
			CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
			paymentRequest.setIdempotencyKey(order_id);
			paymentRequest.setOrderId(order_id);
		//	paymentRequest.setInvoiceNumber(order_id);
			paymentRequest.setCustomerReference(creditCard.getCustomerId());
			
			ConvenienceFeeDetailsAmount convenienceFeeDetailsAmount = new ConvenienceFeeDetailsAmount();
			convenienceFeeDetailsAmount.setAmount(amount);
			convenienceFeeDetailsAmount.setCurrency(currency);
			paymentRequest.setAmount(convenienceFeeDetailsAmount);
			
			StorePaymentMethodRequest paymentMethod = new StorePaymentMethodRequest();
			paymentMethod.setPaymentMethodSource(PaymentMethodRequestSource.PAYMENT_METHOD_ID);
			paymentMethod.setPaymentMethodId(creditCard.getPermanentToken());
			paymentRequest.setPaymentMethod(paymentMethod);
			
			StorePaymentMethodIdRequestAllOfCredentialOnFileInformation cof = new StorePaymentMethodIdRequestAllOfCredentialOnFileInformation();
			cof.setPaymentIndicator(PaymentIndicator.MERCHANT_INITIATED);
			cof.setPaymentInformation(PaymentInformation.SUBSEQUENT); 
			cof.setIssuerId(creditCard.getIssuerId());
			paymentMethod.setCredentialOnFileInformation(cof);
		
			paymentRequest.setEcommerceIndicator(CreatePaymentRequest.EcommerceIndicatorEnum.SSL_MERCHANT);
			paymentRequest.setAutomaticCapture(true);
			
			if (paymentProviderTest) {
				try {
					ObjectMapper mapper = new ObjectMapper();
					System.out.println(
					    mapper.writerWithDefaultPrettyPrinter()
					          .writeValueAsString(paymentRequest));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			PaymentsApi paymentApi = new PaymentsApi(apiClient);
	
			Payment payment = paymentApi.createPayments(monerisApiVersion, config.getMerchandId(), paymentRequest, xCorrelationId);
	
			PaymentStatus paymentStatus = payment.getPaymentStatus();
	
			// Save response as log.
			receiptRef.setProviderRawResponse(payment);
			
			if (paymentStatus == PaymentStatus.SUCCEEDED) {
				receiptRef.setApproved(true);
				receiptRef.setError(false);
				
				receiptRef.setTransAmount(payment.getAmount().getAmount());
				receiptRef.setTxnNumber(payment.getTransactionDetails().getTransactionUniqueId());
				receiptRef.setPaymentId(payment.getPaymentId());
				receiptRef.setTransDate(payment.getTransactionDateTime());
				
				receiptRef.setProviderResponseCode(payment.getTransactionDetails().getResponseCode());
				receiptRef.setISO(payment.getTransactionDetails().getIsoResponseCode());
				receiptRef.setAuthCode(payment.getTransactionDetails().getAuthorizationCode());
				receiptRef.setMessage(payment.getTransactionDetails().getMessage());
				receiptRef.setCardType(payment.getPaymentMethod().getPaymentMethodInformation().getCardInformation().getCardBrand().getValue());
				receiptRef.setCardNumberMasked(payment.getPaymentMethod().getPaymentMethodInformation().getCardInformation().getLastFour());
				receiptRef.setReferenceNum(payment.getPaymentMethod().getPaymentMethodInformation().getPaymentAccountReference());
				receiptRef.setIssuerId(payment.getCredentialOnFileResponse().getIssuerId());
			
			} else {
				receiptRef.setApproved(false);
				receiptRef.setError(true);
				receiptRef.setTxnNumber(payment.getTransactionDetails().getTransactionUniqueId());
				receiptRef.setProviderResponseCode(payment.getTransactionDetails().getResponseCode());
				receiptRef.setISO(payment.getTransactionDetails().getIsoResponseCode());
				receiptRef.setMessage(payment.getTransactionDetails().getMessage());
			}
		} catch (Exception e) {
			receiptRef.setApproved(false);
			receiptRef.setError(true);
			receiptRef.setMessage(e.getMessage());
	
			logger.error("Moneris purchase failed, error: {}", e.getMessage());
		}
		return receiptRef;
	}

	@Override
	public ReceiptRef delete(PaymentProviderConfigurationEntry config, RequestContext requestContext, String paymentMethodId) {
		String xCorrelationId = requestContext.getTrackingNumber();
		String access_token = getOAuthToken(config);
		
		ReceiptRef receiptRef = new ReceiptRef();
		
		try {
			ApiClient apiClient = new ApiClient();
			apiClient.setBasePath(monerisUrl);
			apiClient.setAccessToken(access_token);
			apiClient.addDefaultHeader("Accept", "application/json");
			apiClient.addDefaultHeader("Content-Type", "application/json");
	
			PaymentMethodsApi paymentMethodApi = new PaymentMethodsApi(apiClient);
	
			paymentMethodApi.deletePaymentMethodWithHttpInfo(monerisApiVersion, paymentMethodId, config.getMerchandId(), xCorrelationId);
	
		} catch (Exception e) {
			receiptRef.setApproved(false);
			receiptRef.setError(true);
			receiptRef.setMessage(e.getMessage());
	
			logger.error("Moneris payment failed, error: {}", e.getMessage());
		}
		return receiptRef;		

	}

	/**
	 * Requests a Bearer token from the Moneris OAuth2 endpoint using the
	 * client_credentials grant.  Sandbox vs production URL is driven by
	 * {@code config.isTestMode()}.
	 *
	 * @param config payment-provider configuration holding clientId / clientSecret
	 * @return the raw access_token string to be passed as {@code Authorization: Bearer <token>}
	 * @throws RuntimeException if the token endpoint returns a non-2xx status or the
	 *                          response cannot be parsed
	 */
	private String getOAuthToken(PaymentProviderConfigurationEntry config) {

		String tokenUrl = monerisUrl + "/oauth2/token";

		FormBody formBody = new FormBody.Builder()
				.add("grant_type", "client_credentials")
				.add("client_id",     config.getClientId())
				.add("client_secret", config.getClientSecret())
				.add("scope", "payment.write")
				.build();

		Request request = new Request.Builder()
				.url(tokenUrl)
				.post(formBody)
				.addHeader("Accept", "application/json")
				.build();

		OkHttpClient client = new OkHttpClient();

		try (Response response = client.newCall(request).execute()) {

			if (!response.isSuccessful()) {
				String errorBody = response.body() != null ? response.body().string() : "(empty)";
				logger.error("Moneris OAuth token request failed — HTTP {}: {}", response.code(), errorBody);
				throw new RuntimeException(
						"Moneris OAuth token request failed with HTTP " + response.code());
			}

			String responseBody = response.body().string();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(responseBody);

			JsonNode tokenNode = json.get("access_token");
			if (tokenNode == null || tokenNode.isNull()) {
				logger.error("Moneris OAuth response did not contain an access_token: {}", responseBody);
				throw new RuntimeException("Moneris OAuth response missing access_token");
			}

			logger.debug("Moneris OAuth token obtained successfully");
			return tokenNode.asText();

		} catch (IOException e) {
			logger.error("IOException while requesting Moneris OAuth token: {}", e.getMessage());
			throw new RuntimeException("Failed to obtain Moneris OAuth token", e);
		}
	}

}
