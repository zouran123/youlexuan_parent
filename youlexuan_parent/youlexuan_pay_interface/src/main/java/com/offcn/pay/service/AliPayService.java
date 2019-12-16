package com.offcn.pay.service;

import java.util.Map;

public interface AliPayService {

    /**
     * 预下单接口调用：返回生成二维码的url
     *
     * @param out_trade_no
     *            订单号
     * @param total_fee
     *            金额
     * @return
     */
    public Map createNative(String out_trade_no, String total_fee);

    /**
     * 查询支付状态
     * @param out_trade_no
     */
    public Map queryPayStatus(String out_trade_no);

}
