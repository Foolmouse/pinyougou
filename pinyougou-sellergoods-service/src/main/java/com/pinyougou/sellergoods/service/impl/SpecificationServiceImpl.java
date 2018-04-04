package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import com.pinyougou.sellergoods.service.SpecificationService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private TbSpecificationMapper specificationMapper;

    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSpecification> findAll() {
        return specificationMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Specification specification) {
        specificationMapper.insert(specification.getSpecification());

        if (specification.getSpecificationOptionList() != null && specification.getSpecificationOptionList().size() > 0) {
            //遍历子项
            for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()) {
                //设置外键
                specificationOption.setSpecId(specification.getSpecification().getId());
                //循环插入
                specificationOptionMapper.insert(specificationOption);
            }
        }

    }


    /**
     * 修改
     * 有个问题 : 更新一级对象直接update , 二级对象为什么要先删除再插入? 不直接update?
     */
    @Override
    public void update(Specification specification) {
        specificationMapper.updateByPrimaryKey(specification.getSpecification());

        //构件查询条件
        TbSpecificationOptionExample optionExample = new TbSpecificationOptionExample();
        optionExample.createCriteria().andSpecIdEqualTo(specification.getSpecification().getId());
        //先删除后insert
        specificationOptionMapper.deleteByExample(optionExample);
        List<TbSpecificationOption> optionList = specification.getSpecificationOptionList();

        for (TbSpecificationOption specificationOption : optionList) {
            //前端模型中没有外键 , 需要set
            specificationOption.setSpecId(specification.getSpecification().getId());
            specificationOptionMapper.insert(specificationOption);
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Specification findOne(Long id) {
        TbSpecification specification = specificationMapper.selectByPrimaryKey(id);

        TbSpecificationOptionExample optionExample = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = optionExample.createCriteria().andSpecIdEqualTo(id);

        List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample(optionExample);

        Specification specification1 = new Specification();
        specification1.setSpecification(specification);
        specification1.setSpecificationOptionList(specificationOptionList);

        return specification1;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            specificationMapper.deleteByPrimaryKey(id);

            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            example.createCriteria().andSpecIdEqualTo(id);
            specificationOptionMapper.deleteByExample(example);
        }
    }


    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSpecificationExample example = new TbSpecificationExample();
        TbSpecificationExample.Criteria criteria = example.createCriteria();

        if (specification != null) {

        }

        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }


    @Override
    public List<Map> selectOptionList() {
        return specificationMapper.selectOptionList();
    }

}
