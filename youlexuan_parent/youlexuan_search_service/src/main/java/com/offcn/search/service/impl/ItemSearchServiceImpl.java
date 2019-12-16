package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 查询主方法
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String, Object> map = new HashMap<>();

        String keywords = (String)searchMap.get("keywords");
        keywords = keywords.replaceAll(" ", "");
        searchMap.put("keywords", keywords);

        hiSearch(searchMap, map);

        categorySearch(searchMap, map);

        if(!"".equals(searchMap.get("category"))) {
            brandAndSpecSearch((String)searchMap.get("category"), map);
        } else {
            // 默认使用第一个分类，查询其对应的品牌、规格
            List<String> categoryList = (List<String>)map.get("categoryList");
            if(categoryList.size() > 0) {
                brandAndSpecSearch(categoryList.get(0), map);
            }
        }

        return map;
    }

    @Override
    public void importData(List<TbItem> list) {
        for (TbItem item : list) {
            // {"机身内存":"16G","网络":"联通2G"}
            Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
            Map<String, String> newMap = new HashMap();

            for(String key : map.keySet()) {
                newMap.put(Pinyin.toPinyin(key, "").toLowerCase(), map.get(key));
            }
            item.setSpecMap(newMap);
        }
        System.out.println("===导入solr库开始===");
        solrTemplate.saveBeans("core1", list);
        solrTemplate.commit("core1");
        System.out.println("===导入solr库结束===");
    }


    @Override
    public void deleteData(Long[] ids) {
        Query query = new SimpleQuery();
        Criteria c = new Criteria("item_goodsid").in(ids);
        query.addCriteria(c);

        solrTemplate.delete("core1", query);
        solrTemplate.commit("core1");
    }


    /**
     * 根据分类名称查询品牌、规格列表
     * @param categoryName
     * @param map
     */
    private void brandAndSpecSearch(String categoryName, Map<String, Object> map) {

        Long typeId = (Long)redisTemplate.boundHashOps("itemCat").get(categoryName);

        List<Map> brandList = null, specList = null;

        if(typeId != null) {
            brandList = (List<Map>)redisTemplate.boundHashOps("brandList").get(typeId);
            specList = (List<Map>)redisTemplate.boundHashOps("specList").get(typeId);
        }

        map.put("brandList", brandList);
        map.put("specList", specList);
    }

    /**
     * 根据检索的关键字查询分类：考虑分组
     * @param searchMap
     * @param map
     */
    private void categorySearch(Map searchMap, Map<String, Object> map) {
        List<String> list = new ArrayList<>();

        Query query = new SimpleQuery("*:*");

        // 关键字
        Criteria c = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(c);

        // 添加分组信息
        GroupOptions group = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(group);

        GroupPage<TbItem> page = solrTemplate.queryForGroupPage("core1", query, TbItem.class);
        GroupResult<TbItem> item_category = page.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> groupEntries = item_category.getGroupEntries();

        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> g : content) {
           list.add( g.getGroupValue());
        }

        map.put("categoryList", list);
    }


    /**
     * 高亮、关键字、分类、品牌、规格、价格、分页、排序查询
     * @param searchMap
     * @param map
     */
    private void hiSearch(Map searchMap, Map<String, Object> map) {
        HighlightQuery query = new SimpleHighlightQuery();

        // 1. 关键字 + 高亮查询
        // 给query查询对象设置高亮属性
        HighlightOptions options = new HighlightOptions().addField("item_title");
        options.setSimplePrefix("<span style='color:red'>");
        options.setSimplePostfix("</span>");
        query.setHighlightOptions(options);
        // 关键字查询， 如页面传递过来：手机
        Criteria c = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(c);

        // 2. 分类条件
        if(!"".equals(searchMap.get("category"))) {
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery categoryFq = new SimpleFilterQuery(categoryCriteria);
            query.addFilterQuery(categoryFq);
        }

        // 3. 品牌条件
        if(!"".equals(searchMap.get("brand"))) {
            Criteria brandCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery brandFq = new SimpleFilterQuery(brandCriteria);
            query.addFilterQuery(brandFq);
        }

        // 4. 规格条件
        if(null != searchMap.get("spec")) {
            // {"网络":"电信2G","机身内存":"128G"}
            Map<String, String> spec = (Map)searchMap.get("spec");

            for(String key : spec.keySet()) {
                // 将key转为拼音
                Criteria specC = new Criteria("item_spec_" + Pinyin.toPinyin(key,"").toLowerCase()).is(spec.get(key));
                FilterQuery specFq = new SimpleFilterQuery(specC);
                query.addFilterQuery(specFq);
            }
        }

        // 5. 价格
        if(!"".equals(searchMap.get("price"))) {
            // 0-500，3000-*
            String[] prices = ((String) searchMap.get("price")).split("-");

            // 低值
            Criteria minPriceCri = new Criteria("item_price").greaterThanEqual(prices[0]);
            FilterQuery minPriceFq = new SimpleFilterQuery(minPriceCri);
            query.addFilterQuery(minPriceFq);

            if(!prices[1].equals("*")) {
                // 高值
                Criteria maxPriceCri = new Criteria("item_price").lessThanEqual(prices[1]);
                FilterQuery maxPriceFq = new SimpleFilterQuery(maxPriceCri);
                query.addFilterQuery(maxPriceFq);
            }
        }

        // 6. 分页
        // 当前页pageNo, 每页容量pageSize
        Integer pageNo = (Integer)searchMap.get("pageNo");
        Integer pageSize = (Integer)searchMap.get("pageSize");

        // 假设每页显示5条
        // 1页：0 1 2 3 4
        // 2页：5 6 7 8 9
        // 3页：10 11 12 13 14
        query.setOffset((pageNo - 1) * pageSize);
        query.setRows(pageSize);

        // 7. 排序
        String sortName = (String)searchMap.get("sortName");
        String sortVal = (String)searchMap.get("sortVal");
        if(!"".equals(sortName)) {
            Sort sort = null;
            if("asc".equals(sortVal)) {
                sort = new Sort(Sort.Direction.ASC, "item_" + sortName);
            } else if("desc".equals(sortVal)) {
                sort = new Sort(Sort.Direction.DESC, "item_" + sortName);
            }
            query.addSort(sort);
        }

        // 获取高亮结果，并替换
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage("core1", query, TbItem.class);
        List<HighlightEntry<TbItem>> hList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : hList) {
            TbItem item = entry.getEntity();

            if(entry.getHighlights().size() > 0 && entry.getHighlights().get(0).getSnipplets().size() > 0) {
                item.setTitle(entry.getHighlights().get(0).getSnipplets().get(0));
            }

        }

        List<TbItem> itemList = page.getContent();

        // 总页数
        map.put("totalPages", page.getTotalPages());
        // 总记录数
        map.put("total", page.getTotalElements());
        map.put("rows", itemList);
    }

}
