package com.offcn.task;

import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;

/**
 * 定时任务：监视redis缓存中的秒杀商品
 * 过期后进行移除
 */
@Component
public class WatchSecKillGoods {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Scheduled(cron = "* * * * * *")
    public void removeTimeoutSecKillGoods() {
        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
        for (TbSeckillGoods sG : seckillGoods) {
            if(sG.getEndTime().getTime() < new Date().getTime()) {
                // 同步到数据库
                seckillGoodsMapper.updateByPrimaryKey(sG);
                // 从redis中移除
                redisTemplate.boundHashOps("seckillGoods").delete(sG.getId());

                System.out.println(">>>>>" + sG.getId() + ">>>" + sG.getTitle() + "从redis中移除....");

            }
        }
    }

}
