package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

/**
 * @Author: XTY~
 * @CreateTime: 12/3/2023 下午8:21
 * @Description: 用于用户注册功能
 */

@Controller
public class LoginController implements CommunityConstant {

    // 创建日志对象
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    //项目路径
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;



    /**
     * 生成验证码工具
     */
    @Autowired
    private Producer kaptchaProducer;

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * 用户注册业务
     * @param model 用于存储数据返回给模板
     * @param user 接收用户数据
     * @return
     */
    @RequestMapping(path = "register",method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        // 如果查询后发现用户为空 说明还未注册 那么就说明注册完成
        if(map == null || map.isEmpty()) {
            // 注册成功之后 设置中间页的跳转，并将给中间页的数据存入model之中
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活");
            // 设置中间页的最终跳转路径
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else {
            //注册失败 则将用户的注册信息发送回注册页面
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "site/register";
        }

    }

    /**
     * 用于用户账号激活 激活码:http://localhost:8080/community/activation/userId/激活码
     * @param model 视图对象 用于返回数据
     * @param userId 用户id
     * @param code 激活码
     * @return 激活结果或是跳转页面地址
     */
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String actication(Model model, @PathVariable("userId")int userId,@PathVariable("code")String code) {
        int activation = userService.activation(userId, code);
        if(activation  == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了");
            // 设置激活成功后的跳转路径
            model.addAttribute("target","/login");
        }else if (activation  == ACTIVATION_REPEAT) {
            model.addAttribute("msg","请不要重复激活，该账号已经激活");
            // 设置激活成功后的跳转路径
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","激活失败，激活码有误");
            // 设置激活成功后的跳转路径
            model.addAttribute("target","/index");
        }
        return "site/operate-result";
    }

    /**
     * 获取图片验证码
     * @param response HttpServletResponse
     * @param session HttpSession
     */
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        //生成验证码
        String text = kaptchaProducer.createText();
        // 生成验证码图片
        BufferedImage image = kaptchaProducer.createImage(text);

        // 现将验证码存入session
        session.setAttribute("kaptcha",text);
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 使用工具类将图片写给浏览器
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            LOGGER.error("响应验证码失败:" + e.getMessage());
        }
    }

    /**
     * 用户登录控制层
     * @param username 用户名-前端获取
     * @param password 用户密码-前端获取
     * @param code 用户输入的验证码
     * @param rememberme 记住我选项
     * @param model 视图 用于返回数据
     * @param session 用于获取用户刚点击页面时，系统后台所生成的验证码，该验证码在session中保存
     * @param response 用于客户端向浏览器发送cookie ，该响应保存的是用户登录成功后的ticket 即登陆凭证 使用cookie保存
     * @return
     */
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberme, Model model, HttpSession session, HttpServletResponse response) {
        // 先判断验证码
        String kaptcha = (String) session.getAttribute("kaptcha");
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            //如果用户输入的验证码与后台对不上
            model.addAttribute("codeMsg","验证码有误");
            // 返回登录页面
            return "/site/login";
        }
        // 检查用户账号，密码
        // 判断用户是否勾选记住我 从而设置该用户的等凭证过期时间
        int expiredSeconds = rememberme ? REMEMBERED_EXPIRED_SECONDS :DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")) {
            // 如果进行登录业务后，得到的map中有登录凭证信息 则进行登录
            // 现将从数据库查到的登录凭证写入cookie中 准备使用session发送给浏览器进行登录凭证的保存
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            // 设置该cookie的作用范围是整个项目，因为是用户登录的cookie
            cookie.setPath(contextPath);
            // 设置该cookie的作用时间范围
            cookie.setMaxAge(expiredSeconds);
            // 添加cookie到session中 使用session发送给浏览器进行登录凭证的保存
            response.addCookie(cookie);
            // 重定向到首页
            return "redirect:/index";
        }else {
            //如果没有凭证信息 则向model中添加消息错误 查看到底是什么错误引起的登录失败
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            // 返回登录页面
            return "/site/login";
        }

    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        // 重定向到登录页面
        return "redirect:/login";
    }


}
