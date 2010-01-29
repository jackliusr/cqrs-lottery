package com.xebia.lottery.domain.eventstore;

import org.junit.Assert._;

import org.junit.Test;

import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.domain.aggregates.WhenCustomerIsCreated;
import com.xebia.lottery.domain.aggregates.WhenLotteryIsCreated;
import com.xebia.lottery.events.LotteryTicketPurchasedEvent;

object XStreamEventSerializerTest {
    val LOTTERY_ID = WhenLotteryIsCreated.LOTTERY_ID;
    val CUSTOMER_ID = WhenCustomerIsCreated.CUSTOMER_ID;

    val EVENT = new LotteryTicketPurchasedEvent(LOTTERY_ID, CUSTOMER_ID, "7122");
    val SERIALIZED_EVENT = "<event.LotteryTicketPurchasedEvent>\n" +
    				"  <entityId class=\"uuid\">" + LOTTERY_ID.id + "</entityId>\n" + 
                    "  <aggregateRootId>\n" +
                    "    <version>" + LOTTERY_ID.version + "</version>\n" +
                    "    <id>" + LOTTERY_ID.id + "</id>\n" +
                    "  </aggregateRootId>\n" +
                    "  <ticketNumber>7122</ticketNumber>\n" +
                    "  <customerId>\n" +
                    "    <version>" + CUSTOMER_ID.version + "</version>\n" +
                    "    <id>" + CUSTOMER_ID.id + "</id>\n" +
                    "  </customerId>\n" +
                    "</event.LotteryTicketPurchasedEvent>";  
}

class XStreamEventSerializerTest {
	import XStreamEventSerializerTest._
 
    val subject = new XStreamEventSerializer();
    
    @Test
    def shouldSerializeEvent() {
        assertEquals(SERIALIZED_EVENT, subject.serialize(EVENT));
    }

    @Test
    def shouldDeserializeEvent() {
        assertEquals(EVENT, subject.deserialize(SERIALIZED_EVENT));
    }

}
