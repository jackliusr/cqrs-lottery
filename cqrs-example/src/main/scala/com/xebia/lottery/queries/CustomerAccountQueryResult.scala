package com.xebia.lottery.queries;

import com.xebia.cqrs.domain.ValueObject;
import com.xebia.cqrs.domain.VersionedId;

@SerialVersionUID(1L)
case class CustomerAccountQueryResult(
	val customerId : VersionedId,
	val customerName : String,
	val currentBalance : Double
) extends ValueObject