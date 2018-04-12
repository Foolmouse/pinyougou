package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.search.service.ItemSearchService;
import com.sun.javafx.font.PrismFontFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.jca.work.SimpleTaskWorkManager;

import javax.swing.text.AbstractDocument;
import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbItemMapper itemMapper;


    /**
     * 从复制域中搜索
     * @param searchMap
     * @return
     */
/*
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
    */

    @Override
    public Map<String, Object> search(Map searchMap) {

        Map<String, Object> map = new HashMap<>();
        //1.将经过高亮的结果放入map
        map.putAll(searchList(searchMap));

        //2.根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        //3.查询品牌和规格列表
        Map brandAndSpecMap = null;
        if(!searchMap.get("category").equals("")){
            //前端传了查询分类
            brandAndSpecMap = searchBrandAndSpecList((String) searchMap.get("category"));
            System.out.println("根据前端条件,查询分类下的品牌和规格" + searchMap.get("category"));
        }else{
            //没传, 默认选择第一个
            brandAndSpecMap = searchBrandAndSpecList((String) categoryList.get(0));
            System.out.println("前端没条件, 默认查询第一个分类对应的品牌和规格" + categoryList.get(0));
        }
        map.putAll(brandAndSpecMap);
        return map;
    }

    //删除商品的同时,删除索引
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        SimpleQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").is(goodsIdList);
        query.addCriteria(criteria);

        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //查询高亮
    private Map<String,Object> searchList(Map searchMap){
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //设置高亮域 和前后缀
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);

        //1.1 关键字搜索条件
        //如果前端传的是"手机 32G" 我们需要对其进行空格处理,变成数组
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("item_keywords",keywords.replace(" ",""));

        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2 按分类删选
        if(searchMap.get("category") != null){
            //设置过滤查询条件
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            filterQuery.addCriteria(criteria);
            //加入query
            query.addFilterQuery(filterQuery);
        }

        //1.3 按品牌筛选
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4 按规格搜索
        if(searchMap.get("spec") != null){
            Map<String,String> spec = (Map) searchMap.get("spec");
            for (String specName : spec.keySet()) {
                //添加到动态域
                Criteria specCriteria = new Criteria("item_spec_" + specName).is(spec.get(specName));
                FilterQuery specQuery = new SimpleFilterQuery().addCriteria(specCriteria);
                query.addFilterQuery(specQuery);
            }

        }

        //1.5 按价格搜索
        if(!searchMap.get("price").equals("")){
            String[] prices = ((String) searchMap.get("price")).split("-");

            if(!prices[0].equals("0")){
                Criteria priceCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
                FilterQuery priceQuery = new SimpleFilterQuery().addCriteria(priceCriteria);
                query.addFilterQuery(priceQuery);
            }
            if(!prices[1].equals("*")){
                Criteria priceCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                FilterQuery priceQuery = new SimpleFilterQuery().addCriteria(priceCriteria);
                query.addFilterQuery(priceQuery);
            }

        }

        //1.6 分页查询
        //如果设置了分页查询,只对第一次查询起作用
        Integer pageNo= (Integer) searchMap.get("pageNo");//提取页码
        if(pageNo==null){
            pageNo=1;//默认第一页
        }
        Integer pageSize=(Integer) searchMap.get("pageSize");//每页记录数
        if(pageSize==null){
            pageSize=20;//默认20
        }
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        //1.7 价格排序 sort sortField
        String sort = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");
        Sort orders = null;
        if(sort != null && !sort.equals("")) {
            //判断是升序还是降序
            //选择排序字段
            if(sort.toLowerCase().equals("asc")){
                orders = new Sort(Sort.Direction.ASC, "item_" + sortField);
            }if(sort.toLowerCase().equals("desc")){
                orders = new Sort(Sort.Direction.DESC, "item_" + sortField);
            }
        }
        query.addSort(orders);

        //执行搜索
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //得到高亮结果集
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : highlightEntryList) {
            TbItem tbItem = entry.getEntity();

            List<HighlightEntry.Highlight> list = entry.getHighlights();
            if(list.size()>0 && list.get(0).getSnipplets().size()>0){
                //把title设置为高亮的
                tbItem.setTitle(list.get(0).getSnipplets().get(0));
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("rows",page.getContent());  //每页内容
        map.put("totalPages",page.getTotalPages()); //总页数
        map.put("total",page.getTotalElements());   //总条数
        return map;
    }

    /**
     * 得到得到查询结果里面的商品分类信息
     * @param searchMap
     * @return
     */
    public List searchCategoryList(Map searchMap){
        LinkedList<Object> list = new LinkedList<>();
        SimpleQuery query = new SimpleQuery();
        //构建查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //添加分组查询信息
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);

        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        for (GroupEntry<TbItem> entry : groupEntries) {
            //得到分类信息
            list.add(entry.getGroupValue());
        }
        System.out.println(list);
        return list;
    }


    private Map searchBrandAndSpecList(String categoryName){
        Map map = new HashMap<>();

        //获得模板ID
        Long categoryId = Long.valueOf( (Integer) redisTemplate.boundHashOps("itemCategory").get(categoryName) );
        System.out.println(redisTemplate.boundHashOps("itemCategory").get(categoryName));
        if(categoryId != null){
            //从redis中获取模板id对应的品牌和规格列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(categoryId);
            List specList = (List) redisTemplate.boundHashOps("specList").get(categoryId);

            System.out.println(brandList);
            System.out.println(specList);

            map.put("specList",specList);
            map.put("brandList",brandList);
        }
        System.out.println("从redis中取了...");
        return map;
    }


/*    @Override
    public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
        TbItemExample itemExample = new TbItemExample();
        TbItemExample.Criteria criteria = itemExample.createCriteria();
        criteria.andGoodsIdIn(Arrays.asList(goodsIds));
        criteria.andStatusEqualTo(status);
        List<TbItem> itemList = itemMapper.selectByExample(itemExample);

        return itemList;
    }*/

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }




}
