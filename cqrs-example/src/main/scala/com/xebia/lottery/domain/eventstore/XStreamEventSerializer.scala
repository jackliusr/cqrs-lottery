package com.xebia.lottery.domain.eventstore;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.xebia.cqrs.domain.Event;
import com.xebia.cqrs.eventstore.EventSerializer;
import com.xebia.lottery.events.LotteryEvent;

object XStreamEventSerializer {  
    val LOG = Logger.getLogger(classOf[XStreamEventSerializer]);
}  

class XStreamEventSerializer extends EventSerializer[Event] {
	import XStreamEventSerializer._
  
    val xstream = new XStream();
    xstream.aliasPackage("event", classOf[LotteryEvent].getPackage().getName());
    xstream.addImmutableType(classOf[UUID]);
    
    def deserialize(serialized: Object) = {
        val result = xstream.fromXML(serialized.asInstanceOf[String]).asInstanceOf[Event];
        if (LOG.isDebugEnabled()) {
            LOG.debug("deserialized " + result + " from " + serialized);
        }
        result;
    }

    def serialize(event: Event) = {
        val result = xstream.toXML(event);
        if (LOG.isDebugEnabled()) {
            LOG.debug("serialized " + event + " to " + result);
        }
        result;
    }
}
