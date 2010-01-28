package com.xebia.lottery.ui;

import java.util._;

import org.apache.wicket.datetime._;
import org.apache.wicket.extensions.yui.calendar._;
import org.apache.wicket.markup.html.basic._;
import org.apache.wicket.markup.html.form._;
import org.apache.wicket.markup.html.link._;
import org.apache.wicket.markup.html.list._;
import org.apache.wicket.markup.html.panel._;
import org.apache.wicket.model._;
import org.apache.wicket.spring.injection.annot._;

import com.xebia.cqrs.bus._;
import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.queries._;
import com.xebia.lottery.shared._;
import com.xebia.lottery.commands._;

@SerialVersionUID(1L)
class LotteriesPage extends AbstractLotteryPage {

	private var lotteryQueryService : LotteryQueryService = Configuration.lotteryQueryService;

	add(new ListView[LotteryInfoQueryResult]("lotteries",
			this.lotteryQueryService.findUpcomingLotteries()) {
	override protected def populateItem(
				item : ListItem[LotteryInfoQueryResult]) {
			val info = item.getModelObject().lotteryInfo;
			item.add(new Label("name", info.name));
			item.add(new Label("drawingTimestamp", formatDate(info.drawingTimestamp)));
			item.add(new Label("prizeAmount", String.valueOf(info.prizeAmount)));
			item.add(new Label("ticketPrice", String.valueOf(info.ticketPrice)));
			item.add(new Link[Void]("drawLottery") {
				var bus : Bus = Configuration.inMemoryBus;

				override def onClick() {
					this.bus.sendAndWaitForResponse(new DrawLotteryCommand(
							item.getModelObject().lotteryId));
				}

			});
		}
	});
	add(new FeedbackPanel("feedback"));
	add(new CreateLotteryForm("createLotteryForm"));

 
	private def formatDate(drawingTimestamp : Date) = {
		new StyleDateConverter("MM", false).convertToString(
			drawingTimestamp, getLocale());
	}

	@SerialVersionUID(1L)
	private[LotteriesPage] class CreateLotteryForm(id : String) extends
			StatelessForm[CreateLotteryForm](id) {
		var bus : Bus = Configuration.inMemoryBus;

		var name : String = _;
		var drawingTimestamp : Date = _;
		var prizeAmount : Double = _;
		var ticketPrice : Double = _;

		setDefaultModel(new CompoundPropertyModel[CreateLotteryForm](this));
		add(new TextField[String]("name").setRequired(true));
		add(new DateTimeField("drawingTimestamp").setRequired(true));
		add(new TextField[Double]("prizeAmount").setRequired(true));
		add(new TextField[Double]("ticketPrice").setRequired(true));

		override protected def onSubmit() {
			val response = this.bus
				.sendAndWaitForResponse(new CreateLotteryCommand(
						VersionedId.random(), new LotteryInfo(this.name,
								this.drawingTimestamp, this.prizeAmount,
								this.ticketPrice)));
			System.err.println("response: " + response);
			setResponsePage(classOf[LotteriesPage]);
		}
	}
}
