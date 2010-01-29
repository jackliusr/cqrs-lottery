package com.xebia.lottery.domain.aggregates

import java.util.UUID;

import com.xebia.cqrs.domain.Aggregate;
import com.xebia.cqrs.domain.Entity;
import com.xebia.cqrs.domain.Event;
import com.xebia.lottery.events.LotteryTicketPrizeAwardedEvent;

@SerialVersionUID(1L)
class LotteryTicket(
	aggregate : Aggregate,
	number : String,
	customerId : UUID
) extends Entity[String](number, aggregate) {
    var prizeAmount : Double = _;

    def win(prizeAmount: Double) {
        apply(new LotteryTicketPrizeAwardedEvent(aggregate.versionedId, number, customerId, prizeAmount));
    }

    def onEvent(event: Event) {
      event match {
        case event : LotteryTicketPrizeAwardedEvent => onLotteryTicketPrizeAwarded(event)
        case _ => throw new IllegalArgumentException("unrecognized event: " + event)
      }
    }

    private[LotteryTicket] def onLotteryTicketPrizeAwarded(event: LotteryTicketPrizeAwardedEvent) {
        this.prizeAmount = event.prizeAmount;
    }
}
