package com.xebia.cqrs.eventstore;

/**
 * Sink for change events. Every event stream has its own sequence of events
 * that are stored by the {@link EventStore}. When loading events the event
 * stream meta data and events are send to this {@link EventSink}.
 */
trait EventSink[EventType] {
  
    def setType(typee : String );

    def setVersion(version : Long);

    def setTimestamp(timestamp : Long);

    def setEvents(events : Seq[EventType]);

}
