package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.group.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;



    @Override
    public List<Cart> addGoodsToCart(List<Cart> cartList, Long skuId, Integer num) {
        // Cart类  sellerId  sellerName   orderItemList
        // 页面最终需要：List<Cart>
        TbItem item = itemMapper.selectByPrimaryKey(skuId);
        if(item == null) {
            throw new RuntimeException("该商品不存在");
        }
        if(!item.getStatus().equals("1")) {
            throw new RuntimeException("该商品暂不支持购买");
        }

        // 检查是否存在该店铺
        Cart cart = searchCartBySellerId(cartList, item.getSellerId());

        // 不存在商家
        if(cart == null) {
            cart = new Cart();
            cart.setSellerId(item.getSellerId());
            cart.setSellerName(item.getSeller());

            List<TbOrderItem> orderItemList = new ArrayList<>();
            // 私有方法：根据参数，创建一个orderItem对象
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            cartList.add(cart);
        }
        // 存在商家
        else {
            // 检查传递过来的商品 是否在 该店铺 中存在
            TbOrderItem orderItem = searchOrderItemBySkuId(cart.getOrderItemList(), skuId);

            if(orderItem == null) {
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));
                // 购物车数量减少的情况，需要考虑减到0了
                if(orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                if(cart.getOrderItemList().size() <= 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    @Override
    public List<Cart> findCartListFromRedis(String userName) {
        List<Cart> cartList = (List<Cart>)redisTemplate.boundHashOps("cartList").get(userName);

        if(cartList == null) {
            cartList = new ArrayList();
        }

        return cartList;
    }

    @Override
    public void addGoodsToRedis(List<Cart> cartList, String userName) {
        redisTemplate.boundHashOps("cartList").put(userName, cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> list1, List<Cart> list2) {
        // list2 :  对应的是cookie中的购物车
        // list1 ： 对应的是redis中的购物车
        for (Cart cart : list2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                list1 = addGoodsToCart(list1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return list1;
    }

    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setNum(num);
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * num));
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }

    private TbOrderItem searchOrderItemBySkuId(List<TbOrderItem> orderItemList, Long skuId) {
        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId().longValue() == skuId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }

    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if(cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }
}
