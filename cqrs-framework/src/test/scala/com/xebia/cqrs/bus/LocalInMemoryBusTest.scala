package com.xebia.cqrs.bus;

import scala.collection._

import org.junit.Assert._;

import org.junit._;

object LocalInMemoryBusTest {
  	case class TestCommand(val message : String)
 
	case class TestEvent(val message : String)
}

class LocalInMemoryBusTest {
	import LocalInMemoryBusTest._
 
	class TestEventHandler extends PartialFunction[AnyRef, Unit] {
  	  def isDefinedAt(arg : AnyRef) = arg.isInstanceOf[TestEvent]

	  def apply(arg : AnyRef) {
	    val event = arg.asInstanceOf[TestEvent]
		assertSame(event, LocalInMemoryBusTest.this.subject
					.getCurrentMessage());
		LocalInMemoryBusTest.this.testEventHandlerCalled = true;
	  }
	}
    
	class TestCommandHandler extends PartialFunction[AnyRef, Unit] {
	  var lastMessage : String = _;
   
	  def isDefinedAt(arg : AnyRef) = arg.isInstanceOf[TestCommand]
	  
	  def apply(arg : AnyRef) {
		val command = arg.asInstanceOf[TestCommand]
	    assertSame(command, LocalInMemoryBusTest.this.subject
				.getCurrentMessage());
		this.lastMessage = command.message;
		LocalInMemoryBusTest.this.testCommandHandlerCalled = true;
	  }
	} 
  
	var subject : LocalInMemoryBus = _;
	var commandHandler : TestCommandHandler = _;
	var eventHandler : TestEventHandler = _;

	var testCommandHandlerCalled : Boolean = _;
	var testEventHandlerCalled : Boolean = _;

	import org.springframework.transaction._
	def fakePlatformTransactionManager = new PlatformTransactionManager {
		def getTransaction(definition : TransactionDefinition) = {
			new support.SimpleTransactionStatus
		}
  
		def commit(status : TransactionStatus) = ()
  
		def rollback(status : TransactionStatus) = ()
	}
 
	@Before
	def setUp() {
		this.commandHandler = new TestCommandHandler();
		this.eventHandler = new TestEventHandler();
		this.subject = new LocalInMemoryBus(
		  mutable.Set(this.commandHandler, this.eventHandler)) {
		  val platformTransactionManager = fakePlatformTransactionManager
		}
	}

	@Test
	def shouldSendCommandToRegisteredHandler() {
		this.subject.send(new TestCommand("hello"));
		assertTrue(this.testCommandHandlerCalled);
		assertEquals("hello", this.commandHandler.lastMessage);
	}

	@Test
	def shouldSendEventToRegisteredHandler() {
		this.subject.publish(new TestEvent(""));
		assertTrue(this.testEventHandlerCalled);
	}

	@Test
	def shouldFailToReplyWhenThereIsNoCurrentMessage() {
		try {
			this.subject.reply(new TestEvent("foo"));
			fail("MessageHandlingException expected");
		} catch { 
		  case expected : MessageHandlingException => {
			assertEquals("no current message to reply to", expected
					.getMessage());
		}
      }
	}

	@Test
	def shouldInvokeMessageHandlerForReply() {
	  this.subject = new LocalInMemoryBus( 
		  mutable.Set(this.eventHandler, { case arg : TestCommand =>
                                  subject.reply(new TestEvent(""));
					assertFalse(testEventHandlerCalled);
		  	})) {
		  val platformTransactionManager = fakePlatformTransactionManager
	    }
//				new PartialFunction[AnyRef, Unit] {
//				  def isDefinedAt(arg : AnyRef) = arg.isInstanceOf[TestCommand]
//	  
//				  def apply(arg : AnyRef) {
//				    val command = arg.asInstanceOf[TestCommand]
//					subject.reply(new TestEvent(""));
//					assertFalse(testEventHandlerCalled);
//				  }}

		this.subject.sendAndWaitForResponse(new TestCommand("test command"));

		assertTrue(this.testEventHandlerCalled);
	}

	@Test
	def shouldPostponeInvokingHandlersUntilCurrentMessageHasBeenProcessed() {
	  this.subject = new LocalInMemoryBus( 
		  mutable.Set(this.eventHandler, 
				new PartialFunction[AnyRef, Unit] {
				  def isDefinedAt(arg : AnyRef) = arg.isInstanceOf[TestCommand]
	  
				  def apply(arg : AnyRef) {
						subject.publish(new TestEvent(""));
						assertFalse(testEventHandlerCalled);
					}
				})) {
		  val platformTransactionManager = fakePlatformTransactionManager
	    }

		this.subject.send(new TestCommand("test command"));

		assertTrue(this.testEventHandlerCalled);
	}

	@Test
	def shouldRespondWithRepliedMessages() {
		this.subject = new LocalInMemoryBus( 
		  mutable.Set(this.eventHandler, 
				new PartialFunction[AnyRef, Unit] {
				  def isDefinedAt(arg : AnyRef) = arg.isInstanceOf[TestCommand]
	  
				  def apply(arg : AnyRef) {
						subject.reply(new TestEvent("event"));
				  }
				})) {
			val platformTransactionManager = fakePlatformTransactionManager
		  }

		val response = this.subject
				.sendAndWaitForResponse(new TestCommand("hello"));

		assertTrue(response.containsReplyOfType(classOf[TestEvent]));
		assertEquals("event", response.getReplyOfType(classOf[TestEvent]).message);
	}

	@Test
	def shouldSupportNestedSendingOfCommands() {
		this.subject = new LocalInMemoryBus( 
		  mutable.Set(this.eventHandler, 
                { 
                  case message : TestCommand =>
                	if (!testCommandHandlerCalled) {
						subject.reply(new TestEvent("top"));
						testCommandHandlerCalled = true;
						val response = subject
								.sendAndWaitForResponse(new TestCommand(
										"there"));
						assertTrue(response
								.containsReplyOfType(classOf[TestEvent]));
						assertEquals("nested", response.getReplyOfType(
								classOf[TestEvent]).message);
					} else {
						subject.reply(new TestEvent("nested"));
					}
                })) {
			val platformTransactionManager = fakePlatformTransactionManager
		  }

		val response = this.subject
				.sendAndWaitForResponse(new TestCommand("hello"));

		assertTrue(response.containsReplyOfType(classOf[TestEvent]));
		assertEquals("top", response.getReplyOfType(classOf[TestEvent]).message);
	}
}
