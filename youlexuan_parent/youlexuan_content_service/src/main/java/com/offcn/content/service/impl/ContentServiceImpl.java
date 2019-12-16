package com.offcn.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.content.service.ContentService;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbContentMapper;
import com.offcn.pojo.TbContent;
import com.offcn.pojo.TbContentExample;
import com.offcn.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * content服务实现层
 * @author senqi
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//清除缓存
		redisTemplate.boundHashOps("contentList").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){

		// 在修改之前将老的分类id获取出来
		Long catId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		redisTemplate.boundHashOps("contentList").delete(catId);

		contentMapper.updateByPrimaryKey(content);

		if(catId.longValue() != content.getCategoryId().longValue()) {
			redisTemplate.boundHashOps("contentList").delete(content.getCategoryId());
		}
	}
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//先清除缓存
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();//广告分类ID
			redisTemplate.boundHashOps("contentList").delete(categoryId);

			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content != null){			
						if(content.getTitle() != null && content.getTitle().length() > 0){
				criteria.andTitleLike("%" + content.getTitle() + "%");
			}			if(content.getUrl() != null && content.getUrl().length() > 0){
				criteria.andUrlLike("%" + content.getUrl() + "%");
			}			if(content.getPic() != null && content.getPic().length() > 0){
				criteria.andPicLike("%" + content.getPic() + "%");
			}			if(content.getStatus() != null && content.getStatus().length() > 0){
				criteria.andStatusLike("%" + content.getStatus() + "%");
			}
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 针对查询广告的方法添加缓存
	 * @param catId
	 * @return
	 */
	@Override
	public List<TbContent> findByCategoryId(Long catId) {
		// 先从缓存中查
		// 查到了，直接返回
		// 没查到，去数据库查，然后再将本次的数据放入缓存
		List<TbContent> contentList = (List<TbContent>)redisTemplate.boundHashOps("contentList").get(catId);
		if(contentList == null) {
			System.out.println("从数据库中获取广告列表>>>>>");
			TbContentExample ex = new TbContentExample();
			Criteria c = ex.createCriteria();
			c.andCategoryIdEqualTo(catId);
			// 1表示启用
			c.andStatusEqualTo("1");
			ex.setOrderByClause("sort_order");
			contentList = contentMapper.selectByExample(ex);
			// 放入缓存
			redisTemplate.boundHashOps("contentList").put(catId, contentList);
		} else {
			System.out.println("从缓存中获取广告列表>>>>>");
		}

		return contentList;
	}

}
