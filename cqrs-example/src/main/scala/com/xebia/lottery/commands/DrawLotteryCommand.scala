package com.xebia.lottery.commands

import com.xebia.cqrs.domain.VersionedId;

case class DrawLotteryCommand(lotteryId: VersionedId) extends LotteryCommand(lotteryId)