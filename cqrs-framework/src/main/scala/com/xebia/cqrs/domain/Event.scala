package com.xebia.cqrs.domain

import org.apache.commons.lang.Validate;

abstract case class Event(
        aggregateRootId: VersionedId,
        entityId: Object
) {
  Validate.notNull(aggregateRootId, "aggregateRootId is required");

  def getAggregateRootId() = aggregateRootId;

  def getEntityId() = entityId;
}
