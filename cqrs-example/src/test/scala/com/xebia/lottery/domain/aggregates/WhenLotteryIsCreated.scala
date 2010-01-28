package com.xebia.lottery.domain.aggregates;

import java.util.Date;

import org.junit.Test;

import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.events.LotteryCreatedEvent;
import com.xebia.lottery.shared.LotteryInfo;

object WhenLotteryIsCreated {
	val LOTTERY_ID = VersionedId.random();
    val LOTTER_INFO = new LotteryInfo("lottery", new Date(System.currentTimeMillis() + 100000), 1000.00, 15);
}

class WhenLotteryIsCreated extends BddTestCase {
	import WhenLotteryIsCreated._
  
    var subject : Lottery = _;
    
    override protected def when() {
        subject = new Lottery(LOTTERY_ID, LOTTER_INFO);
    }
    
    @Test
    def shouldRaiseLotteryCreatedEvent() {
        BddTestCase.assertChange(subject, new LotteryCreatedEvent(LOTTERY_ID, LOTTER_INFO));
    }
}
