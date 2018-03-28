package com.pinyougou.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/seller")
@RestController
public class SellerController {

    @Reference
    private SellerService sellerService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @RequestMapping("add")
    public Result add(@RequestBody TbSeller seller){
        System.out.println(seller);
        try {
            //加密密码
            seller.setPassword(passwordEncoder.encode(seller.getPassword()));
            sellerService.add(seller);
            return new Result(true,null);
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"注册失败");
        }
    }

    @RequestMapping("test")
    public void test(){
        System.out.println("hello ?>>>>>>>>>>>>>>>>");
    }

}
