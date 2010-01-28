package com.xebia.cqrs.eventstore;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * Stores and tracks ordered streams of events.
 */
trait EventStore[EventType] {

    /**
     * Creates a new event stream. The stream is initialized with the data and
     * events provided by source.
     * 
     * @param streamId
     *            the stream id of the stream to create.
     * @param source
     *            provides the type, initial version, initial timestamp, and
     *            initial events.
     * @throws DataIntegrityViolationException
     *             a stream with the specified id already exists.
     */
    @throws(classOf[DataIntegrityViolationException])
    def createEventStream(streamId : UUID, source : EventSource[EventType]);

    /**
     * Adds the events from source to the specified stream.
     * 
     * @param streamId
     *            the stream id.
     * @param expectedVersion
     *            the expected version of the stream.
     * @param source
     *            the stream data and events source.
     * @throws EmptyResultDataAccessException
     *             the specified stream does not exist.
     * @throws OptimisticLockingFailureException
     *             thrown when the expected version does not match the actual
     *             version of the stream.
     */
    @throws(classOf[EmptyResultDataAccessException])
    @throws(classOf[OptimisticLockingFailureException])
    def storeEventsIntoStream(streamId: UUID, expectedVersion: Long, source: EventSource[EventType])

    /**
     * Loads the events associated with the stream into the provided sink.
     * 
     * @param streamId
     *            the stream id
     * @param sink
     *            the sink to send the stream data and events to.
     * @throws EmptyResultDataAccessException
     *             no stream with the specified id exists.
     */
    @throws(classOf[EmptyResultDataAccessException])
    def loadEventsFromLatestStreamVersion(streamId: UUID, sink: EventSink[EventType]);

    /**
     * Loads the events associated with the stream into the provided sink.
     * 
     * @param streamId
     *            the stream id
     * @param expectedVersion
     *            the expected version of the stream.
     * @param sink
     *            the sink to send the stream data and events to.
     * @throws EmptyResultDataAccessException
     *             no stream with the specified id exists.
     * @throws OptimisticLockingFailureException
     *             thrown when the expected version does not match the actual
     *             version of the stream.
     */
    @throws(classOf[EmptyResultDataAccessException])
    @throws(classOf[OptimisticLockingFailureException])
    def loadEventsFromExpectedStreamVersion(streamId: UUID, expectedVersion: Long, sink: EventSink[EventType])

    /**
     * Loads the events associated with the stream into the provided sink. Only
     * the events that existed before and at the requested version are loaded.
     * 
     * @param streamId
     *            the stream id
     * @param version
     *            the version (inclusive) of the event stream to load.
     * @param sink
     *            the sink to send the stream data and events to.
     * @throws EmptyResultDataAccessException
     *             no stream with the specified id exists or the version is
     *             lower than the initial version of the stream.
     */
    @throws(classOf[EmptyResultDataAccessException])
    def loadEventsFromStreamUptoVersion(streamId: UUID, version: Long, sink: EventSink[EventType]);

    /**
     * Loads the events associated with the stream into the provided sink. Only
     * the events that existed before and at the requested timestamp are loaded.
     * 
     * @param streamId
     *            the stream id
     * @param timestamp
     *            the timestamp (inclusive) of the event stream to load.
     * @param sink
     *            the sink to send the stream data and events to.
     * @throws EmptyResultDataAccessException
     *             no stream with the specified id exists or the version is
     *             lower than the initial version of the stream.
     */
    def loadEventsFromStreamUptoTimestamp(streamId: UUID, timestamp: Long, sink: EventSink[EventType]);

}
