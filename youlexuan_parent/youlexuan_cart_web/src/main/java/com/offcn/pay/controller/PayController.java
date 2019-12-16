package com.offcn.pay.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制层
 *
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private AliPayService aliPayService;

    @Reference
    private OrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps("payLog").get(name);

        if(payLog != null) {
            // 注意：金额应该保留2位小数，支付宝接口要求的范围[0.01, 100000000.00]
            return aliPayService.createNative(payLog.getOutTradeNo(), String.format("%.2f", payLog.getTotalFee()));
        } else {
            return new HashMap();
        }
    }


    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        Result result = null;

        int x = 0;
        while (true) {
            // 调用查询接口
            Map<String, String> map = null;
            try {
                map = aliPayService.queryPayStatus(out_trade_no);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (map == null) {// 出错
                result = new Result(false, "查询支付状态异常");
                break;
            }
            // 如果成功
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_SUCCESS")) {
                // 支付成功，更新相关的订单状态
                orderService.updateOrderStatus(name, out_trade_no);

                result = new Result(true, "支付成功");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_CLOSED")) {
                result = new Result(false, "未付款交易超时关闭");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_FINISHED")) {
                result = new Result(false, "交易结束");
                break;
            }

            try {
                Thread.sleep(3000);// 间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 如果变量超过设定值退出循环，超时为1分钟
            x++;
            if (x >= 20) {
                result = new Result(false, "二维码超时");
                break;
            }
        }

        return result;
    }


}