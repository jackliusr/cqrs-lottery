package com.xebia.lottery.events;

import com.xebia.cqrs.domain.Event;
import com.xebia.cqrs.domain.VersionedId;

@SerialVersionUID(1L)
abstract class LotteryEvent(
	lotteryId : VersionedId,
	entityId : Object
) extends Event(lotteryId, entityId) {
  
    def getLotteryId() = getAggregateRootId();
}
