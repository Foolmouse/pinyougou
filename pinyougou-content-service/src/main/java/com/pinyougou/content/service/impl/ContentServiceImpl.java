package com.pinyougou.content.service.impl;
import java.util.List;

import com.pinyougou.constant.RedisConstant;
import com.pinyougou.content.service.ContentService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
    private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
	    //考虑到用户可能会修改广告的分类，这样需要把原分类的缓存和新分类的缓存都清除掉。

        TbContent oldContent = contentMapper.selectByPrimaryKey(content.getId());
        //删除原分类
        redisTemplate.boundHashOps(RedisConstant.REDIS_CONTENT_CACHE).delete(oldContent.getCategoryId());

        if(oldContent.getCategoryId() != content.getCategoryId()){
            //如果分类改变
            //删除新分类
            redisTemplate.boundHashOps(RedisConstant.REDIS_CONTENT_CACHE).delete(content.getCategoryId());
        }
		contentMapper.updateByPrimaryKey(content);
	}
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
     * 删除广告后清除缓存
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
		    //先查出来要删的广告是属于什么类型的 , 因为我们是根据类型存的
            TbContent content = contentMapper.selectByPrimaryKey(id);

            try {
                redisTemplate.boundHashOps(RedisConstant.REDIS_CONTENT_CACHE).delete(content.getCategoryId());
            } catch (Exception e) {
                System.out.println("删除缓存出错");
                e.printStackTrace();
            }

            contentMapper.deleteByPrimaryKey(id);
		}


	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getContent()!=null && content.getContent().length()>0){
				criteria.andContentLike("%"+content.getContent()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
        Object cacheList = null;
        try {
	        //先从redis中查找
            //缓存的结构:   key: content >>> value: hash <categoryId , list<content>
            cacheList = redisTemplate.boundHashOps(RedisConstant.REDIS_CONTENT_CACHE).get(categoryId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(cacheList == null){
            //如果redis中没有缓存
            System.out.println("redis中没有缓存");
            TbContentExample example = new TbContentExample();
            Criteria criteria = example.createCriteria();
            //按类型查找
            criteria.andCategoryIdEqualTo(categoryId);
            //开启状态
            criteria.andStatusEqualTo("1");
            //排序
            example.setOrderByClause("sort_order");
            List<TbContent> list = contentMapper.selectByExample(example);
            //存入缓存
            try {
                redisTemplate.boundHashOps(RedisConstant.REDIS_CONTENT_CACHE).put(categoryId,list);
                System.out.println("存入缓存了");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }else{
            //redis中有缓存
            System.out.println(cacheList);
            return (List<TbContent>) cacheList;
        }
    }




}
