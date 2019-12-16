package com.offcn.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbGoodsDesc;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pageDir}")
    private String pageDir;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private TbItemCatMapper itemCatMapper;





    @Override
    public boolean genItemHtml(Long goodsId) {
        Writer out = null;
        try{
            // 获得模板对象
            Configuration conf = freeMarkerConfigurer.getConfiguration();
            Template template = conf.getTemplate("item.ftl");

            // 组织需要传递到页面的map数据
            Map map = new HashMap();
            // 1. 根据goodsId将goods信息查询出来
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);

            // 2. 查询3级分类的名称
            String itemCat1Name = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2Name = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3Name = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();

            // 3. 查询商品介绍等信息：tb_goods_desc
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);

            // 4. 查询商品规格相关信息：tb_item
            TbItemExample ex = new TbItemExample();

            TbItemExample.Criteria c = ex.createCriteria();
            c.andGoodsIdEqualTo(goodsId);
            // 正常状态
            c.andStatusEqualTo("1");
            // 排序：将默认的排第一个
            ex.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(ex);

            map.put("goods", goods);
            map.put("itemCat1Name", itemCat1Name);
            map.put("itemCat2Name", itemCat2Name);
            map.put("itemCat3Name", itemCat3Name);
            map.put("goodsDesc", goodsDesc);
            map.put("itemList", itemList);

            // 指定输出目录及文件
            out = new FileWriter(pageDir + goodsId + ".html");
            template.process(map, out);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if(out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public boolean delItemHtml(Long[] ids) {
        try {
            for (Long id : ids) {
                File file = new File(pageDir + id + ".html");
                file.delete();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
