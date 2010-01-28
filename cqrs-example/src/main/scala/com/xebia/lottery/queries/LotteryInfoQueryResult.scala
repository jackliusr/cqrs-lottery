package com.xebia.lottery.queries;

import com.xebia.cqrs.domain.ValueObject;
import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.shared.LotteryInfo;

@SerialVersionUID(1L)
case class LotteryInfoQueryResult(
    val lotteryId : VersionedId,
    val lotteryInfo : LotteryInfo	
) extends ValueObject