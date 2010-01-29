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
				val aType : String,
				var version : Long,
				var timestamp : Long,
				val initialEvents : List[E]
		) {
			val events = new mutable.ArrayBuffer[VersionedEvent[E]]
	        addEvents(initialEvents);
	
	        private[InMemoryEventStore] def sendEventsToSink(
	          shouldEventBeSent : VersionedEvent[E] => Boolean, sink: EventSink[E]
	        ) {
	        	events
	              .takeWhile { shouldEventBeSent } match {
	                case list => 
	                  val lastEvent = list.lastOption.getOrElse({
	                	  throw new EmptyResultDataAccessException("no event found for specified version or timestamp", 1);
	                  })
	                  sink.setVersion(lastEvent.version);
	                  sink.setTimestamp(lastEvent.timestamp);
	                  sink.setEvents(list.map { _.event });
	            }
	        }
         
	        def setVersion(version : Long) {
	            if (this.version > version) {
	                throw new IllegalArgumentException("version cannot decrease");
	            }
	            this.version = version;
	        }
	        
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
        val version : Long,
        val timestamp : Long,
        val event : E
    )
    
    private[InMemoryEventStore] object VersionedEvent {
    	def atVersion[E](version : Long) = (event : VersionedEvent[E]) => event.version <= version
    	def atTimestamp[E](timestamp : Long) = (event : VersionedEvent[E]) => event.timestamp <= timestamp
    }
}

/**
 * Stores and tracks ordered streams of events.
 */
class InMemoryEventStore[E] extends EventStore[E] {
	import InMemoryEventStore.VersionedEvent
  
    val eventStreams = new mutable.HashMap[UUID, InMemoryEventStore.EventStream[E]]();
    
    def createEventStream(streamId: UUID, source: EventSource[E]) {
        if (eventStreams.contains(streamId)) {
            throw new DataIntegrityViolationException("stream already exists " + streamId);
        }
        eventStreams.put(
          streamId, 
          new InMemoryEventStore.EventStream[E](
            source.aType, 
            source.version, 
            source.timestamp, 
            source.events.toList));
    }
    
    def storeEventsIntoStream(streamId: UUID, expectedVersion: Long, source: EventSource[E]) {
        val stream = getStream(streamId);
        if (stream.version != expectedVersion) {
            throw new OptimisticLockingFailureException("stream " + streamId + ", actual version: " + stream.version + ", expected version: " + expectedVersion);
        }
        stream.setVersion(source.version);
        stream.setTimestamp(source.timestamp);
        stream.addEvents(source.events);
    }

    def loadEventsFromLatestStreamVersion(streamId: UUID, sink: EventSink[E]) {
        val stream = getStream(streamId);
        sink.setType(stream.aType);
        stream.sendEventsToSink(VersionedEvent.atVersion(stream.version), sink);
    }
    
    def loadEventsFromExpectedStreamVersion(streamId: UUID, expectedVersion: Long, sink: EventSink[E]) {
        val stream = getStream(streamId);
        if (stream.version != expectedVersion) {
            throw new OptimisticLockingFailureException("stream " + streamId + ", actual version: " + stream.version + ", expected version: " + expectedVersion);
        }
        sink.setType(stream.aType);
        stream.sendEventsToSink(VersionedEvent.atVersion(stream.version), sink);
    }
    
    def loadEventsFromStreamUptoVersion(streamId: UUID, version: Long, sink: EventSink[E]) {
        val stream = getStream(streamId);
        sink.setType(stream.aType);

        val actualVersion = Math.min(stream.version, version);
        stream.sendEventsToSink(VersionedEvent.atVersion(actualVersion), sink);
    }
    
    def loadEventsFromStreamUptoTimestamp(streamId: UUID, timestamp: Long, sink: EventSink[E]) {
        val stream = getStream(streamId);
        sink.setType(stream.aType);

        val actualTimestamp = Math.min(stream.timestamp, timestamp);
        stream.sendEventsToSink(VersionedEvent.atTimestamp(actualTimestamp), sink);
    }

    def getStream(streamId: UUID) : InMemoryEventStore.EventStream[E] = {
      eventStreams.get(streamId) match {
        case Some(stream) => stream
        case None => throw new EmptyResultDataAccessException("unknown event stream " + streamId, 1);
      }
    }
}
