package com.xebia.cqrs.domain;


case class GreetingEvent(
  override val aggregateRootId: VersionedId, 
  val message: String) extends Event(aggregateRootId, aggregateRootId.id)
