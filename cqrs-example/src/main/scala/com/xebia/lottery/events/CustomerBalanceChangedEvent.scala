package com.xebia.lottery.events;

import com.xebia.cqrs.domain.VersionedId;

@SerialVersionUID(1L)
class CustomerBalanceChangedEvent(
	val customerId : VersionedId,
	oldBalance : Double, 
	amountChanged : Double, 
	newBalance : Double
) extends CustomerEvent(customerId) {
    def getOldBalance() = oldBalance

    def getAmountChanged() = amountChanged

    def getNewBalance() = newBalance
}
