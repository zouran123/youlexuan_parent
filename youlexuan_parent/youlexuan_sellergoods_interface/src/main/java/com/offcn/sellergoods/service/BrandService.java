package com.offcn.sellergoods.service;

import com.offcn.entity.PageResult;
import com.offcn.pojo.TbBrand;
import java.util.List;
import java.util.Map;

public interface BrandService {

    List<TbBrand> findAll();

    PageResult findPage(int pageNum, int pageSize);

    PageResult findPage(TbBrand brand, int pageNum, int pageSize);

    void add(TbBrand brand);

    void update(TbBrand brand);

    TbBrand findOne(Long id);

    void delete(Long[] ids);

    List<Map> findBrandOptionList();
}
