package com.suplin.demo.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.suplin.demo.domain.Content;
import com.suplin.demo.domain.Doc;
import com.suplin.demo.domain.DocExample;
import com.suplin.demo.mapper.ContentMapper;
import com.suplin.demo.mapper.DocMapper;
import com.suplin.demo.mapper.DocMapperCust;
import com.suplin.demo.req.DocQueryReq;
import com.suplin.demo.req.DocSaveReq;
import com.suplin.demo.resp.CommonResp;
import com.suplin.demo.resp.DocQueryResp;
import com.suplin.demo.resp.PageResp;
import com.suplin.demo.util.CopyUtil;
import com.suplin.demo.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Service("DocMapper")
public class DocTable {
    //打印日志
    private static final Logger LOG = LoggerFactory.getLogger(DocTable.class);

    @Resource //jdk自带注解  Autowired是spring自带的
    private DocMapper docMapper;

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private DocMapperCust docMapperCust;


    public List<DocQueryResp> all(Long ebookId) {
        // 固定写法，创建XXXExample对象
        DocExample docExample = new DocExample();
        docExample.createCriteria().andEbookIdEqualTo(ebookId);
        docExample.setOrderByClause("sort asc");
        // 请求数据处理
        List<Doc> docList = docMapper.selectByExample(docExample);
        // 列表赋值
        List<DocQueryResp> list = CopyUtil.copyList(docList, DocQueryResp.class);

        return list;
    }


    public CommonResp<PageResp> list(DocQueryReq req) {
        // 固定写法，创建XXXExample对象
        DocExample docExample = new DocExample();
        docExample.setOrderByClause("sort asc");
        DocExample.Criteria criteria = docExample.createCriteria();
        if (!ObjectUtils.isEmpty(req.getName())) {
            criteria.andNameLike("%" + req.getName() + "%");
        }
        // 请求数据处理
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Doc> docList = docMapper.selectByExample(docExample);

        // 返回数据处理
        PageInfo<Doc> docPageInfo = new PageInfo<>(docList);
        LOG.info("总行数：{}", docPageInfo.getTotal());
        LOG.info("总页数：{}", docPageInfo.getPageNum());


//        ArrayList<DocResp> docResps = new ArrayList<>();
//        for (Doc doc : docList) {
//
////            DocResp docResp = new DocResp();
////            BeanUtils.copyProperties(doc,docResp);
//
//            // 单体复制
//            DocResp copylist = CopyUtil.copy(doc, DocResp.class);
//            docResps.add(copylist);
//        }

        // 列表赋值
        List<DocQueryResp> list = CopyUtil.copyList(docList, DocQueryResp.class);

        PageResp<DocQueryResp> pageResp = new PageResp();
        pageResp.setTotal(docPageInfo.getTotal());
        pageResp.setList(list);
        CommonResp commonResp = new CommonResp();
        commonResp.setContent(pageResp);

        return commonResp;

    }


    @Resource //注入雪花算法
    private SnowFlake snowFlake;

    @Transactional
    public void save(DocSaveReq req) {
        Doc doc = CopyUtil.copy(req, Doc.class);
        Content content = CopyUtil.copy(req, Content.class);

        System.out.println(req);
        if (ObjectUtils.isEmpty(req.getId())) {   //这里需要判断请求参数的id是否存在，不存在则说明需要新增
            // 新增
            doc.setId(snowFlake.nextId()); // 雪花算法
            docMapper.insert(doc);

            content.setId(doc.getId());
            contentMapper.insert(content);
        } else {
            // 修改
            docMapper.updateByPrimaryKey(doc);

            int count = contentMapper.updateByPrimaryKeyWithBLOBs(content);
            if (count == 0) {
                contentMapper.insert(content);
            }
//
//            try {
//                contentMapper.updateByPrimaryKeyWithBLOBs(content);
//            } catch (Exception e) {
//                System.out.println("报错：" + e + "\n" + "进行了插入数据操作");
//                contentMapper.insert(content);
//            }
        }
    }

    public void delete(Long id) {
        docMapper.deleteByPrimaryKey(id);
    }

    public void delete(List<String> ids) {
        // 因为某种bug，tmd很多重复的数值，需要后端去个重
        // 好像又不用去重了。。。。。不会重复了。。。。。。
        // 去重代码
        List<String> lt = ids.stream().collect(
                collectingAndThen(
                        toCollection(() -> new TreeSet<>()), ArrayList::new)
        );

        DocExample docExample = new DocExample();
        DocExample.Criteria criteria = docExample.createCriteria();
        criteria.andFatherIdIn(lt);
        docMapper.deleteByExample(docExample);
    }

    public String findContent(Long id) {
        Content content = contentMapper.selectByPrimaryKey(id);
        return content.getContent();
    }

    public void updateEbookInfo() {
        docMapperCust.updateEbookInfo();
    }
}
