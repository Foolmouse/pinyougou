package com.pinyougou.rest.controller;


import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.ItemService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.ReflectPermission;

//url
//参数
//返回值
//method的类型

@RestController
@RequestMapping("/api")
public class RestDemoController {

    @Reference
    private ItemService itemService;

    @Autowired
    private  RestTemplate restTemplate; //可用此调用其他接口

    @RequestMapping(value = "/item/{id}",method = RequestMethod.GET)
    public ResponseEntity<TbItem> getItem(@PathVariable Long id){

        try {
            TbItem item = itemService.findOne(id);
            return ResponseEntity.status(HttpStatus.OK).body(item);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

}
