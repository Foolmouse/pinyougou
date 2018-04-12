package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService  {

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    public Map<String, Object> search(Map searchMap);


    /**
     * 删除数据
     * @param goodsIdList
     */
    public void deleteByGoodsIds(List goodsIdList);


    /**
     * 根据商品ID和状态查询Item表信息
     * @param goodsIds spu的ID
     * @param status   spu的状态
     * @return
     */
    //public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status );

    /**
     * 导入数据
     * @param list
     */
    public void importList(List list);
}
