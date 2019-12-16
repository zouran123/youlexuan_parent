package com.offcn.solr;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring-*.xml")
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 导入数据
     */
    @Test
    public void testImportData() {
        TbItemExample ex = new TbItemExample();
        TbItemExample.Criteria c = ex.createCriteria();
        // 正常情况下的item数据
        c.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(ex);

        for (TbItem item : itemList) {
            // {"机身内存":"16G","网络":"联通2G"}
            Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
            Map<String, String> newMap = new HashMap();

            for(String key : map.keySet()) {
                newMap.put(Pinyin.toPinyin(key, "").toLowerCase(), map.get(key));
            }
            item.setSpecMap(newMap);

            System.out.println(item.getTitle() + ">>>>>" + item.getPrice() + ">>>>>" + item.getSpec());
        }

        System.out.println("===导入开始===");
        solrTemplate.saveBeans("core1", itemList);
        solrTemplate.commit("core1");
        System.out.println("===导入结束===");
    }

    /**
     * 删除全部数据
     */
    @Test
    public void testDelData() {
        Query query = new SimpleQuery("*:*");
        solrTemplate.delete("core1", query);
        solrTemplate.commit("core1");
        System.out.println("===删除结束===");
    }

}
