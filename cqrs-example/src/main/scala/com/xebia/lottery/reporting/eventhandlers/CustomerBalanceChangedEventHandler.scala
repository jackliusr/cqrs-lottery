package com.xebia.lottery.reporting.eventhandlers;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.xebia.lottery.events.CustomerBalanceChangedEvent;

class CustomerBalanceChangedEventHandler(
	jdbcTemplate : SimpleJdbcTemplate
) extends PartialFunction[AnyRef, Unit] {
    def isDefinedAt(any : AnyRef) = any.isInstanceOf[CustomerBalanceChangedEvent]
    
    def apply(any : AnyRef) {
    	val message = any.asInstanceOf[CustomerBalanceChangedEvent]
        jdbcTemplate.update("update customer set version = ?, account_balance = ? where id = ?", 
                long2Long(message.aggregateRootId.version), 
                double2Double(message.newBalance), 
                message.customerId.id);
    }

}
