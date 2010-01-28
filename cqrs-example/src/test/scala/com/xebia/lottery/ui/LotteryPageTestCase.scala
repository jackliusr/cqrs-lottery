package com.xebia.lottery.ui;

import org.easymock.EasyMock;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;

import com.xebia.cqrs.bus.Bus;
import com.xebia.lottery.queries.LotteryQueryService;

abstract class LotteryPageTestCase {

    protected var context : ApplicationContextMock = _;
    protected var tester : WicketTester = _;
    protected var bus : Bus = _;
    protected var lotteryQueryService : LotteryQueryService = _;

    @Before
    def setUpLotteryPageTestCase() {
        bus = EasyMock.createNiceMock(classOf[Bus]);
        lotteryQueryService = EasyMock.createNiceMock(classOf[LotteryQueryService]);
        
        context = new ApplicationContextMock();
        context.putBean("bus", bus);
        context.putBean("lotteryQueryService", lotteryQueryService);
        tester = new WicketTester(new WicketApplication(context));
    }
    
    protected def replayMocks() {
        EasyMock.replay(bus, lotteryQueryService);
    }
    
    protected def verifyMocks() {
        EasyMock.verify(bus, lotteryQueryService);
    }

}
