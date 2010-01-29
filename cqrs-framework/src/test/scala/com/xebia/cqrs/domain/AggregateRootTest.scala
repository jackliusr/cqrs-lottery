package com.xebia.cqrs.domain;

import org.junit.Assert._;

import org.junit.Before;
import org.junit.Test;

object AggregateRootTest {
    val TEST_ID = VersionedId.random();  
}

class AggregateRootTest {
    var subject : FakeAggregateRoot = _;

    @Before
    def setUp() {
        subject = new FakeAggregateRoot();
        subject.greetPerson("Erik");
    }
    
    @Test
    def shouldDispatchAppliedEvents() {
        assertEquals("Hi Erik", subject.getLastGreeting());
    }
    
    @Test
    def shouldTrackUnsavedEvents() {
        assertEquals(new GreetingEvent(subject.versionedId, "Hi Erik"), subject.unsavedEvents.first);
    }
    
    @Test
    def shouldClearUnsavedChanges() {
        subject.clearUnsavedEvents();
        
        assertEquals(0, subject.unsavedEvents.size);
    }
    
}
