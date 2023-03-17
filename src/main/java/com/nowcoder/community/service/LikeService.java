package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @Author: XTY~
 * @CreateTime: 15/3/2023 下午10:07
 * @Description: 点赞业务层
 */
@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     * @param userId 点赞者
     * @param entityType 实体类型
     * @param entityId 实体id
     */
    public void like(int userId, int entityType, int entityId) {
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
//        // 判断是否已经点过赞
//        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if(isMember) {
//            // 将点赞者从set中移除
//            redisTemplate.opsForSet().remove(entityLikeKey,userId);
//        }else {
//            // 添加点赞者
//            redisTemplate.opsForSet().add(entityLikeKey,userId);
//        }
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKet4y = RedisKeyUtil.getEntityLikeKey()
            }
        });
    }

    /**
     * 查询某实体被点赞的数量
     * @param entityType 实体类型
     * @param entityId 实体id
     * @return 被点赞数
     */
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询用户对某实体的点赞状态
     * @param userId 用户id
     * @param entityType 实体类型
     * @param entityId 实体id
     * @return 点赞状态
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }




}
