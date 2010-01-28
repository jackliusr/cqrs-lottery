package com.xebia.lottery.events;

import com.xebia.cqrs.domain.VersionedId;

@SerialVersionUID(1L)
class LotteryTicketPurchasedEvent(
	lotteryId : VersionedId, 
	val customerId : VersionedId, 
	val ticketNumber : String
) extends LotteryEvent(lotteryId, lotteryId.getId())