package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * goods服务实现层
 * @author senqi
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;


	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goodsMapper.insert(goods.getGoods());
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());

		saveItemList(goods);
	}

	/**
	 * 给item的属性赋值
	 * @param goods
	 * @param item
	 */
	private void setItemVal(Goods goods, TbItem item) {
		item.setCategoryid(goods.getGoods().getCategory3Id());
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());
		item.setGoodsId(goods.getGoods().getId());
		item.setSellerId(goods.getGoods().getSellerId());

		// 添加分类的显示值，冗余设计，为了页面显示方便
		String itemCatName = itemCatMapper.selectByPrimaryKey(item.getCategoryid()).getName();
		item.setCategory(itemCatName);

		// 添加品牌的名字，冗余设计，为了页面显示方便
		String brandName = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId()).getName();
		item.setBrand(brandName);

		// 添加店铺的名字，冗余设计，为了页面显示方便
		String nickName = sellerMapper.selectByPrimaryKey(item.getSellerId()).getNickName();
		item.setSeller(nickName);

		// 从good_desc的图片属性中默认取第一张
		List<Map> picList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (picList.size() > 0) {
			item.setImage((String) picList.get(0).get("url"));
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		// 修改3个内容
		// goods
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		// goodsDesc
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		// item
		// 先删除，再插入
		TbItemExample ex = new TbItemExample();
		TbItemExample.Criteria c = ex.createCriteria();
		c.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(ex);

		saveItemList(goods);
	}

	/**
	 * 保存itemList列表
	 * @param goods
	 */
	private void saveItemList(Goods goods) {
		if ("1".equals(goods.getGoods().getIsEnableSpec())) {
			// 将sku添加到tb_item表中
			for (TbItem item : goods.getItemList()) {
				String title = goods.getGoods().getGoodsName();
				// {"网络":"移动3G","机身内存":"16G"}
				String specStr = item.getSpec();
				// 移动3G 16G
				Map<String, Object> map = JSON.parseObject(specStr, Map.class);
				for (String key : map.keySet()) {
					title += " " + map.get(key);
				}
				item.setTitle(title);

				setItemVal(goods, item);

				itemMapper.insert(item);
			}
		}
		// 不启用规格
		else {
			// 不启动规格，默认向tb_item表添加1条数据
			TbItem item = new TbItem();

			item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
			item.setPrice(goods.getGoods().getPrice());//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认
			item.setNum(999);//库存数量
			item.setSpec("{}");

			setItemVal(goods, item);

			itemMapper.insert(item);
		}
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		// 3部分数据
		// goods
		TbGoods goods = goodsMapper.selectByPrimaryKey(id);
		// goodsDesc
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);

		// item
		TbItemExample ex = new TbItemExample();
		TbItemExample.Criteria c = ex.createCriteria();
		c.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(ex);

		return new Goods(goods, goodsDesc, itemList);

	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			// 将goods的is_delete属性改为：1
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
			// 将关联的item的status属性改为：3

			TbItemExample ex = new TbItemExample();
			TbItemExample.Criteria c = ex.createCriteria();
			c.andGoodsIdEqualTo(id);
			List<TbItem> itemList = itemMapper.selectByExample(ex);
			for (TbItem item : itemList) {
				item.setStatus("3");
				itemMapper.updateByPrimaryKey(item);
			}
		}
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods != null){			
			if(goods.getSellerId() != null && goods.getSellerId().length() > 0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}

						if(goods.getGoodsName() != null && goods.getGoodsName().length() > 0){
				criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
			}			if(goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0){
				criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
			}			if(goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0){
				criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
			}			if(goods.getCaption() != null && goods.getCaption().length() > 0){
				criteria.andCaptionLike("%" + goods.getCaption() + "%");
			}			if(goods.getSmallPic() != null && goods.getSmallPic().length() > 0){
				criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
			}			if(goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0){
				criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
			}			if(goods.getIsDelete() != null && goods.getIsDelete().length() > 0){
				criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
			}
		}

		criteria.andIsDeleteIsNull();

		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	@Override
	public List<TbItem> findItemByGoodsId(Long[] ids) {
		TbItemExample ex = new TbItemExample();
		TbItemExample.Criteria c = ex.createCriteria();
		c.andGoodsIdIn(Arrays.asList(ids));
		// 状态：正常
		c.andStatusEqualTo("1");
		return itemMapper.selectByExample(ex);
	}

}
