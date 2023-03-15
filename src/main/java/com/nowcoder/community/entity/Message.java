package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: XTY~
 * @CreateTime: 15/3/2023 下午12:47
 * @Description: 私信实体类
 */
@Data
public class Message {
    /**
     * 私信id
     */
    private int id;

    /**
     * 发送者id formId = 1 表示来自系统的消息
     */
    private int formId;

    /**
     * 接收者id
     */
    private int toId;

    /**
     * 用于标识该私信属于哪两个用户之间
     */
    private String conversationId;

    /**
     * 私信内容
     */
    private String content;

    /**
     * 私信状态 1-已读 0-标识未读 2-系统消息
     */
    private int status;

    /**
     * 私信创建日期
     */
    private Date creatTime;
}
