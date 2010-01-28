package com.xebia.lottery.commands

import org.apache.commons.lang.Validate;

import com.xebia.cqrs.domain.VersionedId;
import com.xebia.lottery.shared.CustomerInfo;

case class CreateCustomerCommand(
        customerId: VersionedId,
        info: CustomerInfo,
        initialAccountBalance: Double
) extends Command {
  Validate.notNull(customerId, "customerId is required");
  Validate.notNull(info, "info is required");

  def getCustomerId() = customerId;

  def getInfo() = info;

  def getInitialAccountBalance() = initialAccountBalance
}
