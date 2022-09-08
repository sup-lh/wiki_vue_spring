package com.suplin.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.suplin.demo.req.UserLoginReq;
import com.suplin.demo.req.UserQueryReq;
import com.suplin.demo.req.UserResetPasswordReq;
import com.suplin.demo.req.UserSaveReq;
import com.suplin.demo.resp.CommonResp;
import com.suplin.demo.resp.PageResp;
import com.suplin.demo.resp.UserLoginResp;
import com.suplin.demo.resp.UserQueryResp;
import com.suplin.demo.service.UserTable;
import com.suplin.demo.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

//扫描多个包
//@ComponentScan({"com.suplin.demo","com.suplin.demo2"})

//返回页面
//@Controller

//返回字符串
@RestController
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);


    @Resource
    private UserTable userTable;


    // 雪花算法，用来生成token
    @Resource
    private SnowFlake snowFlake;

    // redis
    @Resource
    private RedisTemplate redisTemplate;


    @GetMapping("/user/list")
    public CommonResp list(@Valid UserQueryReq req) {
        CommonResp<PageResp<UserQueryResp>> resp = new CommonResp<>();
        PageResp<UserQueryResp> list = userTable.list(req);
        resp.setContent(list);
        return resp;
    }

    @PostMapping("/user/saveUser")
    public CommonResp save(@Valid @RequestBody UserSaveReq req) {
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        CommonResp<Object> objectCommonResp = new CommonResp<>();
        userTable.save(req);
        return objectCommonResp;
    }

    @DeleteMapping("/user/deleteUser/{id}")
    public CommonResp delete(@PathVariable Long id) {  // 一定要注意得是这个PathVariable这个注解类型
        CommonResp objectCommonResp = new CommonResp<>();
        userTable.delete(id);
        return objectCommonResp;
    }

    @GetMapping("/user/logout/{token}")
    public CommonResp logout(@PathVariable String token) {  // 一定要注意得是这个PathVariable这个注解类型
        CommonResp objectCommonResp = new CommonResp<>();
        redisTemplate.delete(token);
        LOG.info("从redis中删除token：{}", token);
        return objectCommonResp;
    }

    @PostMapping("/user/reset-password")
    public CommonResp resetPassword(@Valid @RequestBody UserResetPasswordReq req) {
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        CommonResp resp = new CommonResp<>();
        userTable.resetPassword(req);
        return resp;
    }


    @PostMapping("/user/login")
    public CommonResp login(@Valid @RequestBody UserLoginReq req) {
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        CommonResp resp = new CommonResp<>();
        UserLoginResp login = userTable.login(req);

        Long token = snowFlake.nextId();
        LOG.info("生成单点登录token：{}，并放入redis中", token);
        login.setToken(token.toString());
        redisTemplate.opsForValue().set(token.toString(), JSONObject.toJSONString(login), 3600 * 24, TimeUnit.SECONDS);

        resp.setContent(login);
        return resp;
    }


    @RequestMapping("/redis/set/{key}/{value}")
    public String set(@PathVariable Long key, @PathVariable String value) {
        redisTemplate.opsForValue().set(key, value, 3600, TimeUnit.SECONDS);
        LOG.info("key: {}, value: {}", key, value);
        return "success";
    }

    @RequestMapping("/redis/get/{key}")
    public Object get(@PathVariable Long key) {
        Object object = redisTemplate.opsForValue().get(key);
        LOG.info("key: {}, value: {}", key, object);
        return object;
    }


}
