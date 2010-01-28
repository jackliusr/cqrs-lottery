package com.xebia.lottery.reporting.eventhandlers;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.xebia.lottery.events.LotteryTicketPurchasedEvent;

class LotteryTicketPurchasedEventHandler(
	jdbcTemplate : SimpleJdbcTemplate
) extends PartialFunction[AnyRef, Unit] {
	def isDefinedAt(any : AnyRef) = any.isInstanceOf[LotteryTicketPurchasedEvent]
    
    def apply(any : AnyRef) {
    	val message = any.asInstanceOf[LotteryTicketPurchasedEvent]
        jdbcTemplate.update("insert into ticket (number, lottery_id, customer_id) values (?, ?, ?)", 
                message.ticketNumber, 
                message.getAggregateRootId().getId(), 
                message.customerId.getId());
        jdbcTemplate.update("update lottery set version = ? where id = ?", 
                long2Long(message.getAggregateRootId().getVersion()), 
                message.getAggregateRootId().getId());
    }

}
