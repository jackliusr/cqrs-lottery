package com.xebia.lottery.domain.aggregates;

import org.junit.Assert._;

import java.util.List;

import org.junit.Before;

import com.xebia.cqrs.domain.AggregateRoot;
import com.xebia.cqrs.domain.Event;

object BddTestCase {
      protected[aggregates] def assertChange(aggregateRoot : AggregateRoot, expected: Event) {
        val changes = aggregateRoot.unsavedEvents;
        var matchedType : Event = null;
        changes.foreach { change =>
            if (change.equals(expected)) {
                return;
            } else if (change.getClass() == expected.getClass()) {
                matchedType = change;
            }
        }
        if (matchedType == null) {
            fail("event <" + expected + "> not found in changes " + changes);
        } else {
            fail("event <" + expected + "> not found, but event of matching type was <" + matchedType + ">");
        }
    }
}

abstract class BddTestCase {
    protected var caught : Exception = _;
    
    protected def given() {}
    protected def when();
    
    @Before
    def setup() {
        given();
        when();
    }
}
