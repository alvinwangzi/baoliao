package com.baoliao.weixin.service.impl;

import com.baoliao.weixin.Constants;
import com.baoliao.weixin.bean.Trade;
import com.baoliao.weixin.bean.User;
import com.baoliao.weixin.dao.TradeDao;
import com.baoliao.weixin.service.TradeService;
import com.baoliao.weixin.util.Utils;
import com.baoliao.weixin.util.WeChatPayUtils;
import com.baoliao.weixin.wechatpay.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradeServiceImpl implements TradeService {

    private Logger log = LoggerFactory.getLogger(TradeServiceImpl.class);

    @Autowired
    TradeDao tradeDao;

    @Value("${domain_name}")
    private String domainName;

    @Override
    public String payByBalance(HttpServletRequest request) throws Exception {
        String buyerOpenId = request.getParameter("buyerOpenId");
        String sellerOpenId = request.getParameter("sellerOpenId");
        String amount = request.getParameter("amount");
        String payType = request.getParameter("type");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String id = request.getParameter("id");
        Date currDate = new Date();

        Trade trade = new Trade();
        trade.setProductId(Integer.parseInt(id));
        trade.setMoney(amount);
        trade.setCreateTime(currDate);
        trade.setBuyerOpenId(buyerOpenId);
        trade.setSellerOpenId(sellerOpenId);
        trade.setPayType(Integer.parseInt(payType));
        log.info("========trade=============" + trade.toString());
        JSONObject jObject = new JSONObject();
        try {
            int num = tradeDao.saveTradeInfo(trade);
            if (num == 1) {
                resultMap.put("success", true);
                jObject = JSONObject.fromObject(resultMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("保存交易信息异常！" + e);
        }
        return jObject.toString();
    }

    @Override
    public String payByWeixin(HttpServletRequest request) throws Exception {
        String buyerOpenId = request.getParameter("buyerOpenId");
        String sellerOpenId = request.getParameter("sellerOpenId");
        String amount = request.getParameter("amount");
        String payType = request.getParameter("type");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String id = request.getParameter("id");
        Date currDate = new Date();

        Map<String, String> data = new HashMap<String, String>();
        String nonceStr = WXPayUtil.generateNonceStr();
        String appId = Constants.WECHAT_PARAMETER.APPID;
        String mchId = PayConstants.WECHAT_PAT_CONFIG.MCH_ID;
        String deviceInfo = "WEB";

//        data.put("appid", appId);
//        data.put("mch_id",mchId );
        data.put("device_info", deviceInfo);
//        data.put("nonce_str", nonceStr);
//        data.put("sign", "");
//        data.put("sign_type", WXPayConstants.MD5);
        data.put("body", "购买料" + id);
        data.put("detail", "购买料" + id);
        data.put("attach", "必赢大数据");
        data.put("out_trade_no", id);
        data.put("total_fee", String.valueOf((int) Double.parseDouble(amount) * 100));
        data.put("spbill_create_ip", WeChatPayUtils.getIp(request));
//        data.put("time_start", Constants.DATA_FORMAT.sdf2.format(currDate));
        data.put("notify_url", domainName + "/trade/paysuccessreturn");
        data.put("trade_type", "JSAPI");
        data.put("openid", buyerOpenId);

        log.info("请求统一下单接口参数:" + data);
        MyConfig config = new MyConfig();
        WXPay wxpay = new WXPay(config);
        Map<String, String> unifiedOrderMap = wxpay.unifiedOrder(data);

        log.info("调用统一下单接口返回结果:" + unifiedOrderMap);

        String returnCode = unifiedOrderMap.get("return_code");
        String resultCode = unifiedOrderMap.get("result_code");
        try {
            if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
                log.info("微信支付下单成功");
                // 进行签名校验
                Map<String, String> map = new HashMap<>();
                map.put("return_code", unifiedOrderMap.get("return_code"));
                map.put("return_msg", unifiedOrderMap.get("return_msg"));
                map.put("appid", unifiedOrderMap.get("appid"));
                map.put("mch_id", unifiedOrderMap.get("mch_id"));
                map.put("nonce_str", unifiedOrderMap.get("nonce_str"));
                map.put("result_code", unifiedOrderMap.get("result_code"));
                map.put("prepay_id", unifiedOrderMap.get("prepay_id"));
                map.put("trade_type", unifiedOrderMap.get("trade_type"));
                // 生成签名
                String mySign = WXPayUtil.generateSignature(map, PayConstants.WECHAT_PAT_CONFIG.MCH_APPSECRET);
                // 微信返回的签名
                String wxSign = unifiedOrderMap.get("sign");
                log.info("返回的签名" + wxSign);
                log.info("最后生成的签名" + mySign);
                System.out.println("最后生成的签名的参数:" + map);
                // 需要返回给页面的数据
                Map<String, String> returnMap = new HashMap<>();
                // TODO 验证签名
//                if (mySign.equals(wxSign)) {
                returnMap.put("appId", unifiedOrderMap.get("appid"));
                returnMap.put("timeStamp", WXPayUtil.getCurrentTimestamp() + "");
                returnMap.put("nonceStr", nonceStr);
                returnMap.put("package", "prepay_id=" + unifiedOrderMap.get("prepay_id"));
                returnMap.put("signType", WXPayConstants.HMACSHA256);
                // 此处生成的签名返回给页面作为参数
                returnMap.put("paySign", WXPayUtil.generateSignature(returnMap, PayConstants.WECHAT_PAT_CONFIG.MCH_APPSECRET));
                log.info("签名校验成功，下单返回信息为" + returnMap);

                Map<String, Object> storeMap = new HashMap<>();
                // 签名校验成功，你可以在此处进行自己业务逻辑的处理
                // storeMap可以存储那些你需要存进数据库的信息，可以生成预支付订单
                resultMap.put("data", returnMap);
                Trade trade = new Trade();
                trade.setProductId(Integer.parseInt(id));
                trade.setMoney(amount);
                trade.setCreateTime(currDate);
                trade.setBuyerOpenId(buyerOpenId);
                trade.setSellerOpenId(sellerOpenId);
                trade.setPayType(Integer.parseInt(payType));
                log.info("========trade=============" + trade.toString());
                JSONObject jObject = new JSONObject();
                try {
                    int num = tradeDao.saveTradeInfo(trade);
                    if (num == 1) {
                        resultMap.put("success", true);
                        jObject = JSONObject.fromObject(resultMap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("保存交易信息异常！" + e);
                }
                return jObject.toString();
               /* } else {
                    log.error("签名校验失败，下单返回信息为 --> {}", JSONObject.fromObject(resultMap));
                    // 签名校验失败，你可以在此处进行校验失败的业务逻辑
                }*/
            }
        } catch (Exception e) {
            log.error("用户支付，失败", e);
            return null;
        }
        return null;
    }

    @Override
    public void queryTradeList(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        String openId = user.getOpenId();
        List<Trade> tradeList = tradeDao.queryTradeList(openId, Constants.TRADE_TYPE.NOMAL_TRADE);
        session.setAttribute("tradeList", tradeList);
    }

    @Override
    public String oper_cash(HttpServletRequest request) throws Exception {
        String inputMoney = request.getParameter("inputMoney");
        User user = (User) request.getSession().getAttribute("user");
        Map<String, String> data = new HashMap<String, String>();
        String nonceStr = WXPayUtil.generateNonceStr();
        String mch_appid = Constants.WECHAT_PARAMETER.APPID;
        String mchid = PayConstants.WECHAT_PAT_CONFIG.MCH_ID;
        String deviceInfo = "WEB";

        data.put("mch_appid", mch_appid);
        data.put("mchid", mchid);
        data.put("device_info", deviceInfo);
        data.put("nonce_str", nonceStr);

        data.put("sign_type", WXPayConstants.MD5);

        data.put("partner_trade_no", Constants.DATA_FORMAT.sdf1.format(new Date()));
        data.put("openid", user.getOpenId());
        data.put("check_name", "NO_CHECK");
//        data.put("re_user_name", user.getNickName());
        data.put("re_user_name", "xunmengyiran");
        data.put("amount", String.valueOf((int) Double.parseDouble(inputMoney) * 100));
        data.put("desc", "tixian");
        data.put("spbill_create_ip", WeChatPayUtils.getIp(request));
        String sign = WXPayUtil.generateSignatureMD5(data, PayConstants.WECHAT_PAT_CONFIG.MCH_APPSECRET);
//        String sign = Utils.getSignCode(data,PayConstants.WECHAT_PAT_CONFIG.MCH_APPSECRET);
        data.put("sign", sign);
        log.info("提现申请参数:" + data);
        String xmlStr = WXPayUtil.mapToXml(data);
        log.info("转为xml参数:" + xmlStr);
//        JSONObject jsonObject = WeixinIntefaceUtil.httpRequest(Constants.URL.ENTERPRISE_PAY_URL,"POST",xmlStr);
        MyConfig config = new MyConfig();
        WXPay wxpay = new WXPay(config);
//        Map<String, String> tiXianResultMap = wxpay.tixian(data);
        String result = Utils.doRefund("https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers", xmlStr, mchid);
        log.info("请求提现的返回数据:" + result);
        Trade trade = new Trade();
        // 提现时候设置产品id -999
        trade.setProductId(-999);
        trade.setMoney(inputMoney);
        trade.setCreateTime(new Date());
        trade.setBuyerOpenId("");
        trade.setBuyerOpenId(user.getOpenId());
        trade.setPayType(1);
        trade.setTradeType(2);
        log.info("========trade=============" + trade.toString());
        Map<String, Object> resultMap = new HashMap<String, Object>();
        JSONObject jObject = new JSONObject();
        try {
           /* int num = tradeDao.saveOperCashInfo(trade);
            if (num == 1) {
                resultMap.put("success", true);
                jObject = JSONObject.fromObject(resultMap);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            log.error("保存交易信息异常！" + e);
        }
        //TODO 模拟提现成功

        return jObject.toString();
    }

    @Override
    public void queryDepositList(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        List<Trade> tradeList = tradeDao.queryTradeList(user.getOpenId(), Constants.TRADE_TYPE.DEPOSIT_TRADE);
        for (Trade trade : tradeList) {
            log.info(trade.toString());
        }
        session.setAttribute("depositList", tradeList);
    }
}
