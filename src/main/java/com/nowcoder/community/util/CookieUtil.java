package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: XTY~
 * @CreateTime: 13/3/2023 下午6:33
 * @Description: 从cookie中获取值的小工具
 */

public class CookieUtil {
    /**
     * 从cookie中获取指定的值
     * @param request HttpServletRequest
     * @param name 从cookie中获取的值的名称
     * @return 我们需要从cookie中获取的值
     */
    public static String getValue(HttpServletRequest request, String name) {
        if (request == null || name == null) {
            throw new IllegalArgumentException("参数为空");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals(name))
                    return cookie.getValue();
            }
        }
        // cookie中没有指定的数据
        return null;
    }

}
