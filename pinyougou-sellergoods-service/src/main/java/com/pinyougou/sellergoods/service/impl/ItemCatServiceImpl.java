package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemCatExample;
import com.pinyougou.pojo.TbItemCatExample.Criteria;
import com.pinyougou.sellergoods.service.ItemCatService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbItemCat> findAll() {
        return itemCatMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbItemCat itemCat) {
        itemCatMapper.insert(itemCat);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbItemCat itemCat) {
        itemCatMapper.updateByPrimaryKey(itemCat);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbItemCat findOne(Long id) {
        return itemCatMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            List<TbItemCat> tbItemCats2 = findByParentId(id);
            if (tbItemCats2 != null) {
                for (TbItemCat tbItemCat2 : tbItemCats2) {
                    List<TbItemCat> tbItemCats3 = findByParentId(tbItemCat2.getId());
                    if (tbItemCats3 != null) {
                        for (TbItemCat tbItemCat3 : tbItemCats3) {
                            itemCatMapper.deleteByPrimaryKey(tbItemCat3.getId());
                        }
                    }
                    itemCatMapper.deleteByPrimaryKey(tbItemCat2.getId());
                }
            }
            itemCatMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbItemCatExample example = new TbItemCatExample();
        Criteria criteria = example.createCriteria();

        if (itemCat != null) {
            if (itemCat.getName() != null && itemCat.getName().length() > 0) {
                criteria.andNameLike("%" + itemCat.getName() + "%");
            }

        }

        Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据上级ID查询列表
     */
    @Override
    public List<TbItemCat> findByParentId(Long parentId) {
        TbItemCatExample example1 = new TbItemCatExample();
        Criteria criteria1 = example1.createCriteria();
        criteria1.andParentIdEqualTo(parentId);
        List<TbItemCat> list = itemCatMapper.selectByExample(example1);

        /**
         * redis老板了解一下
         */
        for (TbItemCat tbItemCat : list) {
            //为什么key是name , value是模板id
            //因为sku查询出来种类 , 根据种类得到模板id , 再得到品牌和规格
            redisTemplate.boundHashOps("itemCategory").put(tbItemCat.getName(),tbItemCat.getTypeId());
        }
        System.out.println("更新缓存:商品分类表");
        return list;
    }


}
