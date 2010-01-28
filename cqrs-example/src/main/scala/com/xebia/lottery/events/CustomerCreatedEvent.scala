package com.xebia.lottery.events;

import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.shared.CustomerInfo;

@SerialVersionUID(1L)
class CustomerCreatedEvent(
  val customerId : VersionedId, 
  val info : CustomerInfo
) extends CustomerEvent(customerId)