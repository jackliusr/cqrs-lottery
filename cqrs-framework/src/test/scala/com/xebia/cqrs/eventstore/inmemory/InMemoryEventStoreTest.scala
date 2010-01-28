package com.xebia.cqrs.eventstore.inmemory;

import com.xebia.cqrs.eventstore.AbstractEventStoreTest;


class InMemoryEventStoreTest extends AbstractEventStoreTest {

    @Override
    override protected def createSubject() = new InMemoryEventStore[String]();
}
