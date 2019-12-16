package com.offcn.page.service;

public interface ItemPageService {

    /**
     * 生成商品详细页
     * @param goodsId
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除生成的详情页
     * @param ids
     * @return
     */
    public boolean delItemHtml(Long[] ids);

}
