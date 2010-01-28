package com.xebia.lottery.shared;

import com.xebia.cqrs.domain._;

@SerialVersionUID(1L)
case class LotteryInfo(
	val name : String,
	val drawingTimestamp : java.util.Date,
	val prizeAmount : Double,
	val ticketPrice : Double
) extends ValueObject