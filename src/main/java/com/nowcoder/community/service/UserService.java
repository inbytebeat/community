package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 项目域名
     */
    @Value("${community.path.domain}")
    private String domain;

    /**
     * 项目名即项目的应用路径
     */
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String,Object> register(User user) {
        Map<String,Object> map = new HashMap<>();

        // 空值处理
        if(user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 如果对象传进来了，但是用户名为空 我们直接返回错误信息
        if(StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }
        
        //验证账号通过用户名
        User u = userMapper.selectByName(user.getUsername());
        if(u != null) {
            map.put("usernameMsg","该账户已存在");
            return map;
        }
        //验证账号通过邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null) {
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }

        // 如果上述条件都满足通过，那么可以将该用户数据存入即注册
        // 生成盐值
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        // 将用户输入的密码进行加密
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        // 用户类型默认为0 普通用户
        user.setType(0);
        // 用户状态默认未激活-0
        user.setStatus(0);
        // 用户激活码生成
        user.setActivationCode(CommunityUtil.generateUUID());
        // 用户随机头像 使用的是牛客网提供的1000个随即头像 实例 http://images.nowcoder.com/head/1t.png 表示1号头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        // 用户创建时间
        user.setCreateTime(new Date());

        //使用模板发送激活邮件
        Context context = new Context();
        // 设置模板内容
        context.setVariable("email",user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        // 选择模板位置，注入内容 然后利用模板生成邮件内容
        String content = templateEngine.process("/mail/activation",context);
        // 利用邮件引擎发送邮件
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }



}
