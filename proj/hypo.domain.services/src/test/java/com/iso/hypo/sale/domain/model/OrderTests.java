package com.iso.hypo.sale.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.common.domain.model.finance.Currency;
import com.iso.hypo.finance.domain.model.Tax;
import com.iso.hypo.sale.domain.model.enumeration.ShippingMethodEnum;

class OrderTests {

    private Currency cad;

    @BeforeEach
    void setUp() {
        cad = new Currency("Canadian Dollar", "CAD", "$");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private MembershipPlan buildMembershipPlan(int priceAmount) {
    	MembershipPlan plan = new MembershipPlan();
        plan.setUuid("plan-uuid");
        plan.setPrice(new Cost(priceAmount, cad));
        return plan;
    }

    private OrderItem buildOrderItem(MembershipPlan plan, int quantity) {
        OrderItem item = new OrderItem();
        item.setMembershipPlan(plan);
        item.setQuantity(quantity);
        return item;
    }

    private Order buildOrder(List<OrderItem> items, ShippingDetail shipping,
                            DiscountDetail discount, List<Tax> taxes, Cost deposit) {
        Order order = new Order();
        order.setCurrency(cad);
        order.setItems(items);
        order.setShippingDetail(shipping);
        order.setDiscountDetail(discount);
        order.setTaxes(taxes);
        order.setDeposit(deposit);
        return order;
    }

    private ShippingDetail buildShippingDetail(ShippingMethodEnum method) {
        ShippingDetail detail = new ShippingDetail();
        detail.setShippingMethod(method);
        return detail;
    }

    // =========================================================================
    // OrderItem tests
    // =========================================================================

    @Nested
    @DisplayName("OrderItem.CalculateCost()")
    class OrderItemCalculateCostTests {

        @Test
        @DisplayName("should set unitPrice from plan price and compute itemTotal")
        void calculateCost_setsUnitPriceAndItemTotal() {
        	MembershipPlan plan = buildMembershipPlan(10000);
            OrderItem item = buildOrderItem(plan, 2);

            item.CalculateCost();

            assertEquals(10000, item.getUnitPrice().getAmount());
            assertEquals(20000, item.getItemTotal().getAmount());
            assertEquals(cad.getCode(), item.getItemTotal().getCurrency().getCode());
        }

        @Test
        @DisplayName("should default quantity to 1 when quantity is 0")
        void calculateCost_defaultsQuantityToOne() {
        	MembershipPlan plan = buildMembershipPlan(5000);
            OrderItem item = buildOrderItem(plan, 0);

            item.CalculateCost();

            assertEquals(1, item.getQuantity());
            assertEquals(5000, item.getItemTotal().getAmount());
        }
    }

    // =========================================================================
    // Order.calculateCost() tests
    // =========================================================================

    @Nested
    @DisplayName("Order.calculateCost()")
    class OrderCalculateCostTests {

        @Test
        @DisplayName("should compute subTotal as sum of item totals")
        void calculateCost_computesSubTotal() {
            OrderItem item1 = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
            OrderItem item2 = new OrderItem(null, new Cost(10000, cad), 2, new Cost(20000, cad));

            Order order = buildOrder(List.of(item1, item2), null, null, null, null);
            order.calculateCost();

            assertEquals(30000, order.getSubTotal().getAmount());
        }

        @Test
        @DisplayName("should compute total with no shipping, no discount, no taxes")
        void calculateCost_noShippingNoDiscountNoTax() {
            OrderItem item = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));

            Order order = buildOrder(List.of(item), null, null, null, null);
            order.calculateCost();

            assertEquals(10000, order.getSubTotal().getAmount());
            assertEquals(0, order.getShippingTotal().getAmount());
            assertEquals(0, order.getDiscountTotal().getAmount());
            assertEquals(10000, order.getTotal().getAmount());
        }

        @Nested
        @DisplayName("Shipping methods")
        class ShippingTests {

            @Test
            @DisplayName("standard shipping adds base cost ($5)")
            void calculateCost_standardShipping() {
                OrderItem item = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
                Order order = buildOrder(List.of(item), buildShippingDetail(ShippingMethodEnum.standard), null, null, null);
                order.calculateCost();

                assertEquals(499, order.getShippingTotal().getAmount());
                assertEquals(10499, order.getTotal().getAmount());
            }

            @Test
            @DisplayName("express shipping adds 50% over base cost")
            void calculateCost_expressShipping() {
                OrderItem item = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
                Order order = buildOrder(List.of(item), buildShippingDetail(ShippingMethodEnum.express), null, null, null);
                order.calculateCost();

                // express = round(500 / 100 * 1.5) * 100 = 800
                assertNotNull(order.getShippingTotal());
                assertEquals(748, order.getShippingTotal().getAmount());
                assertEquals(10748, order.getTotal().getAmount());
            }

            @Test
            @DisplayName("overnight shipping doubles base cost")
            void calculateCost_overnightShipping() {
                OrderItem item = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
                Order order = buildOrder(List.of(item), buildShippingDetail(ShippingMethodEnum.overnight), null, null, null);
                order.calculateCost();

                assertEquals(998, order.getShippingTotal().getAmount()); // 500 * 2
                assertEquals(10998, order.getTotal().getAmount());
            }

            @Test
            @DisplayName("pickup shipping is free")
            void calculateCost_pickupShipping() {
                OrderItem item = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
                Order order = buildOrder(List.of(item), buildShippingDetail(ShippingMethodEnum.pickup), null, null, null);
                order.calculateCost();

                assertEquals(0, order.getShippingTotal().getAmount());
                assertEquals(10000, order.getTotal().getAmount());
            }

            @Test
            @DisplayName("email shipping is free")
            void calculateCost_emailShipping() {
                OrderItem item = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
                Order order = buildOrder(List.of(item), buildShippingDetail(ShippingMethodEnum.email), null, null, null);
                order.calculateCost();

                assertEquals(0, order.getShippingTotal().getAmount());
                assertEquals(10000, order.getTotal().getAmount());
            }
        }

        @Nested
        @DisplayName("Discounts")
        class DiscountTests {

            @Test
            @DisplayName("rate-based discount reduces total correctly")
            void calculateCost_rateDiscount() {
                // subTotal = 10000, 10% discount = round(10000 * 10 / 100) * 100 = 1000
                OrderItem item = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
                DiscountDetail discount = new DiscountDetail("SAVE10", 10.0, null);
                Order order = buildOrder(List.of(item), null, discount, null, null);
                order.calculateCost();

                assertEquals(1000, order.getDiscountTotal().getAmount());
                assertEquals(9000, order.getTotal().getAmount());
            }

            @Test
            @DisplayName("fixed amount discount reduces total correctly")
            void calculateCost_fixedAmountDiscount() {
                OrderItem item = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
                DiscountDetail discount = new DiscountDetail("FLAT500", null, new Cost(500, cad));
                Order order = buildOrder(List.of(item), null, discount, null, null);
                order.calculateCost();

                assertEquals(500, order.getDiscountTotal().getAmount());
                assertEquals(9500, order.getTotal().getAmount());
            }
        }

        @Nested
        @DisplayName("Deposit")
        class DepositTests {

            @Test
            @DisplayName("deposit is subtracted from total")
            void calculateCost_depositReducesTotal() {
                OrderItem item = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
                Cost deposit = new Cost(2000, cad);
                Order order = buildOrder(List.of(item), null, null, null, deposit);
                order.calculateCost();

                assertEquals(8000, order.getTotal().getAmount());
            }
        }

        @Nested
        @DisplayName("Taxes")
        class TaxTests {

            @Test
            @DisplayName("excluded tax is added on top of total")
            void calculateCost_excludedTaxAddedToTotal() {
                // subTotal = 10000, 5% excluded tax = round(10000 * 0.05) = 500
                OrderItem item = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
                Tax tax = new Tax(null, null, "CRA", "CA", "123", 0.05, null, null, false);
                Order order = buildOrder(List.of(item), null, null, List.of(tax), null);
                order.calculateCost();

                assertEquals(500, tax.getTaxAmount().getAmount());
                assertEquals(10500, order.getTotal().getAmount());
            }

            @Test
            @DisplayName("included tax is extracted from subtotal")
            void calculateCost_includedTaxExtractedFromSubtotal() {
                // subTotal = 10000, 5% included tax = round(10000 * 0.05 / 1.05) = 476
                OrderItem item = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
                Tax tax = new Tax(null, null, "CRA", "CA", "123", 0.05, null, null, true);
                Order order = buildOrder(List.of(item), null, null, List.of(tax), null);
                order.calculateCost();

                assertEquals(476, tax.getTaxAmount().getAmount());
                assertEquals(10476, order.getTotal().getAmount());
            }

            @Test
            @DisplayName("multiple taxes are all added to total")
            void calculateCost_multipleTaxes() {
                // subTotal = 10000, GST 5% = 500, PST 9.975% = round(10000 * 0.09975) = 998
                OrderItem item = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
                Tax gst = new Tax(null, null, "CRA", "CA", "111", 0.05, null, null, false);
                Tax pst = new Tax(null, null, "MRQ", "QC", "222", 0.09975, null, null, false);
                Order order = buildOrder(List.of(item), null, null, List.of(gst, pst), null);
                order.calculateCost();

                assertEquals(500, gst.getTaxAmount().getAmount());
                assertEquals(998, pst.getTaxAmount().getAmount());
                assertEquals(11498, order.getTotal().getAmount());
            }
        }

        @Test
        @DisplayName("full order: items + standard shipping + rate discount + deposit + excluded tax")
        void calculateCost_fullOrder() {
            // item1 total=10000, item2 total=20000 → subTotal=30000
            OrderItem item1 = new OrderItem(null, new Cost(10000, cad), 1, new Cost(10000, cad));
            OrderItem item2 = new OrderItem(null, new Cost(10000, cad), 2, new Cost(20000, cad));
            // standard shipping = 500
            // 10% rate discount on 30000 = round(30000 * 10 / 100) * 100 = 3000
            // deposit = 2000
            // 5% excluded tax on 30000 = 1500
            // total = 30000 + 500 - 3000 - 2000 + 1500 = 27000
            Tax tax = new Tax(null, null, "CRA", "CA", "123", 0.05, null, null, false);
            DiscountDetail discount = new DiscountDetail("SAVE10", 10.0, null);
            Cost deposit = new Cost(2000, cad);

            Order order = buildOrder(List.of(item1, item2), buildShippingDetail(ShippingMethodEnum.standard), discount, List.of(tax), deposit);
            order.calculateCost();

            assertEquals(30000, order.getSubTotal().getAmount());
            assertEquals(499, order.getShippingTotal().getAmount());
            assertEquals(3000, order.getDiscountTotal().getAmount());
            assertEquals(26999, order.getTotal().getAmount());
        }
    }
}
