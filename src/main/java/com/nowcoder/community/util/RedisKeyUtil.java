package com.nowcoder.community.util;

/**
 * @Author: XTY~
 * @CreateTime: 15/3/2023 下午10:03
 * @Description: 生成redisKey的小工具
 */

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    // 某个实体的赞 包含了帖子以及评论
    // like:entity:entityType:entityId -> set(userId) - 将为之点赞的用户的id存入set集合中

    /**
     * 获取我们制定好格式的key
     * @param entityType 实体类型
     * @param entityId 实体id
     * @return 该实体在redis中的key值
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return  PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    public static String geyUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }


}
