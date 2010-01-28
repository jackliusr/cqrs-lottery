package com.xebia.lottery.queries;

import java.util.List;
import java.util.UUID;

trait LotteryQueryService {

    def findUpcomingLotteries() : List[LotteryInfoQueryResult];

    def findCustomers() : List[CustomerAccountQueryResult];

    def findLotteryTicketsForCustomer(customerId : UUID) : List[CustomerTicketsQueryResult];

    def getCustomerName(customerId : UUID) : String;

}
