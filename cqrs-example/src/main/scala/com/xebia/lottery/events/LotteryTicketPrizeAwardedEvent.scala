package com.xebia.lottery.events;

import java.util.UUID;

import com.xebia.cqrs.domain.VersionedId;

class LotteryTicketPrizeAwardedEvent(
	versionedId : VersionedId,
	number : String, 
	customerId : UUID, 
	val prizeAmount : Double
) extends LotteryEvent(versionedId, number)
