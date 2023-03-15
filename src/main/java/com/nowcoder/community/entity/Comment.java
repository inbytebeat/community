package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: XTY~
 * @CreateTime: 14/3/2023 下午9:31
 * @Description: 用户评论实体类
 */
@Data
public class Comment {
    /**
     * 评论所属id
     */
    private int id;

    /**
     * 发布该评论的用户id
     */
    private int userId;

    /**
     * 表示该评论的类别，是属于对评论的评论 还是对帖子的评论
     */
    private int entityType;

    /**
     * 表示该评论所评论的帖子id
     */
    private int entityId;

    /**
     * 表示这条评论是对用户的回复，target_id对应的是被回复的用户id
     */
    private int targetId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 该评论的状态 0为默认表正常
     */
    private int status;

    /**
     * 该评论的创建时间
     */
    private Date createTime;


}
