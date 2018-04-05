package com.pinyougou.solrutil;

import com.alibaba.druid.filter.AutoLoad;
import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Resource
    private TbItemMapper tbItemMapper;

    @Autowired
    private SolrTemplate solrTemplate;



    public void importItemData(){
        //查出所有已审核的商品
        TbItemExample itemExample = new TbItemExample();
        itemExample.createCriteria().andStatusEqualTo("1");
        List<TbItem> itemList = tbItemMapper.selectByExample(itemExample);
        System.out.println("===商品列表===");
        for(TbItem item:itemList){
            Map map = JSON.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(map);
            System.out.println(item.getTitle());
        }

        System.out.println("===结束===");
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.importItemData();
    }

}
