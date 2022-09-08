package com.suplin.demo.controller;

import com.suplin.demo.req.EbookQueryReq;
import com.suplin.demo.req.EbookSaveReq;
import com.suplin.demo.resp.CommonResp;
import com.suplin.demo.resp.PageResp;
import com.suplin.demo.service.EbookTable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

//扫描多个包
//@ComponentScan({"com.suplin.demo","com.suplin.demo2"})

//返回页面
//@Controller

//返回字符串
@RestController
public class EbookController {

    @Resource
    private EbookTable ebookTable;

    @GetMapping("/ebook/list")
    public CommonResp<PageResp> wiki_table(@Valid EbookQueryReq req) {
        return ebookTable.list(req);
    }

    @PostMapping("/ebook/save")
    public CommonResp save(@Valid @RequestBody EbookSaveReq req) {
        CommonResp<Object> objectCommonResp = new CommonResp<>();
        ebookTable.save(req);
        return objectCommonResp;
    }

    @DeleteMapping("/ebook/delete/{id}")
    public CommonResp delete(@PathVariable Long id) {  // 一定要注意得是这个PathVariable这个注解类型
        CommonResp objectCommonResp = new CommonResp<>();
        ebookTable.delete(id);
        return objectCommonResp;
    }

}
