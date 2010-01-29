package com.xebia.cqrs.domain

import java.util.UUID;

@SerialVersionUID(1L)
class AggregateRootNotFoundException(
        val aggregateRootType: String,
        val aggregateRootId: UUID
) extends RuntimeException("aggregate root " + aggregateRootType + " with id " + aggregateRootId)