package com.suplin.demo.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.suplin.demo.domain.User;
import com.suplin.demo.domain.UserExample;
import com.suplin.demo.mapper.UserMapper;
import com.suplin.demo.req.UserLoginReq;
import com.suplin.demo.req.UserQueryReq;
import com.suplin.demo.req.UserResetPasswordReq;
import com.suplin.demo.req.UserSaveReq;
import com.suplin.demo.resp.PageResp;
import com.suplin.demo.resp.UserLoginResp;
import com.suplin.demo.resp.UserQueryResp;
import com.suplin.demo.util.CopyUtil;
import com.suplin.demo.util.SnowFlake;
import exception.BusinessException;
import exception.BusinessExceptionCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserTable {
    private static final Logger LOG = LoggerFactory.getLogger(UserTable.class);

    @Resource
    private UserMapper userMapper;

    @Resource
    private SnowFlake snowFlake;

    public PageResp<UserQueryResp> list(UserQueryReq req) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        if (!ObjectUtils.isEmpty(req.getLonginName())) {
            criteria.andLoginNameEqualTo(req.getLonginName());
        }
        PageHelper.startPage(req.getPage(), req.getSize());
        List<User> userList = userMapper.selectByExample(userExample);

        PageInfo<User> pageInfo = new PageInfo<>(userList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<UserQueryResp> list = CopyUtil.copyList(userList, UserQueryResp.class);

        PageResp<UserQueryResp> pageResp = new PageResp();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);

        return pageResp;
    }

    /**
     * 保存
     */
    public void save(UserSaveReq req) {
        User user = CopyUtil.copy(req, User.class);
        if (ObjectUtils.isEmpty(req.getId())) {
            // 新增
            User userDB = selectByLoginName(req.getLoginName());
            if (ObjectUtils.isEmpty(userDB)) {
                // 新增
                user.setId(snowFlake.nextId());
                userMapper.insert(user);
            } else {
                // 用户名已存在
                throw new BusinessException(BusinessExceptionCode.USER_LOGIN_NAME_EXIST);
            }
        } else {
            // 更新
            user.setLoginName(null);
            user.setPassword(null);
            userMapper.updateByPrimaryKeySelective(user);
        }
    }

    public void delete(Long id) {
        userMapper.deleteByPrimaryKey(id);
    }


    public User selectByLoginName(String LoginName) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andLoginNameEqualTo(LoginName);
        List<User> userList = userMapper.selectByExample(userExample);
        if (CollectionUtils.isEmpty(userList)) {
            return null;
        } else {
            return userList.get(0);
        }
    }


    /**
     * 保存
     */
    public void resetPassword(UserResetPasswordReq req) {
        User user = CopyUtil.copy(req, User.class);
        userMapper.updateByPrimaryKeySelective(user);
    }

    /**
     * 登录
     */
    public UserLoginResp login(UserLoginReq req) {
        User userDb = selectByLoginName(req.getLoginName());
        if (ObjectUtils.isEmpty(userDb)) {
            // 空
            LOG.info("输入的用户名不存在");
            throw new BusinessException(BusinessExceptionCode.USER_LOGIN_ERROR);
        } else {
            if (userDb.getPassword().equals(req.getPassword())) {
                // 登录成功
                UserLoginResp userLoginResp = CopyUtil.copy(userDb, UserLoginResp.class);
                return userLoginResp;
            }
            {
                // 密码不正确
                LOG.info("密码错误，输入的为{}，正确的为{}", req.getPassword(), userDb.getPassword());
                throw new BusinessException(BusinessExceptionCode.USER_LOGIN_ERROR);

            }
        }
    }
}