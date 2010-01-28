package com.xebia.cqrs.domain;

import java.util.UUID;


trait Repository {

    def getById[T <: AggregateRoot](typee : Class[T], id : UUID) : T;
    
    def getByVersionedId[T <: AggregateRoot](typee : Class[T], id : VersionedId ) : T;
    
    def add[T <: AggregateRoot](aggregate : T);
    
}
