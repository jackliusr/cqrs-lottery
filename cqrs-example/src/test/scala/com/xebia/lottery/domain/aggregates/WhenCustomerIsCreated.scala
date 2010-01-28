package com.xebia.lottery.domain.aggregates;

import org.junit.Test;

import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.events.CustomerBalanceChangedEvent;
import com.xebia.lottery.events.CustomerCreatedEvent;
import com.xebia.lottery.shared.Address;
import com.xebia.lottery.shared.CustomerInfo;

object WhenCustomerIsCreated {
    val CUSTOMER_ID = VersionedId.random();
    val CUSTOMER_INFO = new CustomerInfo("Jan Jansen", "jan@jansen.nl", new Address("Plantage Middenlaan", "20", "1018 DE", "Amsterdam", "Nederland"));
}

class WhenCustomerIsCreated extends BddTestCase  {
	import WhenCustomerIsCreated._
  
    var subject: Customer = _;
    
    override protected def when() {
        subject = new Customer(CUSTOMER_ID, CUSTOMER_INFO, 10.0);
    }

    @Test
    def shouldRaiseCustomerCreatedEvent() {
        BddTestCase.assertChange(subject, new CustomerCreatedEvent(CUSTOMER_ID, CUSTOMER_INFO));
    }
    
    @Test
    def shouldGiveCustomerInitialBalance() {
        BddTestCase.assertChange(subject, new CustomerBalanceChangedEvent(CUSTOMER_ID, 0,10.0, 10.0));
    }
    
}
