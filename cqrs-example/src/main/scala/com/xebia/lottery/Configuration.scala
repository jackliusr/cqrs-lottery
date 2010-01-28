package com.xebia.lottery

object Configuration {
	  import com.xebia.cqrs._
	  import com.xebia.lottery._
	  import org.springframework.jdbc.core.simple.SimpleJdbcTemplate
	  import org.springframework.jdbc.datasource._
   
	  val dataSource = new SimpleDriverDataSource() {{
	    setDriverClass(classOf[org.hsqldb.jdbcDriver])
	    setUrl("jdbc:hsqldb:mem:cqrs_war")
	  }}
   
	  val jdbcTemplate = new SimpleJdbcTemplate(dataSource)
   
	  val transactionManager = new DataSourceTransactionManager(dataSource)

	  val inMemoryBus : bus.LocalInMemoryBus = new bus.LocalInMemoryBus(
	    collection.mutable.Set(
	    	new reporting.eventhandlers.CustomerBalanceChangedEventHandler(jdbcTemplate),
	    	new reporting.eventhandlers.CustomerCreatedEventHandler(jdbcTemplate),
	    	new reporting.eventhandlers.LotteryCreatedEventHandler(jdbcTemplate),
	    	new reporting.eventhandlers.LotteryTicketPurchasedEventHandler(jdbcTemplate)
	  	)) {
	  	  val platformTransactionManager = transactionManager
	  	}
     
	  val repository = new dao.RepositoryImpl(
	        new eventstore.jdbc.JdbcEventStore(
	          jdbcTemplate, 
	          new lottery.domain.eventstore.XStreamEventSerializer), 
	        inMemoryBus);
	  inMemoryBus.registerSynchronization(repository)
   
	  val customerFactory = new lottery.domain.aggregates.CustomerFactory(inMemoryBus)
	  inMemoryBus.registerHandler(new lottery.domain.commandhandlers.CreateCustomerCommandHandler(repository, customerFactory))
	  inMemoryBus.registerHandler(new lottery.domain.commandhandlers.CreateLotteryCommandHandler(repository))
	  inMemoryBus.registerHandler(new lottery.domain.commandhandlers.DrawLotteryCommandHandler(repository))
	  inMemoryBus.registerHandler(new lottery.domain.commandhandlers.PurchaseLotteryTicketCommandHandler(repository))
   
	  val lotteryQueryService = new reporting.queries.JdbcLotteryQueryService(jdbcTemplate)
}
