package com.xebia.lottery.domain.aggregates;

import scala.collection._;

import org.apache.commons.lang.StringUtils;

import com.xebia.cqrs.bus.Bus;
import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.commands.ValidationError;
import com.xebia.lottery.shared.CustomerInfo;

class CustomerFactory(
	bus : Bus
) {
	implicit def boolean2Option(b : Boolean) = new {
	   def causes[T](result : T) = if (b) { Some(result) } else None
    }
    
    def create(customerId: VersionedId, info: CustomerInfo, initialAccountBalance: Double) = {       
        ((StringUtils.isBlank(info.name) causes error("customer name is required")) :: 
        ((initialAccountBalance < 10.0) causes error("minimum account balance is 10.00")) :: 
         List[ValidationError]())
          .filter { _ != None } match {
            case Nil => Some(new Customer(customerId, info, initialAccountBalance));
            case errors => bus.reply(errors); None
        }
    }
       
   def error(msg : String) = new ValidationError(msg)
}
