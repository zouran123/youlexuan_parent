package com.offcn.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Cart;
import com.offcn.mapper.TbOrderItemMapper;
import com.offcn.mapper.TbOrderMapper;
import com.offcn.mapper.TbPayLogMapper;
import com.offcn.order.service.OrderService;
import com.offcn.pojo.TbOrder;
import com.offcn.pojo.TbOrderExample;
import com.offcn.pojo.TbOrderExample.Criteria;
import com.offcn.pojo.TbOrderItem;
import com.offcn.pojo.TbPayLog;
import com.offcn.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * order服务实现层
 * @author senqi
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private TbOrderItemMapper orderItemMapper;


	@Autowired
	private RedisTemplate redisTemplate;


	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbPayLogMapper payLogMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder o) {
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(o.getUserId());
		// 针对id的处理：
		// 订单表的数据，随着使用时间的延续，会不会越来越大，甚至大的不可思议？

		double totalSum = 0;

		StringBuilder sb = new StringBuilder();

		// 1个cart对应一个order
		// 1个cart对象中的 orderItemList 对应 order表的字表tb_order_item表
		if(cartList != null && cartList.size() > 0) {
			for (Cart cart : cartList) {
				TbOrder order = new TbOrder();

				long oId = idWorker.nextId();

				// 拼接订单号
				sb.append(oId + ",");

				order.setOrderId(oId);

				order.setPaymentType(o.getPaymentType());
				order.setStatus(o.getStatus());
				order.setCreateTime(o.getCreateTime());
				order.setUpdateTime(o.getUpdateTime());
				order.setUserId(o.getUserId());
				order.setReceiver(o.getReceiver());
				order.setReceiverAreaName(o.getReceiverAreaName());
				order.setReceiverMobile(o.getReceiverMobile());
				order.setSellerId(cart.getSellerId());

				// 对应某个商家的 付款总金额
				double sum = 0.0;
				for (TbOrderItem orderItem : cart.getOrderItemList()) {
					sum += orderItem.getTotalFee().doubleValue();
					orderItem.setId(idWorker.nextId());

					// 关联tb_order表的id
					orderItem.setOrderId(oId);

					orderItemMapper.insert(orderItem);
				}
				order.setPayment(new BigDecimal(sum));

				totalSum += sum;

				orderMapper.insert(order);
			}
			redisTemplate.boundHashOps("cartList").delete(o.getUserId());

			// 开始缓存  支付日志 的数据
			TbPayLog payLog = new TbPayLog();

			payLog.setOutTradeNo(idWorker.nextId() + "");
			payLog.setCreateTime(new Date());
			// 支付总金额
			payLog.setTotalFee(new BigDecimal(totalSum));

			payLog.setUserId(o.getUserId());
			// 0表示为支付，1已支付
			payLog.setTradeState("0");
			// 支付类型，1表支付宝，2表线下付款
			payLog.setPayType(o.getPaymentType());

			// 删除sb的最后一位的逗号
			sb.deleteCharAt(sb.length() - 1);
			payLog.setOrderList(sb.toString());

			// 将支付日志存入缓存：已登录用户id作为键
			redisTemplate.boundHashOps("payLog").put(o.getUserId(), payLog);
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param orderId
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order != null){			
						if(order.getPaymentType() != null && order.getPaymentType().length() > 0){
				criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
			}			if(order.getPostFee() != null && order.getPostFee().length() > 0){
				criteria.andPostFeeLike("%" + order.getPostFee() + "%");
			}			if(order.getStatus() != null && order.getStatus().length() > 0){
				criteria.andStatusLike("%" + order.getStatus() + "%");
			}			if(order.getShippingName() != null && order.getShippingName().length() > 0){
				criteria.andShippingNameLike("%" + order.getShippingName() + "%");
			}			if(order.getShippingCode() != null && order.getShippingCode().length() > 0){
				criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
			}			if(order.getUserId() != null && order.getUserId().length() > 0){
				criteria.andUserIdLike("%" + order.getUserId() + "%");
			}			if(order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0){
				criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
			}			if(order.getBuyerNick() != null && order.getBuyerNick().length() > 0){
				criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
			}			if(order.getBuyerRate() != null && order.getBuyerRate().length() > 0){
				criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
			}			if(order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0){
				criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
			}			if(order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0){
				criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
			}			if(order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0){
				criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
			}			if(order.getReceiver() != null && order.getReceiver().length() > 0){
				criteria.andReceiverLike("%" + order.getReceiver() + "%");
			}			if(order.getInvoiceType() != null && order.getInvoiceType().length() > 0){
				criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
			}			if(order.getSourceType() != null && order.getSourceType().length() > 0){
				criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
			}			if(order.getSellerId() != null && order.getSellerId().length() > 0){
				criteria.andSellerIdLike("%" + order.getSellerId() + "%");
			}
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Override
	public void updateOrderStatus(String name, String out_trade_no) {
		// 如果设计的系统，一个用户只支持一笔未支付的订单，此时第二个参数，没有意义

		// 1. 先对payLog中的pay_time，trade_state进行更新，然后存入数据库
		TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps("payLog").get(name);
		payLog.setPayTime(new Date());
		// 已支付
		payLog.setTradeState("1");
		payLogMapper.insert(payLog);

		// 2. 找到对应的所有的order，更新status，payment_time
		String orderListStr = payLog.getOrderList();

		String[] orderIds = orderListStr.split(",");
		for (String orderId : orderIds) {
			TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));

			tbOrder.setPaymentTime(new Date());
			// 已支付
			tbOrder.setStatus("2");

			orderMapper.updateByPrimaryKey(tbOrder);
		}

		// 3. 从缓存中移除对应的支付日志
		redisTemplate.boundHashOps("payLog").delete(name);
	}

}
