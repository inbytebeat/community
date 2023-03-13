package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
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

    //http://localhost:8080/community/activation/userId/激活码

    /**
     * 用于用户账号激活
     * @param model
     * @param userId
     * @param code
     * @return
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


}
