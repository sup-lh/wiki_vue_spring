package com.suplin.demo.controller;

import com.suplin.demo.domain.Doc;
import com.suplin.demo.domain.DocExample;
import com.suplin.demo.mapper.DocMapper;
import com.suplin.demo.mapper.DocMapperCust;
import com.suplin.demo.req.DocQueryReq;
import com.suplin.demo.req.DocSaveReq;
import com.suplin.demo.resp.CommonResp;
import com.suplin.demo.resp.DocQueryResp;
import com.suplin.demo.resp.PageResp;
import com.suplin.demo.service.DocTable;
import com.suplin.demo.service.WsService;
import com.suplin.demo.util.RedisUtil;
import com.suplin.demo.util.RequestContext;
import com.suplin.demo.websocket.WebSocketServer;
import exception.BusinessException;
import exception.BusinessExceptionCode;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

//扫描多个包
//@ComponentScan({"com.suplin.demo","com.suplin.demo2"})

//返回页面
//@Controller

//返回字符串
@RestController
public class DocController {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private WsService wsService;

    @Resource
    private DocMapper docMapper;


    @Resource
    public RedisUtil redisUtil;

    @Resource
    private DocTable docTable;

    @Resource
    private DocMapperCust docMapperCust;

    @Resource
    private WebSocketServer webSocketServer;

    @GetMapping("/doc/allDoc/{ebookId}")
    public CommonResp all(@PathVariable Long ebookId) {
        CommonResp<List<DocQueryResp>> commonResp = new CommonResp<>();
        List<DocQueryResp> all = docTable.all(ebookId);
        commonResp.setContent(all);
        return commonResp;
    }


    @GetMapping("/doc")
    public CommonResp<PageResp> wiki_table(@Valid DocQueryReq req) {
        return docTable.list(req);
    }

    @PostMapping("/doc/saveDoc")
    public CommonResp save(@Valid @RequestBody DocSaveReq req) {
        CommonResp<Object> objectCommonResp = new CommonResp<>();
        req.setViewCount(0);
        req.setVoteCount(0);
        docTable.save(req);
        return objectCommonResp;
    }

    @DeleteMapping("/doc/deleteDoc/{idsStr}")
    public CommonResp delete(@PathVariable String idsStr) {  // 一定要注意得是这个PathVariable这个注解类型
        System.out.println("删除列表: " + idsStr);
        CommonResp objectCommonResp = new CommonResp<>();
        List<String> strings = Arrays.asList(idsStr.split(","));
        System.out.println("隔开后的参数 " + strings);
        docTable.delete(strings);
        return objectCommonResp;
    }

    @GetMapping("/doc/findContent/{id}")
    public CommonResp findContent(@PathVariable Long id) {
        CommonResp<String> objectCommonResp = new CommonResp<>();

        // 文档阅读数加一
        docMapperCust.increaseViewCount(id);

        String content = docTable.findContent(id);
        objectCommonResp.setContent(content);
        return objectCommonResp;
    }


    @GetMapping("/doc/vote/{id}")
    public CommonResp vote(@PathVariable Long id) {
        CommonResp commonResp = new CommonResp();

        String ip = RequestContext.getRemoteAddr();
        if (redisUtil.validateRepeat("DOC_VOTE_" + id + "_" + ip, 5000)) {
            docMapperCust.increaseVoteCount(id);
        } else {
            throw new BusinessException(BusinessExceptionCode.VOTE_REPEAT);
        }

        DocExample docExample = new DocExample();
        DocExample.Criteria criteria = docExample.createCriteria();
        criteria.andIdEqualTo(id);
        List<Doc> docs = docMapper.selectByExample(docExample);
        String logId = MDC.get("LOG_ID");
        wsService.sendInfo("[" + docs.get(0).getName() + "]被点赞！", logId);
//        rocketMQTemplate.convertAndSend("VOTE_TOPIC","[" + docs.get(0).getName() + "]被点赞！");
        System.out.println("[" + docs.get(0).getName() + "]被点赞！");

        return commonResp;

    }

}
