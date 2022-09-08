package com.suplin.demo.controller;

import com.suplin.demo.req.CategoryQueryReq;
import com.suplin.demo.req.CategorySaveReq;
import com.suplin.demo.resp.CategoryQueryResp;
import com.suplin.demo.resp.CommonResp;
import com.suplin.demo.resp.PageResp;
import com.suplin.demo.service.CategoryTable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

//扫描多个包
//@ComponentScan({"com.suplin.demo","com.suplin.demo2"})

//返回页面
//@Controller

//返回字符串
@RestController
public class CategoryController {

    @Resource
    private CategoryTable categoryTable;

    @GetMapping("/category/allCategory")
    public CommonResp all() {
        CommonResp<List<CategoryQueryResp>> commonResp = new CommonResp<>();
        List<CategoryQueryResp> all = categoryTable.all();
        commonResp.setContent(all);
        return commonResp;
    }


    @GetMapping("/category")
    public CommonResp<PageResp> wiki_table(@Valid CategoryQueryReq req) {
        return categoryTable.list(req);
    }

    @PostMapping("/category/saveCategory")
    public CommonResp save(@Valid @RequestBody CategorySaveReq req) {
        CommonResp<Object> objectCommonResp = new CommonResp<>();
        categoryTable.save(req);
        return objectCommonResp;
    }

    @DeleteMapping("/category/deleteCategory/{id}")
    public CommonResp delete(@PathVariable Long id) {  // 一定要注意得是这个PathVariable这个注解类型
        CommonResp objectCommonResp = new CommonResp<>();
        categoryTable.delete(id);
        return objectCommonResp;
    }

}
