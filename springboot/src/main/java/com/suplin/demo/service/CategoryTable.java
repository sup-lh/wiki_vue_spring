package com.suplin.demo.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.suplin.demo.domain.Category;
import com.suplin.demo.domain.CategoryExample;
import com.suplin.demo.mapper.CategoryMapper;
import com.suplin.demo.req.CategoryQueryReq;
import com.suplin.demo.req.CategorySaveReq;
import com.suplin.demo.resp.CommonResp;
import com.suplin.demo.resp.CategoryQueryResp;
import com.suplin.demo.resp.PageResp;
import com.suplin.demo.util.CopyUtil;
import com.suplin.demo.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

@Service("CategoryMapper")
public class CategoryTable {
    //打印日志
    private static final Logger LOG = LoggerFactory.getLogger(CategoryTable.class);

    @Resource //jdk自带注解  Autowired是spring自带的
    private CategoryMapper categoryMapper;


    public List<CategoryQueryResp> all() {
        // 固定写法，创建XXXExample对象
        CategoryExample categoryExample = new CategoryExample();
        categoryExample.setOrderByClause("sort asc");
        // 请求数据处理
        List<Category> categoryList = categoryMapper.selectByExample(categoryExample);
        // 列表赋值
        List<CategoryQueryResp> list = CopyUtil.copyList(categoryList, CategoryQueryResp.class);

        return list;
    }


    public CommonResp<PageResp> list(CategoryQueryReq req) {
        // 固定写法，创建XXXExample对象
        CategoryExample categoryExample = new CategoryExample();
        categoryExample.setOrderByClause("sort asc");
        CategoryExample.Criteria criteria = categoryExample.createCriteria();
        if (!ObjectUtils.isEmpty(req.getName())) {
            criteria.andNameLike("%" + req.getName() + "%");
        }
        // 请求数据处理
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Category> categoryList = categoryMapper.selectByExample(categoryExample);

        // 返回数据处理
        PageInfo<Category> categoryPageInfo = new PageInfo<>(categoryList);
        LOG.info("总行数：{}", categoryPageInfo.getTotal());
        LOG.info("总页数：{}", categoryPageInfo.getPageNum());


//        ArrayList<CategoryResp> categoryResps = new ArrayList<>();
//        for (Category category : categoryList) {
//
////            CategoryResp categoryResp = new CategoryResp();
////            BeanUtils.copyProperties(category,categoryResp);
//
//            // 单体复制
//            CategoryResp copylist = CopyUtil.copy(category, CategoryResp.class);
//            categoryResps.add(copylist);
//        }

        // 列表赋值
        List<CategoryQueryResp> list = CopyUtil.copyList(categoryList, CategoryQueryResp.class);

        PageResp<CategoryQueryResp> pageResp = new PageResp();
        pageResp.setTotal(categoryPageInfo.getTotal());
        pageResp.setList(list);
        CommonResp commonResp = new CommonResp();
        commonResp.setContent(pageResp);

        return commonResp;

    }


    @Resource //注入雪花算法
    private SnowFlake snowFlake;


    public void save(CategorySaveReq req) {
        Category category = CopyUtil.copy(req, Category.class);
        System.out.println(req);
        if (ObjectUtils.isEmpty(req.getId())) {   //这里需要判断请求参数的id是否存在，不存在则说明需要新增
            // 新增
            category.setId(snowFlake.nextId()); // 雪花算法
            categoryMapper.insert(category);
        } else {
            // 修改
            categoryMapper.updateByPrimaryKey(category);
        }
    }

    public void delete(Long id) {
        categoryMapper.deleteByPrimaryKey(id);
    }
}
