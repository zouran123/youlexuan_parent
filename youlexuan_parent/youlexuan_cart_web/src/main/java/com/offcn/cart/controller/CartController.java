package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Result;
import com.offcn.group.Cart;
import com.offcn.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;


    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户是：" + name);

        // 从cookie中找回之前放的购物车（没找到，就返回空集合[]）
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", "utf-8");
        if(cartListStr == null || cartListStr.equals("")) {
            cartListStr = "[]";
        }
        List<Cart> cookie_cartList = JSON.parseArray(cartListStr, Cart.class);

        // 未登录
        if("anonymousUser".equals(name)) {
            return  cookie_cartList;
        }
        else {
            // 在此处，开始同步本地的cookie信息到redis
            // 首先将，本地的cookie信息取回来
            List<Cart> redis_cartList = cartService.findCartListFromRedis(name);

            if(cookie_cartList.size() > 0) {
                System.out.println(">>>>>开始合并cookie数据");
                // 合并cookie_cartList、redis_cartList
                redis_cartList = cartService.mergeCartList(redis_cartList, cookie_cartList);

                // 将新的list存回redis
                cartService.addGoodsToRedis(redis_cartList, name);

                // 将cookie的购物车清空
                CookieUtil.deleteCookie(request, response, "cartList");
            }

            return redis_cartList;
        }
    }

    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long skuId, Integer num) {

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9009");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("当前登录用户是：" + name);

            // 取回之前放的数据
            List<Cart> oldList = findCartList();
            oldList = cartService.addGoodsToCart(oldList, skuId, num);

            // 未登录
            if("anonymousUser".equals(name)) {
                // 将重新组织的cartList放入cookie中
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(oldList), 3600 * 24 * 7, "utf-8");
            }
            else {
                cartService.addGoodsToRedis(oldList, name);
            }

            return new Result(true, "添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        }


    }


}
