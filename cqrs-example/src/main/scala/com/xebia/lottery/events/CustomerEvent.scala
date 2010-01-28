package com.xebia.lottery.events;

import com.xebia.cqrs.domain.Event;
import com.xebia.cqrs.domain.VersionedId;

@SerialVersionUID(1L)
abstract class CustomerEvent(
	customerId : VersionedId
) extends Event(customerId, customerId.getId())
