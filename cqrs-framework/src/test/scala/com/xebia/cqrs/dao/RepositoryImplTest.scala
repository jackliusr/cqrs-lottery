package com.xebia.cqrs.dao;

import org.mockito._;
import org.junit.Assert._;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import com.xebia.cqrs.bus.Bus;
import com.xebia.cqrs.domain._;
import com.xebia.cqrs.eventstore.EventStore;
import com.xebia.cqrs.eventstore.inmemory.InMemoryEventStore;


class RepositoryImplTest {

    val TEST_ID = VersionedId.random();
    
    var bus : Bus = _;
    var aggregateRoot : FakeAggregateRoot = _;
    var eventStore : EventStore[Event] = _;
    var subject : RepositoryImpl = _;
    
    @Before
    def setUp() {
        eventStore = new InMemoryEventStore[Event]();
        bus = Mockito.mock(classOf[Bus]);
        subject = new RepositoryImpl(eventStore, bus)

        aggregateRoot = new FakeAggregateRoot(TEST_ID);
        aggregateRoot.greetPerson("Erik");
        aggregateRoot.greetPerson("Sjors");
        subject.add(aggregateRoot);
    }
    
    @Test
    def shouldFailToAddAggregateWithoutAnyUnsavedChanges() {
        val a = new FakeAggregateRoot(VersionedId.random());
        try {
            subject.add(a);
            fail("IllegalArgumentException expected");
        } catch { 
          case expected : IllegalArgumentException => {}
        }
    }
    
    @Test
    def shouldFailOnNonExistingAggregateRoot() {
        val id = VersionedId.random();
        try {
            subject.getByVersionedId(classOf[FakeAggregateRoot], id);
            fail("AggregateRootNotFoundException expected");
        }  catch { 
          case expected : AggregateRootNotFoundException => {
            assertEquals(classOf[FakeAggregateRoot].getName(), expected.getAggregateRootType());
            assertEquals(id.getId(), expected.getAggregateRootId());
          }
        }
    }
    
    @Test
    def shouldLoadAggregateRootFromEventStore() {
        subject.afterHandleMessage();
        
        val result = subject.getByVersionedId(classOf[FakeAggregateRoot], TEST_ID);
        
        assertNotNull(result);
        assertEquals(aggregateRoot.getLastGreeting(), result.getLastGreeting());
    }
    
    @Test
    def shouldLoadAggregateOnlyOnce() {
        val a = subject.getById(classOf[FakeAggregateRoot], TEST_ID.getId());

        assertSame(aggregateRoot, a);
    }
    
    @Test
    def shouldRejectDifferentAggregatesWithSameId() {
        val a = aggregateRoot;
        val b = new FakeAggregateRoot(TEST_ID);
        b.greetPerson("Jan");
        
        subject.add(a);
        try {
            subject.add(b);
            fail("IllegalStateException expected");
        }  catch { 
          case expected :IllegalStateException => {}
        }
    }
    
    @Test
    def shouldCheckAggregateVersionOnLoadFromSession() {
        try {
            subject.getByVersionedId(classOf[FakeAggregateRoot], TEST_ID.withVersion(0));
            fail("OptimisticLockingFailureException expected");
        }  catch { 
          case expected :OptimisticLockingFailureException => { }
        }
    }
    
    @Test
    def shouldStoreAddedAggregate() {
        aggregateRoot.greetPerson("Erik");
        
        subject.afterHandleMessage();
    }
    
    @Test
    def shouldStoreLoadedAggregateWithNextVersion() {
        subject.afterHandleMessage();

        val result = subject.getByVersionedId(classOf[FakeAggregateRoot], TEST_ID);
        result.greetPerson("Mark");
        subject.afterHandleMessage();
        
        val loaded = subject.getByVersionedId(classOf[FakeAggregateRoot], TEST_ID.nextVersion());
        
        assertEquals("Hi Mark", loaded.getLastGreeting());
    }
    
    @Test
    def shouldPublishChangeEventsOnSave() {
        aggregateRoot.greetPerson("Erik");
        val expectedUnsavedEvents = aggregateRoot.getUnsavedEvents() ::: List()

        subject.afterHandleMessage();
        
        Mockito.verify(bus).publish(expectedUnsavedEvents);
    }
    
    @Test
    def shouldReplyWithNotificationsOnSave() {
        aggregateRoot.greetPerson("Erik");
        val expectedNotifications = aggregateRoot.getNotifications() ::: List()
        
        subject.afterHandleMessage();
        
        Mockito.verify(bus).reply(expectedNotifications);
    }
}
