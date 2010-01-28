package com.xebia.cqrs.eventstore.inmemory

import java.util.UUID;

import collection._;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;

import com.xebia.cqrs.eventstore.EventSink;
import com.xebia.cqrs.eventstore.EventSource;
import com.xebia.cqrs.eventstore.EventStore;

object InMemoryEventStore {
		class EventStream[E](
				private val typee : String,
				private var version : Long,
				private var timestamp : Long,
				private val initialEvents : List[E]
		) {
			val events = new mutable.ArrayBuffer[VersionedEvent[E]]
	        addEvents(initialEvents);
	
	        def sendEventsAtVersionToSink(version : Long, sink : EventSink[E]) {
	            events
	              .takeWhile { _.getVersion() <= version } match {
	                case list => 
	                  sendEventsToSink(list.map { _.getEvent() }, list.lastOption.getOrElse(null), sink)
	            }
	        }
	
	        def sendEventsAtTimestampToSink(timestamp : Long, sink : EventSink[E]) {
	          events
	              .takeWhile { _.getTimestamp() <= timestamp } match {
	                case list => 
	                  sendEventsToSink(list.map { _.getEvent() }, list.lastOption.getOrElse(null), sink)
	            }
	        }
	
	        private def sendEventsToSink(events : Seq[E], lastEvent: VersionedEvent[E], sink: EventSink[E]) {
	            if (lastEvent == null) {
	                throw new EmptyResultDataAccessException("no event found for specified version or timestamp", 1);
	            }
	            sink.setVersion(lastEvent.getVersion());
	            sink.setTimestamp(lastEvent.getTimestamp());
	            sink.setEvents(events);
	        }
	
	        def getType() = typee
	        
	        def getVersion() = version
	
	        def setVersion(version : Long) {
	            if (this.version > version) {
	                throw new IllegalArgumentException("version cannot decrease");
	            }
	            this.version = version;
	        }
	        
	        def getTimestamp() = timestamp 
	        
	        def setTimestamp(timestamp : Long) {
	            if (this.timestamp > timestamp) {
	                throw new IllegalArgumentException("timestamp cannot decrease");
	            }
	            this.timestamp = timestamp;
	        }
	
	        def addEvents(eventsToAdd: Seq[E] ) {
	        	this.events ++= eventsToAdd.map { 
	        	  event => new VersionedEvent[E](this.version, this.timestamp, event) 
	        	}
	        }
	}
  
      private[InMemoryEventStore] class VersionedEvent[E](
          version : Long,
        timestamp : Long,
        event : E
    ) {
        def getVersion() = version
        
        def getTimestamp() = timestamp
        
        def getEvent() = event
    }
}

/**
 * Stores and tracks ordered streams of events.
 */
class InMemoryEventStore[E] extends EventStore[E] {
    
    val eventStreams = new mutable.HashMap[UUID, InMemoryEventStore.EventStream[E]]();
    
    def createEventStream(streamId: UUID, source: EventSource[E]) {
        if (eventStreams.contains(streamId)) {
            throw new DataIntegrityViolationException("stream already exists " + streamId);
        }
        eventStreams.put(
          streamId, 
          new InMemoryEventStore.EventStream[E](
            source.getType(), 
            source.getVersion(), 
            source.getTimestamp(), 
            source.getEvents().toList));
    }
    
    def storeEventsIntoStream(streamId: UUID, expectedVersion: Long, source: EventSource[E]) {
        val stream = getStream(streamId);
        if (stream.getVersion() != expectedVersion) {
            throw new OptimisticLockingFailureException("stream " + streamId + ", actual version: " + stream.getVersion() + ", expected version: " + expectedVersion);
        }
        stream.setVersion(source.getVersion());
        stream.setTimestamp(source.getTimestamp());
        stream.addEvents(source.getEvents());
    }

    def loadEventsFromLatestStreamVersion(streamId: UUID, sink: EventSink[E]) {
        val stream = getStream(streamId);
        sink.setType(stream.getType());
        stream.sendEventsAtVersionToSink(stream.getVersion(), sink);
    }
    
    def loadEventsFromExpectedStreamVersion(streamId: UUID, expectedVersion: Long, sink: EventSink[E]) {
        val stream = getStream(streamId);
        if (stream.getVersion() != expectedVersion) {
            throw new OptimisticLockingFailureException("stream " + streamId + ", actual version: " + stream.getVersion() + ", expected version: " + expectedVersion);
        }
        sink.setType(stream.getType());
        stream.sendEventsAtVersionToSink(stream.getVersion(), sink);
    }
    
    def loadEventsFromStreamUptoVersion(streamId: UUID, version: Long, sink: EventSink[E]) {
        val stream = getStream(streamId);
        sink.setType(stream.getType());

        val actualVersion = Math.min(stream.getVersion(), version);
        stream.sendEventsAtVersionToSink(actualVersion, sink);
    }
    
    def loadEventsFromStreamUptoTimestamp(streamId: UUID, timestamp: Long, sink: EventSink[E]) {
        val stream = getStream(streamId);
        sink.setType(stream.getType());

        val actualTimestamp = Math.min(stream.getTimestamp(), timestamp);
        stream.sendEventsAtTimestampToSink(actualTimestamp, sink);
    }

    def getStream(streamId: UUID) : InMemoryEventStore.EventStream[E] = {
      eventStreams.get(streamId) match {
        case Some(stream) => stream
        case None => throw new EmptyResultDataAccessException("unknown event stream " + streamId, 1);
      }
    }
}
