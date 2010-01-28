package com.xebia.lottery.domain.commandhandlers;

import com.xebia.cqrs.domain.Repository;
import com.xebia.lottery.commands.CreateLotteryCommand;
import com.xebia.lottery.domain.aggregates.Lottery;

class CreateLotteryCommandHandler(
	repository : Repository
) extends PartialFunction[AnyRef, Unit] {
    def isDefinedAt(any : AnyRef) = any.isInstanceOf[CreateLotteryCommand]
    
    def apply(any : AnyRef) {
    	val message = any.asInstanceOf[CreateLotteryCommand]
        val lottery = new Lottery(message.getLotteryId(), message.getInfo());
        repository.add(lottery);
    }
}
