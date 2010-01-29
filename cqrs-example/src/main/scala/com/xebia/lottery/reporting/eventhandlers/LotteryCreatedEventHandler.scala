package com.xebia.lottery.reporting.eventhandlers;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.xebia.lottery.events.LotteryCreatedEvent;
import com.xebia.lottery.shared.LotteryInfo;

class LotteryCreatedEventHandler(
	jdbcTemplate : SimpleJdbcTemplate
) extends PartialFunction[AnyRef, Unit] {
	def isDefinedAt(any : AnyRef) = any.isInstanceOf[LotteryCreatedEvent]
    
    def apply(any : AnyRef) {
    	val message = any.asInstanceOf[LotteryCreatedEvent]
        val info = message.info;
        jdbcTemplate.update("insert into lottery(id, version, name, drawing_timestamp, prize_amount, ticket_price) values (?, ?, ?, ?, ?, ?)", 
                message.aggregateRootId.id, 
                long2Long(message.aggregateRootId.version), 
                info.name, 
                info.drawingTimestamp, 
                double2Double(info.prizeAmount),
                double2Double(info.ticketPrice));
    }

}
