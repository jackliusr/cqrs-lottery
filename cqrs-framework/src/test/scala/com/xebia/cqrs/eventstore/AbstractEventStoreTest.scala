package com.xebia.cqrs.eventstore;

import org.junit.Assert._;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;

abstract class AbstractEventStoreTest {
    
    val ID_1 = UUID.randomUUID();
    val ID_2 = UUID.randomUUID();
    
    val T1 = 1000;
    val T2 = 2000;

    var subject : EventStore[String] = _;

    protected def createSubject() : EventStore[String];
    
    @Before
    def setUp() {
        subject = createSubject();
    }
    
    @Test
    def should_create_event_stream_with_initial_version_and_events() {
        val events = List("foo", "bar");
        val source = new FakeEventSource("type", 0, T1, events);
        val sink = new FakeEventSink("type", 0, T1, events);

        subject.createEventStream(ID_1, source);
        subject.loadEventsFromLatestStreamVersion(ID_1, sink);
        
        sink.verify();
    }
    
    @Test
    def should_fail_to_create_stream_with_duplicate_id() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 0, T1, List("foo", "bar")));
        try {
            subject.createEventStream(ID_1, new FakeEventSource("type", 0, T2, List("baz")));
            fail("DataIntegrityViolationException expected");
        } catch { 
          case expected : DataIntegrityViolationException => {}
        }
    }
    
    @Test
    def should_store_events_into_stream() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 0, T1, List("foo", "bar")));
        subject.storeEventsIntoStream(ID_1, 0, new FakeEventSource("type", 1, T2, List("baz")));
        
        val sink = new FakeEventSink("type", 1, T2, List("foo", "bar", "baz"));
        subject.loadEventsFromLatestStreamVersion(ID_1, sink);
        
        sink.verify();
    }
    
    @Test
    def should_load_events_from_specific_stream_version() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 0, T1, List("foo", "bar")));
        subject.storeEventsIntoStream(ID_1, 0, new FakeEventSource("type", 1, T2, List("baz")));
        
        val sink = new FakeEventSink("type", 1, T2, List("foo", "bar", "baz"));
        subject.loadEventsFromExpectedStreamVersion(ID_1, 1, sink);
        
        sink.verify();
    }
    
    @Test
    def should_fail_to_load_events_from_specific_stream_version_when_expected_version_does_not_match_actual_version() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 0, T1, List("foo", "bar")));
        subject.storeEventsIntoStream(ID_1, 0, new FakeEventSource("type", 1, T2, List("baz")));
        
        try {
            subject.loadEventsFromExpectedStreamVersion(ID_1, 0, new FakeEventSink("type", 1, T2, List("foo", "bar", "baz")));
            fail("OptimisticLockingFailureException expected");
        } catch {
          case expected :OptimisticLockingFailureException => {}
        }
    }
    
    @Test
    def should_store_separate_event_logs_for_different_event_streams() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 0, T1, List("foo", "bar")));
        subject.createEventStream(ID_2, new FakeEventSource("type", 0, T2, List("baz")));
        
        val sink1 = new FakeEventSink("type", 0, T1, List("foo", "bar"));
        subject.loadEventsFromExpectedStreamVersion(ID_1, 0, sink1);
        val sink2 = new FakeEventSink("type", 0, T2, List("baz"));
        subject.loadEventsFromExpectedStreamVersion(ID_2, 0, sink2);
        
        sink1.verify();
        sink2.verify();
    }
    
    @Test
    def should_fail_to_store_events_into_stream_when_versions_do_not_match() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 1, T1, List("foo", "bar")));
        try {
            subject.storeEventsIntoStream(ID_1, 0, new FakeEventSource("type", 1, T2, List("baz")));
            fail("OptimisticLockingFailureException expected");
        } catch {
          case expected :OptimisticLockingFailureException => {}
        }
    }
    
    @Test
    def should_check_optimistic_locking_error_before_decreasing_version_or_timestamp() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 5, T1, List("foo", "bar")));
        try {
            subject.storeEventsIntoStream(ID_1, 4, new FakeEventSource("type", 3, T2, List("baz")));
            fail("OptimisticLockingFailureException expected");
        } catch {
          case expected :OptimisticLockingFailureException => {}
        }
    }
    
    @Test
    def should_fail_to_store_events_into_stream_when_new_version_is_before_previous_version() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 5, T1, List("foo", "bar")));
        try {
            subject.storeEventsIntoStream(ID_1, 5, new FakeEventSource("type", 4, T2, List("baz")));
            fail("IllegalArgumentException expected");
        } catch {
          case expected : IllegalArgumentException => {}
        }
    }
    
    @Test
    def should_fail_to_store_events_into_stream_when_new_timestamp_is_before_previous_timestamp() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 0, T2, List("foo", "bar")));
        try {
            subject.storeEventsIntoStream(ID_1, 0, new FakeEventSource("type", 1, T1, List("baz")));
            fail("IllegalArgumentException expected");
        }  catch {
          case expected : IllegalArgumentException => {}
        }
    }

    @Test
    def should_fail_to_load_events_when_event_stream_version_does_not_match() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 0, T1, List("foo", "bar")));
        subject.storeEventsIntoStream(ID_1, 0, new FakeEventSource("type", 1, T2, List("baz")));
        
        try {
            subject.loadEventsFromExpectedStreamVersion(ID_1, 0, new FakeEventSink("type", 1, T2, List("foo", "bar", "baz")));
            fail("OptimisticLockingFailureException expected");
        } catch {
          case expected : OptimisticLockingFailureException => {}
        }
                
    }
    
    @Test
    def should_fail_to_store_events_into_non_existing_event_stream() {
        try {
            subject.storeEventsIntoStream(ID_1, 0, new FakeEventSource("type", 0, T1, List("foo")));
            fail("EmptyResultDataAccessException expected");
        } catch {
          case expected : EmptyResultDataAccessException => {}
        }
    }
    
    @Test
    def should_fail_to_load_events_from_non_existing_event_stream() {
        try {
            subject.loadEventsFromExpectedStreamVersion(ID_1, 0, new FakeEventSink("type", 0, T1, List("foo")));
            fail("EmptyResultDataAccessException expected");
        } catch {
          case expected : EmptyResultDataAccessException => { }
        }
    }
    
    @Test
    def should_load_events_from_stream_at_specific_version() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 0, T1, List("foo", "bar")));
        subject.storeEventsIntoStream(ID_1, 0, new FakeEventSource("type", 1, T2, List("baz")));
        
        val sink = new FakeEventSink("type", 0, T1, List("foo", "bar"));
        subject.loadEventsFromStreamUptoVersion(ID_1, 0, sink);
        
        sink.verify();
    }

    @Test
    def should_load_all_events_from_stream_when_specified_version_is_higher_than_actual_version() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 0, T1, List("foo", "bar")));
        subject.storeEventsIntoStream(ID_1, 0, new FakeEventSource("type", 1, T2, List("baz")));
        
        val sink = new FakeEventSink("type", 1, T2, List("foo", "bar", "baz"));
        subject.loadEventsFromStreamUptoVersion(ID_1, 3, sink);
        
        sink.verify();
    }
    
    @Test
    def should_fail_to_load_events_from_stream_when_requested_version_is_before_first_event_version() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 2, T1, List("foo", "bar")));
        try {
            subject.loadEventsFromStreamUptoVersion(ID_1, 1, new FakeEventSink("type", 0, T2, List("foo", "bar")));
            fail("EmptyResultDataAccessException expected");
        } catch {
          case expected : EmptyResultDataAccessException => {}
        }
    }
    
    @Test
    def should_load_events_from_stream_at_specific_timestamp() {
        val t = T1 + 250;
        
        subject.createEventStream(ID_1, new FakeEventSource("type", 0, T1, List("foo", "bar")));
        subject.storeEventsIntoStream(ID_1, 0, new FakeEventSource("type", 1, T2, List("baz")));
        
        val sink = new FakeEventSink("type", 0, T1, List("foo", "bar"));
        subject.loadEventsFromStreamUptoTimestamp(ID_1, t, sink);
        
        sink.verify();
    }
    
    @Test
    def should_fail_to_load_events_from_stream_when_request_timestamp_is_before_first_event_timestamp() {
        subject.createEventStream(ID_1, new FakeEventSource("type", 0, T2, List("foo", "bar")));
        try {
            subject.loadEventsFromStreamUptoTimestamp(ID_1, T1, new FakeEventSink("type", 0, T1, List("foo", "bar")));
            fail("EmptyResultDataAccessException expected");
        } catch {
          case expected : EmptyResultDataAccessException => {}
        }
    }
    
    class FakeEventSource(
    	val aType : String,
        val version : Long,
        val timestamp : Long,
        val events : List[String]
    ) extends EventSource[String]
    
    final class FakeEventSink(
        expectedType : String,
        expectedVersion : Long,
        expectedTimestamp : Long,
        expectedEvents : List[String]
    ) extends EventSink[String] {
        var actualType : String = _;
        var actualVersion = -1L;
        var actualTimestamp = -1L;
        var actualEvents : Seq[_ <: String] = List();

        def setType(actualType: String) {
            this.actualType = actualType;
        }

        def setVersion(actualVersion : Long) {
            assertNotNull("type must be set before version", actualType);
            this.actualVersion = actualVersion;
        }

        def setTimestamp(actualTimestamp : Long) {
            assertNotNull("type must be set before version", actualType);
            this.actualTimestamp = actualTimestamp;
        }
        
        def setEvents(actualEvents : Seq[String]) {
            assertNotNull("type must be set before events", actualType);
            this.actualEvents = actualEvents;
        }

        def verify() {
            assertEquals(expectedType, actualType);
            assertEquals(expectedVersion, actualVersion);
            assertEquals(expectedTimestamp, actualTimestamp);
            expectedEvents.zip(actualEvents.toList).foreach { 
              case (a, b) => assertEquals(a, b) }
        }
    }
}
