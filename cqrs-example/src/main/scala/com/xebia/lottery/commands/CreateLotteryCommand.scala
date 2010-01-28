package com.xebia.lottery.commands

import org.apache.commons.lang.Validate;

import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.shared.LotteryInfo;

case class CreateLotteryCommand(
        lotteryId: VersionedId,
        lotteryInfo: LotteryInfo
) extends LotteryCommand(lotteryId) {
  Validate.notNull(lotteryInfo, "lotteryInfo is required");

  def getInfo() = lotteryInfo;
}