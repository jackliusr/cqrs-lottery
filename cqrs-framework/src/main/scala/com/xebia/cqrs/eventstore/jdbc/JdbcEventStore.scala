package com.xebia.cqrs.eventstore.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.xebia.cqrs.eventstore.EventSerializer;
import com.xebia.cqrs.eventstore.EventSink;
import com.xebia.cqrs.eventstore.EventSource;
import com.xebia.cqrs.eventstore.EventStore;

object JdbcEventStore {
    val LOG = Logger.getLogger(classOf[JdbcEventStore[_]]);
    
    class EventStream(
        id: UUID,
        typee : String,
        version: Long,
        timestamp: Long,
        nextEventSequence: Int
    ) {
        def getId() = id

        def getType() = typee

        def getVersion() = version

        def getTimestamp() = timestamp
        
        def getNextEventSequence() = nextEventSequence
    }
    
    class StoredEvent[E](
          version : Long,
          timestamp : Long,
          event : E
    ) {    
        def getVersion() = version

        def getTimestamp() = timestamp

        def getEvent() = event
    }
}

class JdbcEventStore[E](
	jdbcTemplate : SimpleJdbcTemplate,
	eventSerializer : EventSerializer[E]
) extends EventStore[E] {
	import JdbcEventStore._
 
    try {
        jdbcTemplate.update("drop table event if exists");
        jdbcTemplate.update("drop table event_stream if exists");
        jdbcTemplate.update("create table event_stream(id varchar primary key, type varchar not null, version bigint not null, timestamp timestamp not null, next_event_sequence bigint not null)");
        jdbcTemplate.update("create table event(event_stream_id varchar not null, sequence_number bigint not null, version bigint not null, timestamp timestamp not null, data varchar not null, " 
                + "primary key (event_stream_id, sequence_number), foreign key (event_stream_id) references event_stream (id))");
    } catch { 
      case ex : DataAccessException => LOG.info("init database exception", ex);
    }

    @throws(classOf[DataIntegrityViolationException]) 
    def createEventStream(streamId: UUID, source: EventSource[E]) {
        val version = source.getVersion();
        val timestamp = source.getTimestamp();
        val events = source.getEvents();
        val v = 
        jdbcTemplate.update(
        		"insert into event_stream (id, type, version, timestamp, next_event_sequence) values (?, ?, ?, ?, ?)",
                streamId.toString(), 
                source.getType(),
                long2Long(version),
                new Date(timestamp),
                int2Integer(events.size));
        saveEvents(streamId, version, timestamp, 0, events);
    }
    
    def storeEventsIntoStream(streamId: UUID , expectedVersion: Long, source: EventSource[E]) {
        val version = source.getVersion();
        val timestamp = source.getTimestamp();
        val events = source.getEvents();
        
        val stream = getEventStream(streamId);
        val count = jdbcTemplate.update("update event_stream set version = ?, timestamp = ?, next_event_sequence = ? where id = ? and version = ?",
                long2Long(version),
                new Date(timestamp),
                (stream.getNextEventSequence() + events.size).toString,
                streamId.toString(), 
                long2Long(expectedVersion));
        if (count != 1) {
            throw new OptimisticLockingFailureException("id: " + streamId + "; actual: " + stream.getVersion() + "; expected: " + expectedVersion);
        }
        if (version < stream.getVersion()) {
            throw new IllegalArgumentException("version cannot decrease");
        }
        if (timestamp < stream.getTimestamp()) {
            throw new IllegalArgumentException("timestamp cannot decrease");
        }
        
        saveEvents(streamId, version, timestamp, stream.getNextEventSequence(), events);
    }

    def loadEventsFromLatestStreamVersion(streamId : UUID, sink: EventSink[E]) {
        val stream = getEventStream(streamId);
        val storedEvents = loadEventsUptoVersion(stream, stream.getVersion());

        sendEventsToSink(stream, storedEvents, sink);
    }

    def loadEventsFromExpectedStreamVersion(streamId: UUID, expectedVersion: Long, sink: EventSink[E]) {
        val stream = getEventStream(streamId);
        if (stream.getVersion() != expectedVersion) {
            throw new OptimisticLockingFailureException("id: " + streamId + "; actual: " + stream.getVersion() + "; expected: " + expectedVersion);
        }
        val storedEvents = loadEventsUptoVersion(stream, stream.getVersion());

        sendEventsToSink(stream, storedEvents, sink);
    }

    def loadEventsFromStreamUptoVersion(streamId: UUID, version: Long, sink: EventSink[E]) {
        val stream = getEventStream(streamId);
        val storedEvents = loadEventsUptoVersion(stream, version);

        sendEventsToSink(stream, storedEvents, sink);
    }

    def loadEventsFromStreamUptoTimestamp(streamId: UUID, timestamp: Long, sink: EventSink[E]) {
        val stream = getEventStream(streamId);
        val storedEvents = loadEventsUptoTimestamp(stream, timestamp);

        sendEventsToSink(stream, storedEvents, sink);
    }

    private def saveEvents(streamId: UUID, version: Long, timestamp: Long, nextEventSequence: Int, events: Seq[E]) {
    	var nextEventId_v = nextEventSequence;
    	def nextEventId = {nextEventId_v += 1; nextEventId_v }
        events.foreach { event =>
            jdbcTemplate.update("insert into event(event_stream_id, sequence_number, version, timestamp, data) values (?, ?, ?, ?, ?)",
                    streamId.toString(), 
                    nextEventId.toString,
                    long2Long(version),
                    new Date(timestamp),
                    eventSerializer.serialize(event));
        }
    }

    private def getEventStream(streamId: UUID) = {
        jdbcTemplate.queryForObject(
        	"select type, version, timestamp, next_event_sequence from event_stream where id = ?", 
            new EventStreamRowMapper(streamId), 
            streamId.toString());
    }

    private def loadEventsUptoVersion(stream: EventStream, version: Long) = {
        jdbcTemplate.query(
            "select version, timestamp, data from event where event_stream_id = ? and version <= ? order by sequence_number", 
            new StoredEventRowMapper(),
            stream.getId().toString(), 
            long2Long(version)
        ) match {
              case storedEvents if storedEvents.isEmpty() =>
                throw new EmptyResultDataAccessException("no events found for stream " + stream.getId() + " for version " + version, 1);
              case storedEvents => 
                List[StoredEvent[E]](storedEvents.toArray(new Array[StoredEvent[E]](0)): _*)
        }
    }

    private def loadEventsUptoTimestamp(stream: EventStream, timestamp: Long) = {      
        jdbcTemplate.query(
            "select version, timestamp, data from event where event_stream_id = ? and timestamp <= ? order by sequence_number", 
            new StoredEventRowMapper(),
            stream.getId().toString(), 
            new Date(timestamp)
        ) match {
                case storedEvents if storedEvents.isEmpty =>
                  throw new EmptyResultDataAccessException("no events found for stream " + stream.getId() + " for timestamp " + timestamp, 1);
                case storedEvents => 
                  List[StoredEvent[E]](storedEvents.toArray(new Array[StoredEvent[E]](0)): _*)
        }
    }

    private def sendEventsToSink(stream: EventStream, storedEvents: List[StoredEvent[E]], sink: EventSink[E]) {
        sink.setType(stream.getType());
        sink.setVersion(storedEvents.last.getVersion());
        sink.setTimestamp(storedEvents.last.getTimestamp());
        sink.setEvents(storedEvents.map { _.getEvent() });
    }

    private final class EventStreamRowMapper private[JdbcEventStore](
      streamId : UUID
      ) extends ParameterizedRowMapper[EventStream] {
    	@throws(classOf[SQLException]) 
        def mapRow(rs: ResultSet, rowNum: Int) = {
            new EventStream(
            	streamId,
                rs.getString("type"),
                rs.getLong("version"),
                rs.getTimestamp("timestamp").getTime(),
                rs.getInt("next_event_sequence"));
        }
    }

    private final class StoredEventRowMapper extends ParameterizedRowMapper[StoredEvent[E]] {
        @throws(classOf[SQLException]) 
    	def mapRow(rs: ResultSet, rowNum: Int) = {
        	new StoredEvent[E](
                rs.getLong("version"),
                rs.getTimestamp("timestamp").getTime(),
                eventSerializer.deserialize(rs.getString("data")));
        }
    }
}
