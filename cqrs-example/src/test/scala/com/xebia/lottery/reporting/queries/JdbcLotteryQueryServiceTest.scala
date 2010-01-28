package com.xebia.lottery.reporting.queries;

import org.junit.Assert._;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.domain.aggregates.WhenCustomerIsCreated;
import com.xebia.lottery.events.CustomerBalanceChangedEvent;
import com.xebia.lottery.events.CustomerCreatedEvent;
import com.xebia.lottery.events.LotteryCreatedEvent;
import com.xebia.lottery.queries.CustomerAccountQueryResult;
import com.xebia.lottery.queries.LotteryInfoQueryResult;
import com.xebia.lottery.reporting.eventhandlers.CustomerBalanceChangedEventHandler;
import com.xebia.lottery.reporting.eventhandlers.CustomerCreatedEventHandler;
import com.xebia.lottery.reporting.eventhandlers.LotteryCreatedEventHandler;
import com.xebia.lottery.shared.LotteryInfo;

object JdbcLotteryQueryServiceTest {
    val LOTTERY_ID = VersionedId.random();
    val LOTTERY_INFO = new LotteryInfo("lottery", new Date(System.currentTimeMillis() + 100000), 1000.0, 15);
}

@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration { val locations= Array("/test-application-context.xml") }
class JdbcLotteryQueryServiceTest {
	import JdbcLotteryQueryServiceTest._
  
    var lotteryQueryService : JdbcLotteryQueryService = _;
    
    @Autowired { val required = true }
    def setLotteryQueryService(lotteryQueryService : JdbcLotteryQueryService) {
    	this.lotteryQueryService = lotteryQueryService
    }
    
    var lotteryCreatedEventHandler : LotteryCreatedEventHandler = _;
    
    @Autowired { val required = true }
    def setLotteryCreatedEventHandler(lotteryCreatedEventHandler : LotteryCreatedEventHandler) {
    	this.lotteryCreatedEventHandler = lotteryCreatedEventHandler
    }
    
    var customerCreatedEventHandler : CustomerCreatedEventHandler = _;
    
    @Autowired { val required = true }
    def setCustomerCreatedEventHandler(customerCreatedEventHandler : CustomerCreatedEventHandler) {
    	this.customerCreatedEventHandler = customerCreatedEventHandler
    }
    
    var customerBalanceChangedEventHandler : CustomerBalanceChangedEventHandler = _;
    
    @Autowired { val required = true }
    def setCustomerBalanceChangedEventHandler(customerBalanceChangedEventHandler : CustomerBalanceChangedEventHandler) {
    	this.customerBalanceChangedEventHandler = customerBalanceChangedEventHandler
    }
    
    @Test
    def findUpcomingLotteries() {
        assertTrue(lotteryQueryService.findUpcomingLotteries().isEmpty());
        
        lotteryCreatedEventHandler.apply(new LotteryCreatedEvent(LOTTERY_ID, LOTTERY_INFO));
        
        val upcomingLotteries = lotteryQueryService.findUpcomingLotteries();
        assertEquals(1, upcomingLotteries.size());
        val lottery = upcomingLotteries.get(0);
        assertTrue(new LotteryInfoQueryResult(LOTTERY_ID, LOTTERY_INFO) == lottery);
    }

    @Test
    def findCustomers() {
        assertTrue(lotteryQueryService.findCustomers().isEmpty());

        customerCreatedEventHandler.apply(new CustomerCreatedEvent(WhenCustomerIsCreated.CUSTOMER_ID, WhenCustomerIsCreated.CUSTOMER_INFO));
        customerBalanceChangedEventHandler.apply(new CustomerBalanceChangedEvent(WhenCustomerIsCreated.CUSTOMER_ID, 0.0, 50.0, 50.0));

        val customers = lotteryQueryService.findCustomers();
        assertEquals(1, customers.size());
        val customer = customers.get(0);
        assertEquals(WhenCustomerIsCreated.CUSTOMER_ID, customer.customerId);
        assertEquals(WhenCustomerIsCreated.CUSTOMER_INFO.name, customer.customerName);
        assertEquals(50.0, customer.currentBalance, 0.0);
    }
    
}
