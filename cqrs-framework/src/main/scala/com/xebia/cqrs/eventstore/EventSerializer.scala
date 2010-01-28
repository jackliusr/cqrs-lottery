package com.xebia.cqrs.eventstore;


/**
 * Responsible for (de-)serializing events.
 */
trait EventSerializer[E] {

    def serialize(event : E) : AnyRef;
    
    def deserialize(serialized : AnyRef) : E;
    
}
