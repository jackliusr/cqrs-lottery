package com.xebia.lottery.events;

import org.apache.commons.lang.Validate;

import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.shared.LotteryInfo;

@SerialVersionUID(1L)
class LotteryCreatedEvent(
  lotteryId : VersionedId, 
  val info : LotteryInfo
) extends LotteryEvent(lotteryId, lotteryId.id) {
    Validate.notNull(info, "info is required");
}