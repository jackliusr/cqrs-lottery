package com.xebia.lottery.domain.commandhandlers;

import com.xebia.cqrs.domain.Repository;
import com.xebia.lottery.commands.DrawLotteryCommand;
import com.xebia.lottery.domain.aggregates.Lottery;

class DrawLotteryCommandHandler(
	repository : Repository
) extends PartialFunction[AnyRef, Unit]{
    def isDefinedAt(any : AnyRef) = any.isInstanceOf[DrawLotteryCommand]
    
    def apply(any : AnyRef) {
    	val message = any.asInstanceOf[DrawLotteryCommand]
        val lottery = repository.getByVersionedId(classOf[Lottery], message.lotteryId);
        lottery.draw();
    }

}
