package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.pojo.TbItemCat;
import com.offcn.pojo.TbItemCatExample;
import com.offcn.pojo.TbItemCatExample.Criteria;
import com.offcn.sellergoods.service.ItemCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;

/**
 * item_cat服务实现层
 * @author senqi
 *
 */
@Service(timeout = 3000)
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private RedisTemplate redisTemplate;


	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insert(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKey(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			// 将没有删除的id返回页面提出出来
			// 考虑一下有没有子节点
			List<TbItemCat> list = findParentById(id);
			// 有的话，不删除
			if(list != null && list.size() > 0) {
			}
			// 没有的，删除
			else {
				itemCatMapper.deleteByPrimaryKey(id);
			}
		}


	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		
		if(itemCat != null){			
						if(itemCat.getName() != null && itemCat.getName().length() > 0){
				criteria.andNameLike("%" + itemCat.getName() + "%");
			}
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbItemCat> findParentById(Long parentId) {
		TbItemCatExample ex = new TbItemCatExample();
		Criteria c = ex.createCriteria();
		c.andParentIdEqualTo(parentId);

		Set keys = redisTemplate.boundHashOps("itemCat").keys();
		if(keys.size() > 0) {
			System.out.println(">>>>>分类列表已缓存");
		}
		// 添加缓存：为了商品搜索页的分类、品牌数据的查询
		else {
			System.out.println(">>>>>添加分类缓存");
			List<TbItemCat> itemCatList = itemCatMapper.selectByExample(null);
			for (TbItemCat itemCat : itemCatList) {
				redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
			}
		}

		return itemCatMapper.selectByExample(ex);
	}

}
