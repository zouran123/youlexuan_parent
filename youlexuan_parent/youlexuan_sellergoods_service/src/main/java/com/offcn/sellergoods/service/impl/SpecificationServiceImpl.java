package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Specification;
import com.offcn.mapper.TbSpecificationMapper;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationExample;
import com.offcn.pojo.TbSpecificationExample.Criteria;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * specification服务实现层
 * @author senqi
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;


	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		specificationMapper.insert(specification.getSpecification());

		for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {
			// 设置外键关联
			tbSpecificationOption.setSpecId(specification.getSpecification().getId());

			specificationOptionMapper.insert(tbSpecificationOption);
		}

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){

		specificationMapper.updateByPrimaryKey(specification.getSpecification());

		// 先将该规格下面对应的规格选项全部删除
		TbSpecificationOptionExample ex = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria c = ex.createCriteria();
		c.andSpecIdEqualTo(specification.getSpecification().getId());
		specificationOptionMapper.deleteByExample(ex);

		// 然后再重新插入
		for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {
			// 设置外键关联
			tbSpecificationOption.setSpecId(specification.getSpecification().getId());
			specificationOptionMapper.insert(tbSpecificationOption);
		}

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		// 查询id对应的规格
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);

		// 该规格对应的规格选项
		TbSpecificationOptionExample ex = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria c = ex.createCriteria();
		c.andSpecIdEqualTo(id);
		List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(ex);

		Specification spec = new Specification();
		spec.setSpecification(tbSpecification);
		spec.setSpecificationOptionList(tbSpecificationOptions);

		return spec;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			// 删除规格
			specificationMapper.deleteByPrimaryKey(id);

			// 删除规格对应的选项
			TbSpecificationOptionExample ex = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria c = ex.createCriteria();
			c.andSpecIdEqualTo(id);
			specificationOptionMapper.deleteByExample(ex);

		}
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification != null){			
						if(specification.getSpecName() != null && specification.getSpecName().length() > 0){
				criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
			}
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> findSpecOptionList() {
		return specificationMapper.findSpecOptionList();
	}

}
