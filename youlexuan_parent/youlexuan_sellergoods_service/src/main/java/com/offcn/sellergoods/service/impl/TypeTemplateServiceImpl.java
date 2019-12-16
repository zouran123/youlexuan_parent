package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.mapper.TbTypeTemplateMapper;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.pojo.TbTypeTemplate;
import com.offcn.pojo.TbTypeTemplateExample;
import com.offcn.pojo.TbTypeTemplateExample.Criteria;
import com.offcn.sellergoods.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * type_template服务实现层
 * @author senqi
 *
 */
@Service(timeout = 3000)
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate != null){			
						if(typeTemplate.getName() != null && typeTemplate.getName().length() > 0){
				criteria.andNameLike("%" + typeTemplate.getName() + "%");
			}			if(typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0){
				criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
			}			if(typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0){
				criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
			}			if(typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0){
				criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
			}
		}
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);

		// 添加缓存：为了搜索页面的品牌、规格的显示
		List<TbTypeTemplate> typeTemplateList = null;

		// 品牌
		Set brandKeys = redisTemplate.boundHashOps("brandList").keys();
		if(brandKeys.size() > 0) {
			System.out.println(">>>>>品牌列表已缓存");
		} else {
			System.out.println(">>>>>添加品牌的缓存");
			typeTemplateList = typeTemplateMapper.selectByExample(null);
			for (TbTypeTemplate tbTypeTemplate : typeTemplateList) {
				List<Map> brandList = JSON.parseArray(tbTypeTemplate.getBrandIds(), Map.class);
				redisTemplate.boundHashOps("brandList").put(tbTypeTemplate.getId(), brandList);
			}
		}
		// 规格
		Set specKeys = redisTemplate.boundHashOps("specList").keys();
		if(specKeys.size() > 0) {
		} else {
			System.out.println(">>>>>添加规格的缓存");
			if(typeTemplateList == null) {
				typeTemplateList = typeTemplateMapper.selectByExample(null);
			}
			for (TbTypeTemplate tbTypeTemplate : typeTemplateList) {
				List<Map> specList = findSpecList(tbTypeTemplate.getId());
				redisTemplate.boundHashOps("specList").put(tbTypeTemplate.getId(), specList);
			}
		}

		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return typeTemplateMapper.selectOptionList();
	}

	/**
	 * 根据模板id获取规格，以及规格对应的选项
	 * @param typeId
	 * @return
	 */
	@Override
	public List<Map> findSpecList(Long typeId) {
		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(typeId);
		// list: [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
		// map: {"id":27,"text":"网络"}
		List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
		for (Map map : list) {
			// 根据规格id进行条件查询，得到规格选项
			TbSpecificationOptionExample ex = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria c = ex.createCriteria();
			c.andSpecIdEqualTo(Long.parseLong((int)map.get("id") + ""));
			List<TbSpecificationOption> specOptionList = specificationOptionMapper.selectByExample(ex);

			map.put("options", specOptionList);
		}
		return list;
	}



}
