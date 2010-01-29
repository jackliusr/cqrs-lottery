package com.xebia.lottery.reporting.eventhandlers;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.xebia.lottery.events.CustomerCreatedEvent;
import com.xebia.lottery.shared.CustomerInfo;

class CustomerCreatedEventHandler(
	jdbcTemplate : SimpleJdbcTemplate
) extends PartialFunction[AnyRef, Unit] {
	def isDefinedAt(any : AnyRef) = any.isInstanceOf[CustomerCreatedEvent]
    
    def apply(any : AnyRef) {
    	val message = any.asInstanceOf[CustomerCreatedEvent]
        val info = message.info;
        jdbcTemplate.update("insert into customer(id, version, name, account_balance, email, street_name, house_number, postal_code, city, country) values (?, ?, ?, 0, ?, ?, ?, ?, ?, ?)", 
                message.customerId.id, 
                long2Long(message.customerId.version), 
                info.name, 
                info.email, 
                info.address.streetName,
                info.address.houseNumber,
                info.address.postalCode,
                info.address.city,
                info.address.country);
    }

}
