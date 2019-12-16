package com.offcn.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.mapper.TbSeckillOrderMapper;
import com.offcn.pojo.TbSeckillGoods;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.pojo.TbSeckillOrderExample;
import com.offcn.pojo.TbSeckillOrderExample.Criteria;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

/**
 * seckill_order服务实现层
 *
 * @author senqi
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;


    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.selectByExample(null);
    }

    /**
     * 分页
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Long id, String name) {
        TbSeckillGoods secKillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
        if (secKillGoods == null) {
            throw new RuntimeException("该商品已抢完，下次早点来哦");
        }
        if (secKillGoods.getStockCount() <= 0) {
            throw new RuntimeException("该商品已抢完，下次早点来哦");
        }
        if (secKillGoods.getEndTime().getTime() < new Date().getTime()) {
            throw new RuntimeException("该商品秒杀已截止，下次早点来哦");
        }
        // 库存减1
        secKillGoods.setStockCount(secKillGoods.getStockCount() - 1);
        redisTemplate.boundHashOps("seckillGoods").put(id, secKillGoods);

        if (secKillGoods.getStockCount() == 0) {
            // 将该秒杀商品同步到数据库
            seckillGoodsMapper.updateByPrimaryKey(secKillGoods);
            // 从缓存移除该秒杀商品
            redisTemplate.boundHashOps("seckillGoods").delete(id);
        }

        // 生成和用户相关的抢购订单
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setMoney(secKillGoods.getCostPrice());//秒杀价格
        seckillOrder.setSeckillId(id);
        seckillOrder.setSellerId(secKillGoods.getSellerId());
        seckillOrder.setUserId(name);//设置用户ID
        seckillOrder.setStatus("0");//状态

        // 将订单放入缓存
        redisTemplate.boundHashOps("seckillOrder").put(name, seckillOrder);
    }

    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillOrderMapper.deleteByPrimaryKey(id);
        }
    }

    /**
     * 分页+查询
     */
    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSeckillOrderExample example = new TbSeckillOrderExample();
        Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
            }
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
            }
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
            }
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
            }
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
            }
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
            }
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
            }
        }

        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateOrderStatus(String name) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(name);
        seckillOrder.setPayTime(new Date());
        seckillOrder.setStatus("1");
        // 将已经支付的秒杀订单入库
        seckillOrderMapper.insert(seckillOrder);

        // 将缓存对应的秒杀订单移除
        redisTemplate.boundHashOps("seckillOrder").delete(name);
    }

    @Override
    public void doSomeThingAtTimeOut(String name) {
        // 1. 还原库存
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(name);

        // 找到对应的秒杀商品的id
        Long seckillId = seckillOrder.getSeckillId();

        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
        // seckillGoods 有可能不存在：库存没有了；时间到期了
        // 时间到期了: 不用还原了
        // 库存没有了: 需要还原
        if(seckillGoods == null) {
            TbSeckillGoods tbSeckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillId);
            if(tbSeckillGoods.getEndTime().getTime() > new Date().getTime()) {

            } else {
                seckillGoods = tbSeckillGoods;
            }
        }

        if(seckillGoods != null) {
            seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
            // 重新存回到redis中
            redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
        }

        // 2. 移除redis中的订单信息
        redisTemplate.boundHashOps("seckillOrder").delete(name);

    }

}
