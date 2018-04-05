package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    /**
     * 从复制域中搜索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map searchMap) {
        //key是keywords , value 是值
        Query query = new SimpleQuery();
        //指定搜索 复制域
        Criteria criteria = new Criteria("item_keywords");
        //is是分词搜索 , contain 不会分词
        criteria.is(searchMap.get("keywords"));

        query.addCriteria(criteria);

        //query.setOffset(20);//开始索引（默认0）
        //query.setRows(20);//每页记录数(默认10)

        //分页搜索,根据@Field注解自动对应类属性
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);

        Map<String, Object> map = new HashMap<>();
        map.put("rows",page.getContent());
        return map;
    }
}
