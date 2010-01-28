package com.xebia.cqrs.domain;

import java.util.UUID;

abstract class AggregateRoot(
    versionedId : VersionedId
) extends Entity[UUID](versionedId.getId(), new Aggregate(versionedId)) {
    
    def loadFromHistory(events : Seq[_ <: Event] ) {
        aggregate.loadFromHistory(events);
    }

    def getNotifications() = aggregate.getNotifications();

    def clearNotifications() {
        aggregate.clearNotifications();
    }

    def getUnsavedEvents() = aggregate.getUnsavedEvents();

    def getVersionedId() = aggregate.getVersionedId();

    def clearUnsavedEvents() {
        aggregate.clearUnsavedEvents();
    }

    def incrementVersion() {
        aggregate.incrementVersion();
    }
}
