package com.pinyougou.sellergoods.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import entity.Goods;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbSellerMapper sellerMapper;
    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbGoods goods) {
        goodsMapper.insert(goods);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbGoods goods) {
        goodsMapper.updateByPrimaryKey(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbGoods findOne(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            goodsMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }
/*
    @Override
    public void add(Goods goods) {

        //设置未申请状态
        goods.getGoods().setAuditStatus("0");

        goodsMapper.insert(goods.getGoods());//插入商品表

        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//设置商品描述表外键
        goodsDescMapper.insert(goods.getGoodsDesc());//商品描述

        //sku表
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            List<TbItem> list = goods.getItemList();
            for (TbItem item : list) {
                String title = goods.getGoods().getGoodsName();
                //parseObject?
                //每个规格都加入title中
                Map specMap = JSON.parseObject(item.getSpec());
                for (Object key : specMap.keySet()) {
                    title += "" + specMap.get(key);
                }
                item.setTitle(title);
                item.setGoodsId(goods.getGoods().getId());//spu号
                item.setSellerId(goods.getGoods().getSellerId());//商家id
                item.setCategoryid(goods.getGoods().getCategory3Id());//3级标题

                item.setCreateTime(new Date());//创建时间
                item.setUpdateTime(new Date());//更新时间

                TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
                item.setBrand(brand.getName());//品牌名称

                TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
                item.setCategory(itemCat.getName());//分类名称

                TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
                item.setSeller(seller.getName());//商家名称

                //设置图片url , 取第一张
                List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
                if (imageList.size() > 0) {
                    item.setImage((String) imageList.get(0).get("url"));
                }
                //插入sku
                itemMapper.insert(item);
            }
        }
        */


        @Override
        public void add (Goods goods){
            goods.getGoods().setAuditStatus("0");
            goodsMapper.insert(goods.getGoods());    //插入商品表
            goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
            goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展数据
            if ("1".equals(goods.getGoods().getIsEnableSpec())) {
                //启用规格, 有多个sku
                for (TbItem item : goods.getItemList()) {
                    //标题
                    String title = goods.getGoods().getGoodsName();
                    Map<String, Object> specMap = JSON.parseObject(item.getSpec());
                    for (String key : specMap.keySet()) {
                        title += "" + specMap.get(key);
                    }
                    item.setTitle(title);
                    setItemValues(goods, item);
                    itemMapper.insert(item);
                }
            } else {
                //不启用规格,表示是单一规格
                TbItem item = new TbItem();
                item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
                item.setPrice(goods.getGoods().getPrice());//价格
                item.setStatus("1");//状态
                item.setIsDefault("1");//是否默认
                item.setNum(99999);//库存数量
                item.setSpec("{}");
                setItemValues(goods, item);
                itemMapper.insert(item);
            }
        }

    /**
     * 将增加sku时的通用设置抽取出来
     * @param goods
     * @param item
     */
    private void setItemValues (Goods goods, TbItem item){
            item.setGoodsId(goods.getGoods().getId());//商品SPU编号
            item.setSellerId(goods.getGoods().getSellerId());//商家编号
            item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号（3级）
            item.setCreateTime(new Date());//创建日期
            item.setUpdateTime(new Date());//修改日期

            //品牌名称
            TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
            item.setBrand(brand.getName());
            //分类名称
            TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
            item.setCategory(itemCat.getName());

            //商家名称
            TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
            item.setSeller(seller.getNickName());

            //图片地址（取spu的第一个图片）
            List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
            if (imageList.size() > 0) {
                item.setImage((String) imageList.get(0).get("url"));
        }


    }


}
