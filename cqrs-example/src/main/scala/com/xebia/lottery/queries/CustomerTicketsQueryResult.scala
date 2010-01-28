package com.xebia.lottery.queries;

import com.xebia.cqrs.domain.ValueObject;

@SerialVersionUID(1L)
case class CustomerTicketsQueryResult(
    ticketNumber : String,
    lotteryName : String,
    customerName : String
) extends ValueObject