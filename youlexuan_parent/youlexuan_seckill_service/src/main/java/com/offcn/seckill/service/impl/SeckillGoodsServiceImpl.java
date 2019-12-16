package com.offcn.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.pojo.TbSeckillGoods;
import com.offcn.pojo.TbSeckillGoodsExample;
import com.offcn.pojo.TbSeckillGoodsExample.Criteria;
import com.offcn.seckill.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

/**
 * seckill_goods服务实现层
 * @author senqi
 *
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.insert(seckillGoods);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillGoods seckillGoods){
		seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillGoods findOne(Long id){
		return (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillGoodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillGoodsExample example=new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillGoods != null){			
						if(seckillGoods.getTitle() != null && seckillGoods.getTitle().length() > 0){
				criteria.andTitleLike("%" + seckillGoods.getTitle() + "%");
			}			if(seckillGoods.getSmallPic() != null && seckillGoods.getSmallPic().length() > 0){
				criteria.andSmallPicLike("%" + seckillGoods.getSmallPic() + "%");
			}			if(seckillGoods.getSellerId() != null && seckillGoods.getSellerId().length() > 0){
				criteria.andSellerIdLike("%" + seckillGoods.getSellerId() + "%");
			}			if(seckillGoods.getStatus() != null && seckillGoods.getStatus().length() > 0){
				criteria.andStatusLike("%" + seckillGoods.getStatus() + "%");
			}			if(seckillGoods.getIntroduction() != null && seckillGoods.getIntroduction().length() > 0){
				criteria.andIntroductionLike("%" + seckillGoods.getIntroduction() + "%");
			}
		}
		
		Page<TbSeckillGoods> page= (Page<TbSeckillGoods>)seckillGoodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbSeckillGoods> findList() {

		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();

		if(seckillGoodsList == null || seckillGoodsList.size() == 0) {
			TbSeckillGoodsExample ex = new TbSeckillGoodsExample();
			Criteria c = ex.createCriteria();

			c.andStatusEqualTo("1");
			c.andStartTimeLessThanOrEqualTo(new Date());
			c.andEndTimeGreaterThanOrEqualTo(new Date());
			c.andStockCountGreaterThan(0);
			seckillGoodsList = seckillGoodsMapper.selectByExample(ex);

			System.out.println(">>>>>添加秒杀商品到redis");
			for (TbSeckillGoods tbSeckillGood : seckillGoodsList) {
				redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGood.getId(), tbSeckillGood);
			}
		} else {

		}

		return seckillGoodsList;
	}

}
