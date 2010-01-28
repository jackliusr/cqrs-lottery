package com.xebia.lottery.domain.aggregates;

import org.junit.Test;

import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.events.CustomerBalanceChangedEvent;
import com.xebia.lottery.events.CustomerCreatedEvent;
import com.xebia.lottery.events.LotteryCreatedEvent;
import com.xebia.lottery.events.LotteryTicketPurchasedEvent;
import com.xebia.lottery.shared.Address;
import com.xebia.lottery.shared.CustomerInfo;
import com.xebia.lottery.shared.LotteryInfo;

object GivenUpcomingLotteryWhenCustomerPurchasesLotteryTicket {
    val CUSTOMER_ID = WhenCustomerIsCreated.CUSTOMER_ID;
    val CUSTOMER_INFO = new CustomerInfo("Jan Jansen", "jan@jansen.nl", new Address("Plantage Middenlaan", "20", "1018 DE", "Amsterdam", "Nederland"));
    
    val LOTTERY_ID = WhenLotteryIsCreated.LOTTERY_ID;
    val LOTTER_INFO = WhenLotteryIsCreated.LOTTER_INFO;
}

class GivenUpcomingLotteryWhenCustomerPurchasesLotteryTicket extends BddTestCase {
	import GivenUpcomingLotteryWhenCustomerPurchasesLotteryTicket._
  
    var customer : Customer = _;
    var lottery : Lottery = _;
    
    override protected def given() {
        customer = new Customer(CUSTOMER_ID);
        customer.loadFromHistory(List(
                new CustomerCreatedEvent(CUSTOMER_ID, CUSTOMER_INFO), 
                new CustomerBalanceChangedEvent(CUSTOMER_ID, 0, 50, 50)));
        
        lottery = new Lottery(LOTTERY_ID);
        lottery.loadFromHistory(List(new LotteryCreatedEvent(LOTTERY_ID, LOTTER_INFO)));
    }
    
    override protected def when() {
        lottery.purchaseTicketForCustomer(customer);
    }
    
    @Test
    def shouldRaiseTicketPurchasedEvent() {
        BddTestCase.assertChange(
                lottery, 
                new LotteryTicketPurchasedEvent(LOTTERY_ID, CUSTOMER_ID, "431130"));
    }
    
    @Test
    def shouldDeductTicketPriceFromCustomerBalance() {
        BddTestCase.assertChange(
                customer,
                new CustomerBalanceChangedEvent(CUSTOMER_ID, 50, -15, 35));
    }    
}
