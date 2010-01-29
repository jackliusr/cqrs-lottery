package com.xebia.lottery.ui;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

import com.xebia.cqrs.bus._;
import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.commands.CreateCustomerCommand;
import com.xebia.lottery.commands.ValidationError;
import com.xebia.lottery.queries.CustomerAccountQueryResult;
import com.xebia.lottery.queries.LotteryQueryService;
import com.xebia.lottery.shared.Address;
import com.xebia.lottery.shared.CustomerInfo;

class CustomersPage extends AbstractLotteryPage {

    var lotteryQueryService : LotteryQueryService = Configuration.lotteryQueryService;

    add(new ListView[CustomerAccountQueryResult]("customers", lotteryQueryService.findCustomers()) {
          
            override protected def populateItem(item : ListItem[CustomerAccountQueryResult]) {
                item.add(new Label("name", item.getModelObject().customerName));
                item.add(new Label("accountBalance", String.valueOf(item.getModelObject().currentBalance)));
                item.add(new BookmarkablePageLink[CustomerTicketsPage](
                  "tickets", 
                  classOf[CustomerTicketsPage], 
                  CustomerTicketsPage.link(item.getModelObject().customerId.id)));
            }
        });
    add(new FeedbackPanel("feedback"));
    add(new CreateCustomerForm("createCustomerForm"));
    
    @SerialVersionUID(1L)
    private[CustomersPage] class CreateCustomerForm(
    	id : String
    ) extends StatelessForm[CreateCustomerForm](id) {
        
        var bus : Bus = Configuration.inMemoryBus;
        
        var name : String = _;
        var email : String = _;
        var streetName : String = _;
        var houseNumber : String = _;
        var postalCode : String = _;
        var city : String = _;
        var initialAccountBalance : Double = _;
        
        setDefaultModel(new CompoundPropertyModel[CreateCustomerForm](this));
        add(new TextField[String]("name").setRequired(true));
        add(new TextField[String]("email").setRequired(true));
        add(new TextField[String]("streetName").setRequired(true));
        add(new TextField[String]("houseNumber").setRequired(true));
        add(new TextField[String]("postalCode").setRequired(true));
        add(new TextField[String]("city").setRequired(true));
        add(new TextField[Double]("initialAccountBalance").setRequired(true));
        
        override protected def onSubmit() {
            val address = new Address(streetName, houseNumber, postalCode, city, "Nederland");
            val info = new CustomerInfo(name, email, address);
            val response = bus.sendAndWaitForResponse(new CreateCustomerCommand(VersionedId.random(), info, initialAccountBalance));
            System.err.println("response: " + response);
            response
              .getRepliesOfType(classOf[ValidationError])
              .foreach { validationError => error(validationError.errorMessage) }
            
            if (!hasError()) {
                setResponsePage(classOf[CustomersPage]);
            }
        }

    }

}
