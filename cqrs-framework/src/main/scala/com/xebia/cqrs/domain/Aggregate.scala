package com.xebia.cqrs.domain;

import collection._;

import org.apache.commons.lang.Validate;

class Aggregate(var versionedId : VersionedId) {

    var entitiesById = new mutable.HashMap[Any, Entity[_]]();
    var unsavedEvents = new mutable.ArrayBuffer[Event]()
    var notifications = new mutable.ArrayBuffer[Notification]();

    private[domain] def apply(event : Event) {
        entitiesById.get(event.getEntityId()).foreach { entity =>
          entity.onEvent(event);
          unsavedEvents += event; 
        }
    }

    private[domain] def notify(notification : Notification) {
        Validate.notNull(notification, "notification is required");
        notifications += notification;
    }
    
    def add[T <: Any](entity : Entity[T]) {
        entitiesById + (entity.getId() -> entity);
    }
    
    def remove(entity : Entity[_]) {
        entitiesById -= entity.getId();
    }
    
    def getVersionedId() = versionedId;
    
    def loadFromHistory(events : Seq[_ <: Event]) {
        events.foreach { event =>
            entitiesById
                    .get(event.getEntityId())
                    .foreach { entity => entity.onEvent(event) };
        }
    }

    def getUnsavedEvents() = List(unsavedEvents:_*)
    
    def clearUnsavedEvents() {
        unsavedEvents.clear();
    }
    
    def incrementVersion() {
        versionedId = versionedId.nextVersion();
    }

    def getNotifications() = List(notifications:_*);
    
    def clearNotifications() {
        notifications.clear();
    }
}
