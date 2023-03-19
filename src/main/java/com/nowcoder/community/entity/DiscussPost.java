package com.nowcoder.community.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
@Document(indexName = "discusspost", type = "_doc", shards = 6, replicas = 3)
public class DiscussPost {
    /**
     * 贴子id
     */
    @Id
    private int id;

    /**
     * 发布者id
     */
    @Field(type = FieldType.Integer)
    private int userId;

    /**
     * 贴子标题
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /**
     * 贴子内容
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 贴子内容 0-普通 1-置顶
     */
    @Field(type = FieldType.Integer)
    private int type;


    /**
     * 贴子状态 0-正常 1-精华 2-拉黑
     */
    @Field(type = FieldType.Integer)
    private int status;

    /**
     * 贴子创建时间
     */
    @Field(type = FieldType.Date)
    private Date createTime;

    /**
     * 贴子评论数
     */
    @Field(type = FieldType.Integer)
    private int commentCount;

    /**
     * 贴子的分数
     */
    @Field(type = FieldType.Double)
    private double score;
}
