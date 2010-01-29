package com.xebia.cqrs.bus

import collection.mutable.ArrayBuffer
import org.apache.commons.lang.builder.{ToStringStyle, ToStringBuilder}


class Response(messages : Seq[AnyRef]) {
  def containsReplyOfType(t: Class[_ <: AnyRef]) = messages.exists { t.isInstance(_) }

  def getReplyOfType[T](t: Class[T]) = {
    getRepliesOfType(t) match {
    	case x :: Nil => x
    	case x :: xs => throw new IllegalArgumentException("multiple notifications of type " + t.getName());
    	case Nil => throw new IllegalArgumentException("no notification of type " + t.getName());
    }
  }

  def getRepliesOfType[T](t: Class[T]): List[T] = {
    for {notification <- messages.toList if (t.isInstance(notification))}
    yield t.cast(notification)
  }

  override def toString() = "Response (" + messages + ")";
}
