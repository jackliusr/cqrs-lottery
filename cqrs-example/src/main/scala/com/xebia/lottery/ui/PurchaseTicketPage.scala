package com.xebia.lottery.ui;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

import com.xebia.cqrs.bus.Bus;
import com.xebia.cqrs.bus.Response;
import com.xebia.lottery.commands.PurchaseTicketCommand;
import com.xebia.lottery.commands.ValidationError;
import com.xebia.lottery.queries.CustomerAccountQueryResult;
import com.xebia.lottery.queries.LotteryInfoQueryResult;
import com.xebia.lottery.queries.LotteryQueryService;

class PurchaseTicketPage extends AbstractLotteryPage {
    add(new FeedbackPanel("feedback"));
    add(new PurchaseTicketForm("purchaseTicketForm"));

    @SerialVersionUID(1L)
    private[PurchaseTicketPage] class PurchaseTicketForm(
    	id : String
    ) extends StatelessForm[PurchaseTicketForm](id) {
        
        var bus : Bus = Configuration.inMemoryBus;
        var lotteryQueryService : LotteryQueryService = Configuration.lotteryQueryService;

        var selectedLottery : LotteryInfoQueryResult = _;
        var selectedCustomer : CustomerAccountQueryResult = _;

        setDefaultModel(new CompoundPropertyModel[PurchaseTicketForm](this));
        add(new DropDownChoice[LotteryInfoQueryResult](
                "selectedLottery", 
                lotteryQueryService.findUpcomingLotteries(), 
                new ChoiceRenderer[LotteryInfoQueryResult]("lotteryInfo.name")).setNullValid(false).setRequired(true));
        add(new DropDownChoice[CustomerAccountQueryResult](
                "selectedCustomer", 
                lotteryQueryService.findCustomers(), 
                new ChoiceRenderer[CustomerAccountQueryResult]("customerName")).setNullValid(false).setRequired(true));
        
        override protected def onSubmit() {
            val response = bus.sendAndWaitForResponse(new PurchaseTicketCommand(selectedLottery.lotteryId, selectedCustomer.customerId));
            response
              .getRepliesOfType(classOf[ValidationError])
              .foreach { validationError => error(validationError.errorMessage) }
            if (!hasError()) {
                setResponsePage(classOf[CustomerTicketsPage], CustomerTicketsPage.link(selectedCustomer.customerId.id));
            }
        }

    }

}
