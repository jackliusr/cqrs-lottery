package com.xebia.lottery.shared;

import com.xebia.cqrs.domain.ValueObject;

@SerialVersionUID(1L)
case class Address(
	streetName : String,
	houseNumber : String,
	postalCode : String,
    city : String, 
    country : String
) extends ValueObject