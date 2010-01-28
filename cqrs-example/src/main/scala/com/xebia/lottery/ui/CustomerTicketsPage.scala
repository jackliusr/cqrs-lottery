package com.xebia.lottery.ui;

import java.util.UUID;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import com.xebia.lottery.queries.CustomerTicketsQueryResult;
import com.xebia.lottery.queries.LotteryQueryService;

object CustomerTicketsPage {
    def link(customerId : UUID) = {
        val parameters = new PageParameters()
        parameters.add("id", String.valueOf(customerId));
        parameters;
    }
}

class CustomerTicketsPage(
	parameters : PageParameters
) extends AbstractLotteryPage {

    var lotteryQueryService : LotteryQueryService = Configuration.lotteryQueryService;
    
    val customerId = UUID.fromString(parameters.getString("id"));
    add(new Label("customerName", lotteryQueryService.getCustomerName(customerId)));
    add(new ListView[CustomerTicketsQueryResult]("tickets", lotteryQueryService.findLotteryTicketsForCustomer(customerId)) {
        override protected def populateItem(item : ListItem[CustomerTicketsQueryResult]) {
            item.add(new Label("number", item.getModelObject().ticketNumber));
            item.add(new Label("lotteryName", item.getModelObject().lotteryName));
        }
    });
}
