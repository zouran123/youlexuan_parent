package com.offcn.cart.service;

import com.offcn.group.Cart;

import java.util.List;

public interface CartService {

    List<Cart> addGoodsToCart(List<Cart> cartList, Long skuId, Integer num);

    List<Cart> findCartListFromRedis(String userName);

    void addGoodsToRedis(List<Cart> cartList, String userName);

    List<Cart> mergeCartList(List<Cart> list1, List<Cart> list2);

}
