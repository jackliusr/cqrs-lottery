package com.xebia.lottery.shared;

import com.xebia.cqrs.domain._;

@SerialVersionUID(1L)
case class CustomerInfo(
	val name : String,
	val email : String,
	val address : Address
) extends ValueObject