package com.xebia.cqrs.eventstore;

/**
 * Source for change events. Every event source has its own sequence of events
 * that are stored by the {@link EventStore}. In DDD your aggregates are the
 * event sources.
 */
trait EventSource[EventType] {
    def aType : String

    def version : Long
    
    def timestamp : Long

    def events : Seq[EventType]
}
