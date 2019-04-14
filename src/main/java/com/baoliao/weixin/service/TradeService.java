package com.baoliao.weixin.service;

import javax.servlet.http.HttpServletRequest;

public interface TradeService {

    String payByBalance(HttpServletRequest request) throws Exception;

    String payByWeixin(HttpServletRequest request) throws Exception;

    void queryTradeList(HttpServletRequest request) throws Exception;

    String oper_cash(HttpServletRequest request, UserService userService) throws Exception;

    void queryDepositList(HttpServletRequest request) throws Exception;
}
