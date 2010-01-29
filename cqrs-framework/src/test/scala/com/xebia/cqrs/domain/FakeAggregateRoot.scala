package com.xebia.cqrs.domain;

class FakeAggregateRoot(id : VersionedId) extends AggregateRoot(id) {

    var lastGreeting: String = _;

    def this() {
        this(AggregateRootTest.TEST_ID);
    }

    def greetPerson(name : String) {
        apply(new GreetingEvent(versionedId, "Hi " + name));
        notify(new GreetingNotification("Greeted " + name));
    }
    
    def getLastGreeting() = lastGreeting;
    
    override def onEvent(event : Event) {
        lastGreeting = (event.asInstanceOf[GreetingEvent]).message;
    }
}