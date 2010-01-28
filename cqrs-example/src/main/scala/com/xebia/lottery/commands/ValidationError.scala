package com.xebia.lottery.commands

import com.xebia.cqrs.domain.Notification;

case class ValidationError(
        errorMessage: String
        ) extends Notification {
  def getErrorMessage() = errorMessage

}
