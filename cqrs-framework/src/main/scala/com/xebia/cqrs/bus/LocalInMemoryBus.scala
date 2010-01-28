package com.xebia.cqrs.bus

import collection._;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;


object LocalInMemoryBus {
  private val LOG = Logger.getLogger(classOf[LocalInMemoryBus]);
  private class CurrentMessageInformation(
          private[LocalInMemoryBus] val currentMessage: AnyRef
          ) {
    val responses = new mutable.ArrayBuffer[Response]();

    def addReplies(messages: Seq[AnyRef]) {
      responses += new Response(messages);
    }
  }
}

abstract class LocalInMemoryBus(
  handlers : mutable.Set[PartialFunction[AnyRef, Unit]]
) extends Bus {
  import dao.TransactionSupport._
  import org.springframework.transaction.PlatformTransactionManager
  
  val synchronizations : mutable.ArrayBuffer[BusSynchronization] = new mutable.ArrayBuffer
  
  implicit val platformTransactionManager : PlatformTransactionManager
  
  def registerSynchronization(synchronization : BusSynchronization) {
    synchronizations += synchronization
  }

  def registerHandler(handler : PartialFunction[AnyRef, Unit]) {
	handlers += handler
  }
  
  val eventQueue = new ThreadLocal[mutable.Queue[AnyRef]]() {
    override protected def initialValue() = new mutable.Queue[AnyRef]()
  };

  val state = new ThreadLocal[LocalInMemoryBus.CurrentMessageInformation]() {
    override protected def initialValue() = new LocalInMemoryBus.CurrentMessageInformation(null);
  };

  @throws(classOf[MessageHandlingException])
  def send(message: AnyRef) {
    transactional {
	    dispatchMessage(message);
	    dispatchAllQueuedMessages();
    }
  }

  def sendAndWaitForResponse(command: AnyRef): Response = {
    transactional {
	    val responses = dispatchMessage(command);
	    if (responses.isEmpty) {
	      throw new MessageHandlingException("no response while executing command " + command);
	    }
	
	    dispatchAllQueuedMessages();
	
	    if (LocalInMemoryBus.LOG.isDebugEnabled() && responses.size > 1) {
	      LocalInMemoryBus.LOG.debug("additional responses ignored: " + responses.slice(1, responses.size));
	    }
	    responses(0);
     }
  }

  def reply(message: AnyRef) {
    transactional {
    	reply(List(message))
    }
  }

  def reply(messages: Seq[AnyRef]) {
    transactional {
	    if (getCurrentMessage() == null) {
	      throw new MessageHandlingException("no current message to reply to");
	    }
	
	    state.get().addReplies(messages);
	    publish(messages);
    }
  }

  def publish(message: AnyRef) {
    transactional {
    	publish(List(message));
    }
  }

  def publish(messages: Seq[AnyRef]) {
    transactional {
	    eventQueue.get() ++= messages;
	    if (getCurrentMessage() == null) {
	      dispatchAllQueuedMessages();
	    }
    }
  }

  def getCurrentMessage() = transactional { state.get().currentMessage; }
  
  def dispatchAllQueuedMessages() {
    transactional {
	    try {
	      while (!eventQueue.get().isEmpty) {
	        dispatchMessage(eventQueue.get().dequeue);
	      }
	    } catch {
	      case e: RuntimeException => {
	        eventQueue.get().clear();
	        throw e;
	      }
	    }
    }
  }

  def dispatchMessage(message: AnyRef): Seq[Response] = {
    Validate.notNull(message, "message is required");
    val savedState = state.get();
    try {
      state.set(new LocalInMemoryBus.CurrentMessageInformation(message));
      transactional {
	      invokeBeforeHandleMessage();
	      invokeHandlers(message);
	      invokeAfterHandleMessage();
      }
      return state.get().responses;
    } finally {
	  state.set(savedState);
    }
  }

  def invokeBeforeHandleMessage() {
	transactional { synchronizations foreach {_.beforeHandleMessage()} }
  }

  def invokeAfterHandleMessage() {
    transactional { synchronizations foreach {_.afterHandleMessage()} }
  }

  private def invokeHandlers(message: AnyRef) {
    try {
      val matchedHandlers = handlers.filter { _.isDefinedAt(message) }
      if (LocalInMemoryBus.LOG.isDebugEnabled()) {
        if (matchedHandlers.isEmpty) {
          LocalInMemoryBus.LOG.debug("no handlers rfor message of " + message.getClass());
        } else {
          LocalInMemoryBus.LOG.debug("dispatching to handlers: " + message);
        }
      }
      
      matchedHandlers.foreach { _(message) }
    } catch {
      case (ex: Exception) => {
        throw new MessageHandlingException(ex);
      }
    }
  }
}
