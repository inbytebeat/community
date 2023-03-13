package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: XTY~
 * @CreateTime: 13/3/2023 下午2:19
 * @Description: 用户登录凭证
 */

@Data
public class LoginTicket {
    private int id;

    /**
     * 用户id
     */
    private int userId;

    /**
     * 核心是ticket即（登陆凭证，用于浏览器提交给客户端识别其登录状态） 表login_ticket的关键字段
     */
    private String ticket;

    /**
     * 用户登录状态 0表示有效 1表示无效
     */
    private int status;

    /**
     * 该登录凭证有效日期
     */
    private Date expired;
}
