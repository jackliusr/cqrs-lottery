package com.xebia.cqrs.eventstore.jdbc;

import org.junit.Assert._;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.xebia.cqrs.eventstore.AbstractEventStoreTest;
import com.xebia.cqrs.eventstore.EventSerializer;
import com.xebia.cqrs.eventstore.EventStore;


@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration{ val locations = Array("/test-application-context.xml")}
class JdbcEventStoreTest extends AbstractEventStoreTest {
    
    var jdbcTemplate : SimpleJdbcTemplate = _;

	@Autowired 
    def setSimpleJdbcTemplate(template : SimpleJdbcTemplate) { jdbcTemplate = template }
    
    var eventSerializer = new EventSerializer[String]() {

        def serialize(event : String) = event;

        def deserialize(serialized : Object) = (serialized.asInstanceOf[String]);
    };

    @Override
    protected def createSubject() = {
        assertNotNull("jdbcTemplate not injected", jdbcTemplate);
        val result = new JdbcEventStore[String](jdbcTemplate, eventSerializer)
        result;
    }
    
}
