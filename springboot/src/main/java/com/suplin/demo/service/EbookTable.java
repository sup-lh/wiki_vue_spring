package com.suplin.demo.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.suplin.demo.domain.Ebook;
import com.suplin.demo.domain.EbookExample;
import com.suplin.demo.mapper.EbookMapper;
import com.suplin.demo.req.EbookQueryReq;
import com.suplin.demo.req.EbookSaveReq;
import com.suplin.demo.resp.CommonResp;
import com.suplin.demo.resp.EbookQueryResp;
import com.suplin.demo.resp.PageResp;
import com.suplin.demo.util.CopyUtil;
import com.suplin.demo.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

@Service("EbookMapper")
public class EbookTable {
    //打印日志
    private static final Logger LOG = LoggerFactory.getLogger(EbookTable.class);

    @Resource //jdk自带注解  Autowired是spring自带的
    private EbookMapper ebookMapper;

    public CommonResp<PageResp> list(EbookQueryReq req) {
        // 固定写法，创建XXXExample对象
        EbookExample ebookExample = new EbookExample();
        EbookExample.Criteria criteria = ebookExample.createCriteria();
        if (!ObjectUtils.isEmpty(req.getBookName())) {
            criteria.andBookNameLike("%" + req.getBookName() + "%");
        }
        if (!ObjectUtils.isEmpty(req.getCategory2Id())) {
            criteria.andCategory2IdEqualTo(req.getCategory2Id());
        }
        if (!ObjectUtils.isEmpty(req.getCategory1Id())) {
            criteria.andCategory1IdEqualTo(req.getCategory1Id());
        }
        // 请求数据处理
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Ebook> ebookList = ebookMapper.selectByExample(ebookExample);

        // 返回数据处理
        PageInfo<Ebook> ebookPageInfo = new PageInfo<>(ebookList);
        LOG.info("总行数：{}", ebookPageInfo.getTotal());
        LOG.info("总页数：{}", ebookPageInfo.getPageNum());


//        ArrayList<EbookResp> ebookResps = new ArrayList<>();
//        for (Ebook ebook : ebookList) {
//
////            EbookResp ebookResp = new EbookResp();
////            BeanUtils.copyProperties(ebook,ebookResp);
//
//            // 单体复制
//            EbookResp copylist = CopyUtil.copy(ebook, EbookResp.class);
//            ebookResps.add(copylist);
//        }

        // 列表赋值
        List<EbookQueryResp> list = CopyUtil.copyList(ebookList, EbookQueryResp.class);

        PageResp<EbookQueryResp> pageResp = new PageResp();
        pageResp.setTotal(ebookPageInfo.getTotal());
        pageResp.setList(list);
        CommonResp commonResp = new CommonResp();
        commonResp.setContent(pageResp);

        return commonResp;

    }


    @Resource //注入雪花算法
    private SnowFlake snowFlake;


    public void save(EbookSaveReq req) {
        Ebook ebook = CopyUtil.copy(req, Ebook.class);
        System.out.println(req);
        if (ObjectUtils.isEmpty(req.getId())) {   //这里需要判断请求参数的id是否存在，不存在则说明需要新增
            // 新增
            ebook.setId(snowFlake.nextId()); // 雪花算法
            ebookMapper.insert(ebook);
        } else {
            // 修改
            ebookMapper.updateByPrimaryKey(ebook);
        }
    }

    public void delete(Long id) {
        ebookMapper.deleteByPrimaryKey(id);
    }
}
