package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
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
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

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

    /**
     * 根据ticket查询用户的登陆凭证
     * @param ticket 登录凭证表示
     * @return 指定用户的登陆凭证
     */
    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }


    /**
     * 用户注册方法
     * @param user 用户从前端输入的注册数据
     * @return 注册信息提示
     */
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
        
        //通过用户名验证账号是否存在
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
        // 将用户存入库中
        userMapper.insertUser(user);

        //使用模板发送激活邮件
        Context context = new Context();
        // 设置模板内容
        context.setVariable("email",user.getEmail());
        // 用户邮件中的跳转链接 例：http://localhost:8080/community/activation/userId/激活码
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        System.out.println("这是用户信息" + user);
        context.setVariable("url",url);
        // 选择模板位置，注入内容 然后利用模板生成邮件内容
        String content = templateEngine.process("/mail/activation",context);
        // 利用邮件引擎发送邮件
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }

    /**
     * 用户激活功能
     * @param userId 准备激活的用户id
     * @param code 用户输入的激活码
     * @return 激活状态
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1) {
            // 如果用户的激活状态是1 则表示已激活 则返回重复激活状态码
            return ACTIVATION_REPEAT;
        }else {
            if (user.getActivationCode().equals(code)) {
                // 如果用户携带的激活码与查询该用户所对应的的激活码相同则说明是用户本人在激活，则进行激活
                userMapper.updateStatus(userId, 1);
                return ACTIVATION_SUCCESS;
            } else {
                // 如果用户携带的激活码与查询该用户所对应的的激活码不相同则说明不是用户本人在激活，则返回激活失败
                return ACTIVATION_FAILURE;
            }
        }
    }

    /**
     * 用户登录功能业务层
     * @param userName 用户名
     * @param password 未加密的，用户输入的密码
     * @param expiredSeconds 该登录的有效时间
     * @return 登录结果的封装
     */
    public Map<String,Object> login(String userName, String password, int expiredSeconds) {
        HashMap<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(userName)) {
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)) {
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        User user = userMapper.selectByName(userName);
        // 验证账户是否存在  和检查该账户的激活状态
        if(user == null) {
            map.put("usernameMsg","该账号 不存在");
            return map;
        }else if(user.getStatus() == 0) {
            map.put("userStatusMsg","该用户还未激活，请先去邮箱激活");
            return map;
        }

        // 验证用户输入的密码 通过md5 + salt
        password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(password)) {
            map.put("passwordMsg","用户密码错误 请重新输入");
            return map;
        }

        //如果通过了全部的判断 说明用户输入没有问题则登录
        // 生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * 使用户登录凭证失效
     * @param loginTicket 登录凭证的唯一标识
     */
    public void logout(String loginTicket) {
        // 设置用户的登录凭证状态失效即可
        loginTicketMapper.updateStatus(loginTicket,1);
    }

    /**
     * 修改用户头像
     * @param userId 用户id
     * @param headUrl 用户头像url
     * @return 受影响的行数
     */
    public int updateHeader(int userId, String headUrl) {
        return userMapper.updateHeader(userId,headUrl);
    }

    /**
     * 修改用户密码
     * @param userId 用户id
     * @param newPassword 用户密码
     * @return 受影响的行数
     */
    public int updatePassword(int userId, String newPassword) {
        return userMapper.updatePassword(userId,newPassword);
    }

}
