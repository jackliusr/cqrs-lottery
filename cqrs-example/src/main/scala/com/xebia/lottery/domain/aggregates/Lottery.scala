package com.xebia.lottery.domain.aggregates;

import java.util.Random;

import scala.collection._;

import com.xebia.cqrs.domain.AggregateRoot;
import com.xebia.cqrs.domain.Event;
import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.commands.ValidationError;
import com.xebia.lottery.events.LotteryCreatedEvent;
import com.xebia.lottery.events.LotteryTicketPurchasedEvent;
import com.xebia.lottery.shared.LotteryInfo;

object Lottery {
    val RANDOM = new Random(42);
    val tickets = new mutable.HashSet[LotteryTicket]();
}

class Lottery(
	id : VersionedId
) extends AggregateRoot(id) {
	import Lottery._
  
    var ticketPrice : Double = _;
    var prizeAmount : Double = _;

    def this(id: VersionedId, info: LotteryInfo) {
        this(id);
        apply(new LotteryCreatedEvent(id,  info));
    }

    def purchaseTicketForCustomer(customer: Customer) {
        if (!customer.isBalanceSufficient(this.ticketPrice)) {
            notify(new ValidationError("insufficient account balance to purchase ticket"));
            return;
        }
        
        customer.deductBalance(this.ticketPrice);
        apply(new LotteryTicketPurchasedEvent(
          aggregate.getVersionedId(), 
          customer.getVersionedId(), 
          generateTicketNumber()));
    }

    def draw() {
        val winningTicket = tickets.toList.first
        winningTicket.win(prizeAmount);
    }
    
    def generateTicketNumber() = String.format("%06d", int2Integer(RANDOM.nextInt(1000000)));

    def onEvent(event: Event) {
        if (event.isInstanceOf[LotteryCreatedEvent]) {
            onLotteryCreatedEvent(event.asInstanceOf[LotteryCreatedEvent]);
        } else if (event.isInstanceOf[LotteryTicketPurchasedEvent]) {
            onTicketPurchasedEvent(event.asInstanceOf[LotteryTicketPurchasedEvent]);
        } else {
            throw new IllegalArgumentException("unrecognized event: " + event);
        }
    }

    private[Lottery] def onLotteryCreatedEvent(event: LotteryCreatedEvent) {
        this.ticketPrice = event.info.ticketPrice;
        this.prizeAmount = event.info.prizeAmount;
    }

    private[Lottery] def onTicketPurchasedEvent(event: LotteryTicketPurchasedEvent) {
        tickets + new LotteryTicket(aggregate, event.ticketNumber, event.customerId.getId());
    }

}
