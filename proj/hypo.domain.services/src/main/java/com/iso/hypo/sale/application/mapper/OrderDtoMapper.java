package com.iso.hypo.sale.application.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.common.domain.model.location.Address;
import com.iso.hypo.finance.domain.model.Tax;
import com.iso.hypo.sale.application.dto.OrderDto;
import com.iso.hypo.sale.domain.model.BillingDetail;
import com.iso.hypo.sale.domain.model.DiscountDetail;
import com.iso.hypo.sale.domain.model.Order;
import com.iso.hypo.sale.domain.model.OrderItem;
import com.iso.hypo.sale.domain.model.PaymentDetail;
import com.iso.hypo.sale.domain.model.ShippingDetail;

@Component
public class OrderDtoMapper {

	private final ModelMapper modelMapper;

	public OrderDtoMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public <D> D map(Object source, Class<D> destinationType) {
		if (source == null) return null;
		return modelMapper.map(source, destinationType);
	}

	public OrderDto toDto(Order entity) {
		return map(entity, OrderDto.class);	
	}

	public Order toEntity(OrderDto dto) {
		return map(dto, Order.class);
	}
	
	public ModelMapper initOrderMappings(ModelMapper mapper) {
		PropertyMap<Order, Order> orderPropertyMap = new PropertyMap<Order, Order>() {
			@Override
	        protected void configure()
	        {
				skip().setId(null);
				skip().setUuid(null);
				skip().setCreatedOn(null);
				skip().setCreatedBy(null);
				skip().setModifiedOn(null);
				skip().setModifiedBy(null);
				skip().setDeleted(false);
				skip().setDeletedOn(null);
				skip().setDeletedBy(null);
				skip().setActive(false);
				skip().setActivatedOn(null);
				skip().setDeactivatedOn(null);
				skip().setActivatedBy(null);
				skip().setDeactivatedBy(null);
	        }
		};
		
		PropertyMap<BillingDetail, BillingDetail> billingDetailPropertyMap = new PropertyMap<BillingDetail, BillingDetail>() {
			@Override
			protected void configure() {
			}
		};
		
		PropertyMap<OrderItem, OrderItem> orderItemPropertyMap = new PropertyMap<OrderItem, OrderItem>() {
			@Override
			protected void configure() {
			}
		};
		
		PropertyMap<ShippingDetail, ShippingDetail> shippingDetailPropertyMap = new PropertyMap<ShippingDetail, ShippingDetail>() {
			@Override
			protected void configure() {
			}
		};
		
		PropertyMap<DiscountDetail, DiscountDetail> discountDetailPropertyMap = new PropertyMap<DiscountDetail, DiscountDetail>() {
			@Override
			protected void configure() {
			}
		};
		
		PropertyMap<PaymentDetail, PaymentDetail> paymentDetailPropertyMap = new PropertyMap<PaymentDetail, PaymentDetail>() {
			@Override
			protected void configure() {
			}
		};
		
	    PropertyMap<Cost, Cost> costPropertyMap = new PropertyMap<Cost, Cost>() {
			@Override
			protected void configure() {
			}
		};
		
	    PropertyMap<Tax, Tax> taxPropertyMap = new PropertyMap<Tax, Tax>() {
				@Override
				protected void configure() {
				}
		};
		
		PropertyMap<Address, Address> addressPropertyMap = new PropertyMap<Address, Address>() {
			@Override
			protected void configure() {
			}
		};
		
		mapper.addMappings(orderPropertyMap);
		mapper.addMappings(billingDetailPropertyMap);
		mapper.addMappings(orderItemPropertyMap);
		mapper.addMappings(shippingDetailPropertyMap);
		mapper.addMappings(discountDetailPropertyMap);
		mapper.addMappings(paymentDetailPropertyMap);
		mapper.addMappings(costPropertyMap);
		mapper.addMappings(taxPropertyMap);
		mapper.addMappings(addressPropertyMap);
		
		return mapper;
	}
}
