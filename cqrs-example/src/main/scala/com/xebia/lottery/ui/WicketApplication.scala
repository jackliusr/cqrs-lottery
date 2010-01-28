package com.xebia.lottery.ui;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see com.xebia.lottery.ui.Start#main(String[])
 */
@Component
class WicketApplication extends WebApplication {
  
	var applicationContext : ApplicationContext = _
  
	def this(applicationContext : ApplicationContext) {
	  this();
	  this.applicationContext = applicationContext
	}
 
	override protected def init() {
	    super.init();
	    
	    initBookmarkablePages();
        initSpringInjection();
	}

    private def initBookmarkablePages() {
        mountBookmarkablePage("customers", classOf[CustomersPage]);
        mountBookmarkablePage("lotteries", classOf[LotteriesPage]);
        mountBookmarkablePage("purchase-ticket", classOf[PurchaseTicketPage]);
    }

    private def initSpringInjection() {
        if (applicationContext == null) {
	        addComponentInstantiationListener(new SpringComponentInjector(this));
	    } else {
            addComponentInstantiationListener(new SpringComponentInjector(this, applicationContext));
	    }
    }
	
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	def getHomePage() = classOf[PurchaseTicketPage]

}
