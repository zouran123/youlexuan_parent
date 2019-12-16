package com.offcn.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("经过自定义认证类>>>>>" + username);
        // 目的：查询数据库，判断该用户的用户名、密码是否正确
        // 思考：该方法只给你了一个用户名
        // 问题剖析：我们一直在做的登录，其实是不靠谱的！

        List<GrantedAuthority> list = new ArrayList();
        list.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        TbSeller seller = sellerService.findOne(username);
        if(seller != null) {
            // 参1：用户名
            // 参2：密码
            // 参3：角色
            if(seller.getStatus().equals("1")) {
                return new User(username, seller.getPassword(), list);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
