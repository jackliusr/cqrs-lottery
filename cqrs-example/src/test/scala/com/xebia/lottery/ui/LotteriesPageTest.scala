package com.xebia.lottery.ui;

import org.easymock.EasyMock;

import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;

import com.xebia.cqrs.bus.Response;
import com.xebia.lottery.commands.CreateLotteryCommand;

/**
 * Simple test using the WicketTester
 */
class LotteriesPageTest extends LotteryPageTestCase {

    @Test
    def testRenderMyPage() {
        EasyMock
          .expect(bus.sendAndWaitForResponse(EasyMock.isA(classOf[CreateLotteryCommand])))
          .andReturn(new Response(List()));
        replayMocks();
        
        // start and render the test page
        tester.startPage(classOf[LotteriesPage]);

        // assert rendered page class
        tester.assertRenderedPage(classOf[LotteriesPage]);

        val form = tester.newFormTester("createLotteryForm");
        form.setValue("name", "lottery");
        form.setValue("drawingTimestamp:date", "02.12.09"); // US format :(
        form.setValue("drawingTimestamp:hours", "11");
        form.setValue("drawingTimestamp:minutes", "44");
        form.setValue("prizeAmount", "1000,00");
        form.setValue("ticketPrice", "1,50");
        form.submit();
        
        tester.assertNoErrorMessage();
        verifyMocks();
    }

}
