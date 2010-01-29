package com.xebia.lottery.commands;

import org.apache.commons.lang.Validate;

import com.xebia.cqrs.domain.VersionedId;

abstract class LotteryCommand(lotteryId : VersionedId) extends Command {
    Validate.notNull(lotteryId, "lotteryId is required");
}
