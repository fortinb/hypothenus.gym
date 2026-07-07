package com.iso.hypo.finance.infrastructure.port.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.port.PaymentProviderConfigurationEntry;
import com.iso.hypo.finance.application.port.PaymentProviderPort;
import com.iso.hypo.finance.application.port.dto.paymentprovider.CreditCardRef;
import com.iso.hypo.finance.application.port.dto.paymentprovider.ReceiptRef;

import JavaAPI.AvsInfo;
import JavaAPI.CardVerification;
import JavaAPI.CofInfo;
import JavaAPI.CvdInfo;
import JavaAPI.HttpsPostRequest;
import JavaAPI.Receipt;
import JavaAPI.Refund;
import JavaAPI.ResAddCC;
import JavaAPI.ResDelete;
import JavaAPI.ResPurchaseCC;

@Component
public class FinanceMonerisOldPortAdapter implements PaymentProviderPort {

	private static final Logger logger = LoggerFactory.getLogger(FinanceMonerisOldPortAdapter.class);

	public FinanceMonerisOldPortAdapter() {
	}

	@Override
	public ReceiptRef verify(PaymentProviderConfigurationEntry config,  RequestContext requestContext, CreditCardRef creditCard) {
		java.util.Date createDate = new java.util.Date();

		String order_id = config.getBrandCode() + "_verify_" + createDate.getTime();
		String crypt = "7";

		ReceiptRef receiptRef = new ReceiptRef();
		
		try {
			boolean status_check = false;

			AvsInfo avsCheck = new AvsInfo();
			if (creditCard.getZipCode() != null) {
				avsCheck.setAvsZipCode(creditCard.getZipCode());
			}

			CvdInfo cvdCheck = new CvdInfo();
			cvdCheck.setCvdIndicator("1"); // 1: CVD value is present.
			cvdCheck.setCvdValue(creditCard.getCvd());

			CardVerification cardVerification = new CardVerification();
			cardVerification.setOrderId(order_id);
			cardVerification.setPan(creditCard.getCardNumber());
			cardVerification.setExpdate(creditCard.getExpirationDate());
			cardVerification.setCryptType(crypt);
			cardVerification.setAvsInfo(avsCheck);
			cardVerification.setCvdInfo(cvdCheck);

			// optional - Credential on File details
			CofInfo cof = new CofInfo();
			cof.setPaymentIndicator("C"); // C - unscheduled Credential on File (first transactions only)
			cof.setPaymentInformation("0"); // 0 - first transaction in a series (storing payment details provided by the cardholder)

			cardVerification.setCofInfo(cof);

			HttpsPostRequest mpgReq = new HttpsPostRequest();
			mpgReq.setProcCountryCode(creditCard.getCountryCode());
			mpgReq.setTestMode(true); // false or comment out this line for production transactions
			mpgReq.setStoreId(config.getStoreId());
			mpgReq.setApiToken(config.getApiKey());
			mpgReq.setTransaction(cardVerification);
			mpgReq.setStatusCheck(status_check);
			mpgReq.send();

			Receipt receipt = mpgReq.getReceipt();

			debugReceipt(receipt);

			boolean approved = "027".equals(receipt.getResponseCode());
			boolean completed = "true".equals(receipt.getComplete());
			boolean timeout = "true".equals(receipt.getTimedOut());

			if (!completed || timeout) {
				receiptRef.setError(true);
				receiptRef.setMessage(receipt.getMessage());
			} else {
				receiptRef.setApproved(approved);
				receiptRef.setError(false);
				receiptRef.setCardType(receipt.getCardType());
				receiptRef.setTxnNumber(receipt.getTxnNumber());
				receiptRef.setReceiptId(receipt.getReceiptId());
				receiptRef.setTransType(receipt.getTransType());
				receiptRef.setReferenceNum(receipt.getReferenceNum());
				receiptRef.setProviderResponseCode(receipt.getResponseCode());
				receiptRef.setISO(receipt.getISO());
				receiptRef.setMessage(receipt.getMessage());
				receiptRef.setAuthCode(receipt.getAuthCode());
				receiptRef.setTransDate(receipt.getTransDate());
				receiptRef.setTransTime(receipt.getTransTime());
				receiptRef.setIssuerId(receipt.getIssuerId());
				receiptRef.setCvdResultCode("1M".equals(receipt.getCvdResultCode()));
				receiptRef.setAvsResultCode("Z".equals(receipt.getAvsResultCode()));
			}
		} catch (Exception e) {
			receiptRef.setApproved(false);
			receiptRef.setError(true);
			receiptRef.setMessage(e.getMessage());

			logger.error("Moneris card verification failed for orderId: {}, error: {}", order_id, e.getMessage());
		}

		return receiptRef;
	}

	@Override
	public ReceiptRef register(PaymentProviderConfigurationEntry config,  RequestContext requestContext, CreditCardRef creditCard) {
		String crypt = "7";
		boolean status_check = false;

		ReceiptRef receiptRef = new ReceiptRef();

		try {

			ResAddCC resaddcc = new ResAddCC();
			resaddcc.setPan(creditCard.getCardNumber());
			resaddcc.setExpdate(creditCard.getExpirationDate());
			resaddcc.setCryptType(crypt);
			resaddcc.setCustId(creditCard.getCustomerId());

			if (creditCard.getPhoneNumber() != null) {
				resaddcc.setPhone(creditCard.getPhoneNumber());
			}

			if (creditCard.getEmail() != null) {
				resaddcc.setEmail(creditCard.getEmail());
			}

			if (creditCard.getCardHolderName() != null) {
				resaddcc.setNote(creditCard.getCardHolderName());
			}

			// Credential on File details
			CofInfo cof = new CofInfo();
			cof.setIssuerId(creditCard.getIssuerId());

			resaddcc.setCofInfo(cof);

			HttpsPostRequest mpgReq = new HttpsPostRequest();
			mpgReq.setTestMode(true); // false or comment out this line for production transactions
			mpgReq.setStoreId(config.getStoreId());
			mpgReq.setApiToken(config.getApiKey());
			mpgReq.setTransaction(resaddcc);
			mpgReq.setStatusCheck(status_check);
			mpgReq.send();

			Receipt receipt = mpgReq.getReceipt();

			debugReceipt(receipt);

			boolean approved = "001".equals(receipt.getResponseCode());
			boolean completed = "true".equals(receipt.getComplete());
			boolean timeout = "true".equals(receipt.getTimedOut());

			if (!completed || timeout) {
				receiptRef.setError(true);
				receiptRef.setMessage(receipt.getMessage());
			} else {
				receiptRef.setApproved(approved);
				receiptRef.setError(false);
				receiptRef.setPermanentToken(receipt.getDataKey());
				receiptRef.setTransDate(receipt.getTransDate());
				receiptRef.setTransTime(receipt.getTransTime());
				receiptRef.setCardNumberMasked(receipt.getMaskedPan());
			}
		} catch (Exception e) {
			receiptRef.setApproved(false);
			receiptRef.setError(true);
			receiptRef.setMessage(e.getMessage());

			logger.error("Moneris card registration failed, error: {}", e.getMessage());
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
			String amount) {
		String crypt = "7";
		boolean status_check = false;

		ReceiptRef receiptRef = new ReceiptRef();

		try {

			ResPurchaseCC resPurchaseCC = new ResPurchaseCC();
			resPurchaseCC.setDataKey(creditCard.getPermanentToken());
			resPurchaseCC.setOrderId(orderId);
			resPurchaseCC.setCustId(customerId);
			resPurchaseCC.setAmount(amount);
			resPurchaseCC.setCryptType(crypt);

			// Credential on File details
			CofInfo cof = new CofInfo();
			cof.setPaymentIndicator("U");
			cof.setPaymentInformation("2");
			cof.setIssuerId(creditCard.getIssuerId());

			resPurchaseCC.setCofInfo(cof);

			HttpsPostRequest mpgReq = new HttpsPostRequest();
			mpgReq.setTestMode(true); // false or comment out this line for production transactions
			mpgReq.setStoreId(config.getStoreId());
			mpgReq.setApiToken(config.getApiKey());
			mpgReq.setTransaction(resPurchaseCC);
			mpgReq.setStatusCheck(status_check);
			mpgReq.send();

			Receipt receipt = mpgReq.getReceipt();

			debugReceipt(receipt);

			boolean approved = "027".equals(receipt.getResponseCode());
			boolean completed = "true".equals(receipt.getComplete());
			boolean timeout = "true".equals(receipt.getTimedOut());

			if (!completed || timeout) {
				receiptRef.setError(true);
				receiptRef.setMessage(receipt.getMessage());
			} else {
				receiptRef.setApproved(approved);
				receiptRef.setError(false);
				receiptRef.setTransAmount(receipt.getTransAmount());
				receiptRef.setTxnNumber(receipt.getTxnNumber());
				receiptRef.setReceiptId(receipt.getReceiptId());
				receiptRef.setTransType(receipt.getTransType());
				receiptRef.setReferenceNum(receipt.getReferenceNum());
				receiptRef.setProviderResponseCode(receipt.getResponseCode());
				receiptRef.setAuthCode(receipt.getAuthCode());
				receiptRef.setISO(receipt.getISO());
				receiptRef.setMessage(receipt.getMessage());
				receiptRef.setAuthCode(receipt.getAuthCode());
				receiptRef.setTransDate(receipt.getTransDate());
				receiptRef.setTransTime(receipt.getTransTime());
			}
		} catch (Exception e) {
			receiptRef.setApproved(false);
			receiptRef.setError(true);
			receiptRef.setMessage(e.getMessage());

			logger.error("Moneris payment failed, error: {}", e.getMessage());
		}

		return receiptRef;
	}

	@Override
	public ReceiptRef refund(
			PaymentProviderConfigurationEntry config, 
			 RequestContext requestContext,
			CreditCardRef creditCard, 
			String orderId,
			String customerId, 
			String txnNumber,
			String amount) {
		String crypt = "7";
		boolean status_check = false;
		String dynamic_descriptor = orderId; // Dynamic descriptor can be up to 25 characters, and will appear on the customer's card statement. Using orderId for simplicity, but it can be customized as needed.
		ReceiptRef receiptRef = new ReceiptRef();

		try {

			Refund refund = new Refund();
			refund.setTxnNumber(txnNumber);
			refund.setOrderId(orderId);
			refund.setAmount(amount);
			refund.setCryptType(crypt);
			refund.setCustId(customerId);
			refund.setDynamicDescriptor(dynamic_descriptor);

			HttpsPostRequest mpgReq = new HttpsPostRequest();
			mpgReq.setTestMode(true); // false or comment out this line for production transactions
			mpgReq.setStoreId(config.getStoreId());
			mpgReq.setApiToken(config.getApiKey());
			mpgReq.setTransaction(refund);
			mpgReq.setStatusCheck(status_check);
			mpgReq.send();

			Receipt receipt = mpgReq.getReceipt();

			debugReceipt(receipt);

			boolean approved = "027".equals(receipt.getResponseCode());
			boolean completed = "true".equals(receipt.getComplete());
			boolean timeout = "true".equals(receipt.getTimedOut());

			if (!completed || timeout) {
				receiptRef.setError(true);
				receiptRef.setMessage(receipt.getMessage());
			} else {
				receiptRef.setApproved(approved);
				receiptRef.setError(false);
				receiptRef.setTransAmount(receipt.getTransAmount());
				receiptRef.setTxnNumber(receipt.getTxnNumber());
				receiptRef.setReceiptId(receipt.getReceiptId());
				receiptRef.setTransType(receipt.getTransType());
				receiptRef.setReferenceNum(receipt.getReferenceNum());
				receiptRef.setProviderResponseCode(receipt.getResponseCode());
				receiptRef.setAuthCode(receipt.getAuthCode());
				receiptRef.setISO(receipt.getISO());
				receiptRef.setMessage(receipt.getMessage());
				receiptRef.setAuthCode(receipt.getAuthCode());
				receiptRef.setTransDate(receipt.getTransDate());
				receiptRef.setTransTime(receipt.getTransTime());
			}
		} catch (Exception e) {
			receiptRef.setApproved(false);
			receiptRef.setError(true);
			receiptRef.setMessage(e.getMessage());

			logger.error("Moneris payment failed, error: {}", e.getMessage());
		}

		return receiptRef;
	}

	@Override
	public ReceiptRef delete(PaymentProviderConfigurationEntry config, RequestContext requestContext, CreditCardRef creditCard) {
		boolean status_check = false;
		
		ReceiptRef receiptRef = new ReceiptRef();
		
		try {

			ResDelete resDelete = new ResDelete(creditCard.getPermanentToken());

			HttpsPostRequest mpgReq = new HttpsPostRequest();
			mpgReq.setTestMode(true); // false or comment out this line for production transactions
			mpgReq.setStoreId(config.getStoreId());
			mpgReq.setApiToken(config.getApiKey());
			mpgReq.setTransaction(resDelete);
			mpgReq.setStatusCheck(status_check);
			mpgReq.send();

			Receipt receipt = mpgReq.getReceipt();

			debugReceipt(receipt);

			boolean approved = "001".equals(receipt.getResponseCode());
			boolean completed = "true".equals(receipt.getComplete());
			boolean timeout = "true".equals(receipt.getTimedOut());

			if (!completed || timeout) {
				receiptRef.setError(true);
				receiptRef.setMessage(receipt.getMessage());
			} else {
				receiptRef.setApproved(approved);
				receiptRef.setError(false);
				receiptRef.setAuthCode(receipt.getAuthCode());
				receiptRef.setTransDate(receipt.getTransDate());
				receiptRef.setTransTime(receipt.getTransTime());
			}
		} catch (Exception e) {
			receiptRef.setApproved(false);
			receiptRef.setError(true);
			receiptRef.setMessage(e.getMessage());

			logger.error("Moneris payment failed, error: {}", e.getMessage());
		}

		return receiptRef;
	}
	
	private void debugReceipt(Receipt receipt) {
		if (!logger.isDebugEnabled()) {
			return;
		}

		logger.debug("Moneris Receipt Details:");
		logger.debug("CardType = {}", receipt.getCardType());
		logger.debug("DataKey = {}", receipt.getDataKey());
		logger.debug("TransAmount = {}", receipt.getTransAmount());
		logger.debug("TxnNumber = {}", receipt.getTxnNumber());
		logger.debug("ReceiptId = {}", receipt.getReceiptId());
		logger.debug("TransType = {}", receipt.getTransType());
		logger.debug("ReferenceNum = {}", receipt.getReferenceNum());
		logger.debug("ResponseCode = {}", receipt.getResponseCode());
		logger.debug("ResSuccess = " + receipt.getResSuccess());
		logger.debug("ISO = {}", receipt.getISO());
		logger.debug("BankTotals = {}", receipt.getBankTotals());
		logger.debug("Cust ID = " + receipt.getResCustId());
		logger.debug("Phone = " + receipt.getResPhone());
		logger.debug("Email = " + receipt.getResEmail());
		logger.debug("Note = " + receipt.getResNote());
		logger.debug("Masked Pan = " + receipt.getResMaskedPan());
		logger.debug("Exp Date = " + receipt.getResExpdate());
		logger.debug("Crypt Type = " + receipt.getResCryptType());
		logger.debug("Message = {}", receipt.getMessage());
		logger.debug("AuthCode = {}", receipt.getAuthCode());
		logger.debug("Complete = {}", receipt.getComplete());
		logger.debug("TransDate = {}", receipt.getTransDate());
		logger.debug("TransTime = {}", receipt.getTransTime());
		logger.debug("Ticket = {}", receipt.getTicket());
		logger.debug("TimedOut = {}", receipt.getTimedOut());
		logger.debug("IsVisaDebit = {}", receipt.getIsVisaDebit());
		logger.debug("IssuerId = {}", receipt.getIssuerId());
		logger.debug("CardAccountNumber = {}", receipt.getCardAccountNumber());
		logger.debug("AvsResponseCode = {}", receipt.getAvsResponseCode());
		logger.debug("AvsResultCode = {}", receipt.getAvsResultCode());
		logger.debug("CardHolderName = {}", receipt.getCardCardHolderName());
		logger.debug("CvvResponseCode = {}", receipt.getCvvResponseCode());
		logger.debug("CvdResultCode = {}", receipt.getCvdResultCode());
	}


}
