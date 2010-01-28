package com.xebia.lottery.domain.commandhandlers;

import com.xebia.cqrs.domain.Repository;
import com.xebia.lottery.commands.CreateCustomerCommand;
import com.xebia.lottery.domain.aggregates.Customer;
import com.xebia.lottery.domain.aggregates.CustomerFactory;

class CreateCustomerCommandHandler(
	repository : Repository,
	customerFactory : CustomerFactory
) extends PartialFunction[AnyRef, Unit] {
    def isDefinedAt(any : AnyRef) = any.isInstanceOf[CreateCustomerCommand]
    
    def apply(any : AnyRef) {
    	val message = any.asInstanceOf[CreateCustomerCommand]
        val customer = 
          customerFactory.create(
            message.getCustomerId(), 
            message.getInfo(), 
            message.getInitialAccountBalance());
        repository.add(customer.getOrElse{ error("Unable to create customer") });
    }
}
