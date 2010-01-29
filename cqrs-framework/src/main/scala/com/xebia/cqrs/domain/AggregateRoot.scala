package com.xebia.cqrs.domain;

import java.util.UUID;

abstract class AggregateRoot(
    versionedId : VersionedId
) extends Entity[UUID](versionedId.id, new Aggregate(versionedId)) {
    
    def loadFromHistory(events : Seq[_ <: Event] ) {
        aggregate.loadFromHistory(events);
    }

    def notifications = aggregate.notifications;

    def clearNotifications() {
        aggregate.clearNotifications();
    }

    def unsavedEvents = aggregate.unsavedEvents;

    def versionedId = aggregate.versionedId;

    def clearUnsavedEvents() {
        aggregate.clearUnsavedEvents();
    }

    def incrementVersion() {
        aggregate.incrementVersion();
    }
}
