package com.iso.hypo.finance.infrastructure.moneris;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.finance.infrastructure.moneris.model.Card;
import com.iso.hypo.finance.infrastructure.moneris.model.CardholderInformation;
import com.iso.hypo.finance.infrastructure.moneris.model.CreateValidationRequest;
import com.iso.hypo.finance.infrastructure.moneris.model.PaymentMethodRequestSource;
import com.iso.hypo.finance.infrastructure.moneris.model.StorePaymentMethodRequest;

public class StorePaymentMethodSerializationTest {

    // Mixin to force serialization of the paymentMethodSource property on the base class
    @JsonIgnoreProperties(value = {}) // clear ignored properties so mixin can re-expose the discriminator
    public abstract static class StorePaymentMethodRequestMixin {
        @JsonProperty("paymentMethodSource")
        public abstract PaymentMethodRequestSource getPaymentMethodSource();
    }

    @Test
    public void baseClassWithMixinSerializesPaymentMethodSourceAsEnumValue() throws JsonProcessingException {
        // Build a StorePaymentMethodRequest (base class used by generator merging child props)
        StorePaymentMethodRequest baseRequest = new StorePaymentMethodRequest();

        Card card = new Card();
        card.setCardNumber("4111111111111111");
        card.setExpiryMonth(12);
        card.setExpiryYear(2030);
        baseRequest.setCard(card);

        CardholderInformation holder = new CardholderInformation();
        holder.setCardholderName("Test Cardholder");
        baseRequest.setCardholderInformation(holder);

        // set the enum explicitly
        baseRequest.setPaymentMethodSource(PaymentMethodRequestSource.CARD);

        ObjectMapper mapper = new ObjectMapper();
        // Add mixin so the getter is honored for serialization despite generated @JsonIgnoreProperties
        mapper.addMixIn(StorePaymentMethodRequest.class, StorePaymentMethodRequestMixin.class);

        String json = mapper.writeValueAsString(baseRequest);
        System.out.println(json);

        assertTrue(json.contains("\"paymentMethodSource\":\"CARD\""), "JSON did not contain expected discriminator: " + json);
    }

    @Test
    public void discriminatorIsCardWhenSerializingCardRequest() throws JsonProcessingException {
        // Build a StorePaymentMethodCardRequest (concrete subtype)
        StorePaymentMethodRequest cardRequest = new StorePaymentMethodRequest();

        Card card = new Card();
        card.setCardNumber("4111111111111111");
        card.setExpiryMonth(12);
        card.setExpiryYear(2030);
        cardRequest.setCard(card);

        CardholderInformation holder = new CardholderInformation();
        holder.setCardholderName("Test Cardholder");
        cardRequest.setCardholderInformation(holder);

        // set the enum explicitly
        cardRequest.setPaymentMethodSource(PaymentMethodRequestSource.CARD);

        CreateValidationRequest validationRequest = new CreateValidationRequest();
        validationRequest.setPaymentMethod(cardRequest);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(validationRequest);
        System.out.println(json);

        // Expect the polymorphic discriminator property to contain the subtype name "CARD"
        assertTrue(json.contains("\"paymentMethodSource\":\"CARD\""), "JSON did not contain expected discriminator: " + json);
    }
}