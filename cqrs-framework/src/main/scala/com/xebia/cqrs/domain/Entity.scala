package com.xebia.cqrs.domain

abstract class Entity[IdType](
        val id: IdType,
        protected val aggregate: Aggregate
        ) {
  this.aggregate.add(this);

  protected[domain] def apply(event: Event) {
    aggregate.apply(event);
  }

  protected[domain] def notify(notification: Notification) {
    aggregate.notify(notification);
  }

  protected[domain] def onEvent(event: Event);

}
