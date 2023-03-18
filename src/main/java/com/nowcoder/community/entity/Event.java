package com.nowcoder.community.entity;

import lombok.Data;

import java.rmi.dgc.Lease;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: XTY~
 * @CreateTime: 18/3/2023 下午4:03
 * @Description: kafka中的事件
 */
public class Event {
    /**
     * 话题名称
     */
    private String topic;

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * 事件触发者
     */
    private int userId;

    /**
     * 事件发生在什么实体之上
     */
    private int entityType;

    /**
     * 实体id
     */
    private int entityId;

    /**
     * 该实体的作者id
     */
    private int entityUserId;

    /**
     * 后期额外需要的字段存入mao中
     */
    private Map<String,Object> data = new HashMap<>();



}
