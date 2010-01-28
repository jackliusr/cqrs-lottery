package com.xebia.lottery.domain.commandhandlers;

import com.xebia.cqrs.domain.Repository;
import com.xebia.lottery.commands.PurchaseTicketCommand;
import com.xebia.lottery.domain.aggregates.Customer;
import com.xebia.lottery.domain.aggregates.Lottery;

class PurchaseLotteryTicketCommandHandler(
	repository : Repository
) extends PartialFunction[AnyRef, Unit] {
    def isDefinedAt(any : AnyRef) = any.isInstanceOf[PurchaseTicketCommand]
    
    def apply(any : AnyRef) {
    	val command = any.asInstanceOf[PurchaseTicketCommand]
        val lottery = repository.getByVersionedId(classOf[Lottery], command.lotteryId);
        val customer = repository.getByVersionedId(classOf[Customer], command.customerId);
        lottery.purchaseTicketForCustomer(customer);
    }

}
