package com.iso.hypo.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.iso.hypo.common.domain.model.LocalizedString;
import com.iso.hypo.common.domain.model.enumeration.LanguageEnum;
import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.common.domain.model.finance.Currency;
import com.iso.hypo.common.domain.model.location.Address;
import com.iso.hypo.finance.domain.model.Tax;
import com.iso.hypo.sale.domain.model.BillingDetail;
import com.iso.hypo.sale.domain.model.DiscountDetail;
import com.iso.hypo.sale.domain.model.MembershipPlan;
import com.iso.hypo.sale.domain.model.Order;
import com.iso.hypo.sale.domain.model.OrderItem;
import com.iso.hypo.sale.domain.model.PaymentDetail;
import com.iso.hypo.sale.domain.model.ShippingDetail;
import com.iso.hypo.sale.domain.model.enumeration.OrderProcessingStateEnum;
import com.iso.hypo.sale.domain.model.enumeration.OrderStatusEnum;
import com.iso.hypo.sale.domain.model.enumeration.PaymentMethodEnum;
import com.iso.hypo.sale.domain.model.enumeration.ShippingMethodEnum;

import net.datafaker.Faker;

public class OrderBuilder {
	private static Faker faker = new Faker();

	private static final Currency CAD = new Currency("Canadian Dollar", "CAD", "$");

	public static Order build(String brandUuid, String memberUuid, List<MembershipPlan> membershipPlans) {
		List<OrderItem> items = buildItems(membershipPlans);
		Cost subTotal = null;
		Cost shippingTotal = null;
		Cost discountTotal = null;
		Cost deposit = null;
		Cost total = null;
		
		Order entity = new Order(
				UUID.randomUUID().toString(),
				brandUuid,
				memberUuid,
				buildBillingDetail(),
				faker.numerify("ORD-#####"),
				OrderStatusEnum.created,
				OrderProcessingStateEnum.idle,
				items,
				buildShippingDetail(),
				buildDiscountDetail(),
				buildPaymentDetail(),
				deposit,
				subTotal,
				null,
				shippingTotal,
				discountTotal,
				total,
				Instant.now(),
				null);
		
		return entity;
	}

	public static BillingDetail buildBillingDetail() {
		return new BillingDetail(
				buildAddress(),
				faker.internet().emailAddress(),
				faker.name().fullName());
	}

	public static ShippingDetail buildShippingDetail() {
		return new ShippingDetail(
				buildAddress(),
				ShippingMethodEnum.standard,
				faker.company().name(),
				faker.numerify("TRACK-########"),
				null,
				null);
	}

	public static DiscountDetail buildDiscountDetail() {
		return new DiscountDetail(
				faker.numerify("COUPON-####"),
				10.0,
				buildCost(10));
	}

	public static PaymentDetail buildPaymentDetail() {
		return new PaymentDetail(
				PaymentMethodEnum.credit,
				UUID.randomUUID().toString());
	}

	public static List<OrderItem> buildItems(List<MembershipPlan> membershipPlans) {
		ArrayList<OrderItem> items = new ArrayList<>();
		for (MembershipPlan membershipPlan : membershipPlans) {
			items.add(buildOrderItem(membershipPlan));
		}
		return items;
	}

	public static OrderItem buildOrderItem(MembershipPlan membershipPlan) {
		return new OrderItem(
				buildMembershipPlan(membershipPlan.getUuid()),
				membershipPlan.getPrice(),
				1,
				membershipPlan.getPrice());
	}

	public static MembershipPlan buildMembershipPlan(String membershipPlanUuid) {
		MembershipPlan membershipPlan = new MembershipPlan();
		membershipPlan.setUuid(membershipPlanUuid);
		return membershipPlan;
	}
	
	public static List<Tax> buildTaxes() {
		ArrayList<Tax> taxes = new ArrayList<>();
		taxes.add(buildTax("GST", 0.05));
		taxes.add(buildTax("QST", 0.09975));
		return taxes;
	}

	public static Tax buildTax(String code, double rate) {
		Tax tax = new Tax();
		tax.setCode(buildTaxCode(code));
		tax.setName(buildTaxName(code));
		tax.setRate(rate);
		tax.setIsIncludedInPrice(false);
		return tax;
	}
	
	public static List<LocalizedString> buildTaxCode(String code) {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(code, LanguageEnum.fr));
		items.add(new LocalizedString(code, LanguageEnum.en));

		return items;
	}
	
	public static List<LocalizedString> buildTaxName(String name) {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(name, LanguageEnum.fr));
		items.add(new LocalizedString(name, LanguageEnum.en));

		return items;
	}
	public static Cost buildCost(int amount) {
		return new Cost(amount, CAD);
	}

	public static Address buildAddress() {
		return new Address(
				faker.address().buildingNumber(),
				faker.address().streetName(),
				null,
				faker.address().cityName(),
				faker.address().countryCode(),
				faker.address().stateAbbr(),
				faker.address().zipCode());
	}
}