package com.xebia.lottery.domain.aggregates;

import org.apache.commons.lang.Validate;

import com.xebia.cqrs.domain.AggregateRoot;
import com.xebia.cqrs.domain.Event;
import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.events.CustomerBalanceChangedEvent;
import com.xebia.lottery.events.CustomerCreatedEvent;
import com.xebia.lottery.shared.CustomerInfo;

class Customer(
  id : VersionedId
  ) extends AggregateRoot(id) {

    var accountBalance : Double = _;
    
    def this(customerId: VersionedId, customerInfo: CustomerInfo, initialAccountBalance: Double) {
        this(customerId);
        apply(new CustomerCreatedEvent(customerId, customerInfo));
        apply(new CustomerBalanceChangedEvent(customerId, 0.0, initialAccountBalance, initialAccountBalance));
    }
    
    def isBalanceSufficient(amount : Double) = this.accountBalance >= amount;

    def deductBalance(amount : Double) {
        Validate.isTrue(isBalanceSufficient(amount), "insufficient balance");
        apply(new CustomerBalanceChangedEvent(versionedId, this.accountBalance, -amount, this.accountBalance - amount));
    }

    override def onEvent(event : Event) {
    	event match {
    	  case event : CustomerCreatedEvent => onCustomerCreatedEvent(event);
    	  case event : CustomerBalanceChangedEvent => onCustomerBalanceChangedEvent(event);
    	  case _ => throw new IllegalArgumentException("unrecognized event: " + event);
    	}
    }

    private def onCustomerCreatedEvent(event: CustomerCreatedEvent) {
    }

    private def onCustomerBalanceChangedEvent(event: CustomerBalanceChangedEvent) {
        this.accountBalance = event.newBalance;
    }

}
