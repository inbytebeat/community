package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    /**
     * 用户id
     */
    private int id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 密码盐值
     */
    private String salt;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户类型
     */
    private int type;

    /**
     * 用户状态
     */
    private int status;

    /**
     *
     */
    private String activationCode;

    /**
     *
     */
    private String headerUrl;

    /**
     * 用户创建时间
     */
    private Date createTime;

}
