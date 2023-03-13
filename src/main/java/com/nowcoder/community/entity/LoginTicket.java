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
    private int userId;
    /**
     * 核心是ticket即（登陆凭证，用于浏览器提交给客户端识别其登录状态） 表login_ticket的关键字段
     */
    private String ticket;
    private int status;
    private Date expired;
}
