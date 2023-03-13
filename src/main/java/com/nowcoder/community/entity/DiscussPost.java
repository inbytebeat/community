package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DiscussPost {
    /**
     * 贴子id
     */
    private int id;

    /**
     * 发布者id
     */
    private int userId;

    /**
     * 贴子标题
     */
    private String title;

    /**
     * 贴子内容
     */
    private String content;

    /**
     * 贴子内容 0-普通 1-置顶
     */
    private int type;
    /**
     * 贴子状态 0-正常 1-精华 2-拉黑
     */
    private int status;

    /**
     * 贴子创建时间
     */
    private Date createTime;

    /**
     * 贴子评论数
     */
    private int commentCount;

    /**
     * 贴子的分数
     */
    private double score;
}
