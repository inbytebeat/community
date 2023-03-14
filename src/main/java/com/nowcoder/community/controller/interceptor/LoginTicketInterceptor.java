package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @Author: XTY~
 * @CreateTime: 13/3/2023 下午6:32
 * @Description: 获取用户登录信息的拦截器
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 实用工具类获取cookie中的数据
        String ticket = CookieUtil.getValue(request,"ticket");
        if(ticket != null) {
            // 获取用户登录凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查凭证是否为空，是否有效，是否过期
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                //  如果凭证可用，则通过登录凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 然后在此次会话中的其他请求中也能够持有用户数据 将用户数据持久化存入对应线程的map中，但是服务器会被多台浏览器访问也就是多个线程，我们需要将每个用户通过ThreadLocal单独存放，进行隔离，防止互相影响，而threadlocal的底层正是通过线程来区分，然后存入各自线程的map中
                hostHolder.setUser(user);
                System.out.println(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null) {
            // 将user存入model中
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 返回拦截器说明请求已经执行完毕 则将ThreadLocal中的数据清理
        hostHolder.clear();
    }
}
