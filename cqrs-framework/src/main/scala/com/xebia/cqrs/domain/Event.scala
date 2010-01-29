package com.xebia.cqrs.domain

import org.apache.commons.lang.Validate;

abstract case class Event(
        val aggregateRootId: VersionedId,
        val entityId: Object
) {
  Validate.notNull(aggregateRootId, "aggregateRootId is required");
}
