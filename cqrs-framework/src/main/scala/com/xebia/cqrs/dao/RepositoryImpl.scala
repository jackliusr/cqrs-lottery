package com.xebia.cqrs.dao

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import collection._

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;

import com.xebia.cqrs.bus.Bus;
import com.xebia.cqrs.bus.BusSynchronization;
import com.xebia.cqrs.domain.AggregateRoot;
import com.xebia.cqrs.domain.AggregateRootNotFoundException;
import com.xebia.cqrs.domain.Event;
import com.xebia.cqrs.domain.Repository;
import com.xebia.cqrs.domain.VersionedId;
import com.xebia.cqrs.eventstore.EventSink;
import com.xebia.cqrs.eventstore.EventSource;
import com.xebia.cqrs.eventstore.EventStore;

object RepositoryImpl {
  class AggregateRootSource(
          aggregateRoot: AggregateRoot
          ) extends EventSource[Event] {    
    def getType() = aggregateRoot.getClass().getName();

    def getVersion() = aggregateRoot.getVersionedId().getVersion();

    def getTimestamp() = System.currentTimeMillis();

    def getEvents() = aggregateRoot.getUnsavedEvents();
  }

  class AggregateRootSink[T <: AggregateRoot](
          expectedType: Class[T],
          id: UUID
          ) extends EventSink[Event] {
    var actualType: Class[_ <: T] = _;
    var actualVersion: Long = _;
    var aggregateRoot: T = _;

    def setType(typee: String) {
      try {
        actualType = Class.forName(typee).asSubclass(expectedType);
      } catch {
        case ex: ClassNotFoundException => {
          throw new RuntimeException(ex);
        }
      }
    }

    def setVersion(version: Long) {
      actualVersion = version + 1;
    }

    def setTimestamp(timestamp: Long) = ()

    def setEvents(events: Seq[Event]) {
      instantiateAggregateRoot();
      aggregateRoot.loadFromHistory(events);
    }

    private def instantiateAggregateRoot() {
      try {
        val constructor = actualType.getConstructor(classOf[VersionedId]);
        aggregateRoot = constructor.newInstance(VersionedId.forSpecificVersion(id, actualVersion));
      } catch {
        case e: SecurityException => throw new RuntimeException(e)
        case e: NoSuchMethodException => throw new RuntimeException(e);
        case e: IllegalArgumentException => throw new RuntimeException(e);
        case e: InstantiationException => throw new RuntimeException(e);
        case e: IllegalAccessException => throw new RuntimeException(e);
        case e: InvocationTargetException => throw new RuntimeException(e);
      }
    }
    def getAggrateRoot() = aggregateRoot;
  }
}

class RepositoryImpl(
    eventStore : EventStore[Event],
    bus : Bus
) extends Repository with BusSynchronization {  
  val sessions = new ThreadLocal[Session]() {
    override protected def initialValue() = new Session();
  };

  override def getById[T <: AggregateRoot](typee: Class[T], id: UUID) = sessions.get().getById(typee, id);

  override def getByVersionedId[T <: AggregateRoot](typee: Class[T], id: VersionedId) = sessions.get().getByVersionedId(typee, id);

  override def add[T <: AggregateRoot](aggregate: T) {
    if (aggregate != null) {
      sessions.get().add(aggregate);
    }
  }

  def afterHandleMessage() {
    sessions.get().afterHandleMessage();
  }

  def beforeHandleMessage() {
    sessions.get().beforeHandleMessage();
  }

  private[RepositoryImpl] class Session {
    var aggregatesById = new mutable.HashMap[UUID, AggregateRoot]();

    def getById[T <: AggregateRoot](expectedType: Class[T], id: UUID): T = {
      aggregatesById.get(id) match {
        case Some(aggregate) => expectedType.cast(aggregate)
        case None =>
          try {
	        val sink = new RepositoryImpl.AggregateRootSink[T](expectedType, id);
	        eventStore.loadEventsFromLatestStreamVersion(id, sink);
	        return sink.getAggrateRoot();
	      } catch {
	        case ex: EmptyResultDataAccessException => {
	          throw new AggregateRootNotFoundException(expectedType.getName(), id);
	        }
	      }
      }
    }

    def getByVersionedId[T <: AggregateRoot](expectedType: Class[T], id: VersionedId): T = {
      aggregatesById.get(id.getId) match {
        case Some(aggregate) => 
          	val result = expectedType.cast(aggregate)
        	if (!id.nextVersion().equals(result.getVersionedId())) {
	          throw new OptimisticLockingFailureException("actual: " + (result.getVersionedId().getVersion() - 1) + ", expected: " + id.getVersion());
	        }
	        result;
        case None =>
          try {
	        val sink = new RepositoryImpl.AggregateRootSink[T](expectedType, id.getId());
	        eventStore.loadEventsFromExpectedStreamVersion(id.getId(), id.getVersion(), sink);
	        val result = sink.getAggrateRoot();
	        addToSession(result);
	        return result;
	      } catch {
	        case ex: EmptyResultDataAccessException =>
	          throw new AggregateRootNotFoundException(expectedType.getName(), id.getId());
	      }
      }
    }

    def add[T <: AggregateRoot](aggregate: T) {
      if (aggregate.getUnsavedEvents().isEmpty) {
        throw new IllegalArgumentException("aggregate has no unsaved changes");
      }
      addToSession(aggregate);
    }

    private def addToSession[T <: AggregateRoot](aggregate: T) {
      aggregatesById.put(aggregate.getVersionedId().getId(), aggregate) match {
        case Some(previous) if previous != aggregate => 
          throw new IllegalStateException("multiple instances with same id " + aggregate.getVersionedId().getId());
        case _ => ()
      }
    }

    def beforeHandleMessage() = ()

    def afterHandleMessage() {
      val notifications = aggregatesById.values.toList.flatMap { _.getNotifications()};
      aggregatesById.values.foreach {
        aggregate =>
          aggregate.clearNotifications();

          val unsavedEvents = aggregate.getUnsavedEvents();
          if (!unsavedEvents.isEmpty) {
            bus.publish(unsavedEvents);
            saveAggregate(aggregate);
          }
      }
      bus.reply(notifications);

      // should be done just before transaction commit...
      aggregatesById.clear();
    }

    def saveAggregate(aggregate: AggregateRoot) {
      if (aggregate.getVersionedId().isForInitialVersion()) {
        eventStore.createEventStream(
          aggregate.getVersionedId().getId(),
          new RepositoryImpl.AggregateRootSource(aggregate));
      } else {
        eventStore.storeEventsIntoStream(
          aggregate.getVersionedId().getId(),
          aggregate.getVersionedId().getVersion() - 1,
          new RepositoryImpl.AggregateRootSource(aggregate));
      }
      aggregate.clearUnsavedEvents();
      aggregate.incrementVersion();
    }

  }
}
