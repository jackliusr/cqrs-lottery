package com.xebia.lottery.commands

import org.apache.commons.lang.Validate;

import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.shared.CustomerInfo;

case class CreateCustomerCommand(
        val customerId: VersionedId,
        val info: CustomerInfo,
        val initialAccountBalance: Double
) extends Command {
  Validate.notNull(customerId, "customerId is required");
  Validate.notNull(info, "info is required");
}
