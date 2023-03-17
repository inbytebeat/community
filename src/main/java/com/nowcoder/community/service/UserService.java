package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

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

    /**
     * 根据id查询用户
     * @param id 用户id
     * @return 用户数据
     */
    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if(user == null) {
            user = initCache(id);
        }
        return user;
    }

    /**
     * 根据ticket查询用户的登陆凭证
     * @param ticket 登录凭证表示
     * @return 指定用户的登陆凭证
     */
    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
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
                clearCache(userId);
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
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // 将登陆凭证存入redis中 redis会自动的将对象序列化成为一个JSON格式的字符串
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * 使用户登录凭证失效
     * @param loginTicket 登录凭证的唯一标识
     */
    public void logout(String loginTicket) {
        // 设置用户的登录凭证状态失效即可
//        loginTicketMapper.updateStatus(loginTicket,1);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket);
        // 从redis中取出登陆凭证对象数据 然后将其登录凭证状态改为失效即可
        LoginTicket ticket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        ticket.setStatus(1);
        // 然后再将登陆凭证存回redis中 完成登录凭证状态的修改
        redisTemplate.opsForValue().set(redisKey,loginTicket);

    }

    /**
     * 修改用户头像
     * @param userId 用户id
     * @param headUrl 用户头像url
     * @return 受影响的行数
     */
    public int updateHeader(int userId, String headUrl) {
        int rows = userMapper.updateHeader(userId,headUrl);
        clearCache(userId);
        return rows;
    }

    /**
     * 修改用户密码
     * @param userId 用户id
     * @param newPassword 用户密码
     * @return 受影响的行数
     */
    public int updatePassword(int userId, String newPassword) {
        int rows = userMapper.updatePassword(userId,newPassword);
        clearCache(userId);
        return rows;
    }

    /**
     * 根据用户名查询用户
     * @param userName 用户名
     * @return 用户信息
     */
    public User selectUsrByUserName(String userName) {
        return userMapper.selectByName(userName);
    }

    /**
     * 使用redis查询用户数据 当查询用户数据时，优先从缓存中取值
     * @param userId 用户id
     * @return 用户数据
     */
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * 当从缓存中取不到数据时，这时来数据库中查询用户数据，然后缓存到redis中
     * @param userId 用户id
     * @return 用户数据
     */
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * 数据变更时，直接清除缓存数据
     * @param userId 用户id
     */
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }


}
