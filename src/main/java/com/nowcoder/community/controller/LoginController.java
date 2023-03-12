package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * @Author: XTY~
 * @CreateTime: 12/3/2023 下午8:21
 * @Description: 用于用户注册功能
 */

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
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
    @RequestMapping(path = "/activation/#{userId}/#{code}",method = RequestMethod.GET)
    public String actication(Model model, @PathVariable("userId")int userId) {
        return null;
    }


}
