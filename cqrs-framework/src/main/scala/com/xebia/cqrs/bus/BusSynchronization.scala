package com.xebia.cqrs.bus;

trait BusSynchronization {

    def beforeHandleMessage();
    
    def afterHandleMessage();
}
