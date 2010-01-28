package com.xebia.cqrs.dao

object TransactionSupport {
	import org.springframework.transaction._
	implicit def transactional[T](body : => T)(implicit platformTransactionManager : PlatformTransactionManager) = {
	  var result = new support.TransactionTemplate(platformTransactionManager).execute(
	    new support.TransactionCallback() {
	      def doInTransaction(status : TransactionStatus) : Object = {
	        body.asInstanceOf[Object];
	      }
	    }
	  ).asInstanceOf[T]
      result
	}
}
