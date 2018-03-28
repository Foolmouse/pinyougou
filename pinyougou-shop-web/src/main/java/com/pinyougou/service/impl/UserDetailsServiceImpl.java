package com.pinyougou.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 认证类
 * @author Administrator
 *
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }


    @Override
    public UserDetails loadUserByUsername(String seller_id) throws UsernameNotFoundException {
        System.out.println("经过了UserDetailsServiceImpl");
        System.out.println(sellerService);
        //数据库验证
        TbSeller tbSeller = sellerService.findOne(seller_id);
        if(tbSeller!=null){
            // 1? 表示审核通过
            if(tbSeller.getStatus().equals("1")){
                ArrayList<GrantedAuthority> grantAuths = new ArrayList<>();
                grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));

                //security会自动封装了加了密的密码在seller中
                return new User(seller_id,tbSeller.getPassword(),grantAuths);
            }else{
                return null;
            }
        }else{
            return null;
        }

    }



}
