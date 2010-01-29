package com.xebia.cqrs.domain;

import collection._;

import org.apache.commons.lang.Validate;

class Aggregate(var versionedId : VersionedId) {
    var entitiesById = new mutable.HashMap[Any, Entity[_]]();
    
    var _unsavedEvents = new mutable.ArrayBuffer[Event]()
    def unsavedEvents = List(_unsavedEvents:_*)
    
    var _notifications = new mutable.ArrayBuffer[Notification]();
    def notifications = List(_notifications:_*);
    
    private[domain] def apply(event : Event) {
        entitiesById.get(event.entityId).foreach { entity =>
          entity.onEvent(event);
          _unsavedEvents += event; 
        }
    }

    private[domain] def notify(notification : Notification) {
        Validate.notNull(notification, "notification is required");
        _notifications += notification;
    }
    
    def add[T <: Any](entity : Entity[T]) {
        entitiesById + (entity.id -> entity);
    }
    
    def remove(entity : Entity[_]) {
        entitiesById -= entity.id;
    }
    
    def loadFromHistory(events : Seq[_ <: Event]) {
        events.foreach { event =>
            entitiesById
                    .get(event.entityId)
                    .foreach { entity => entity.onEvent(event) };
        }
    }
    
    def clearUnsavedEvents() {
        _unsavedEvents.clear();
    }
    
    def incrementVersion() {
        versionedId = versionedId.nextVersion();
    }

    
    def clearNotifications() {
        _notifications.clear();
    }
}
