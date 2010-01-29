package com.xebia.lottery.commands

import com.xebia.cqrs.domain.VersionedId;

case class PurchaseTicketCommand(
        val lotteryId: VersionedId,
        val customerId: VersionedId
) extends Command