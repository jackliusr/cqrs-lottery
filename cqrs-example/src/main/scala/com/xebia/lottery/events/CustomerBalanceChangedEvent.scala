package com.xebia.lottery.events;

import com.xebia.cqrs.domain.VersionedId;

@SerialVersionUID(1L)
class CustomerBalanceChangedEvent(
	val customerId : VersionedId,
	val oldBalance : Double, 
	val amountChanged : Double, 
	val newBalance : Double
) extends CustomerEvent(customerId)