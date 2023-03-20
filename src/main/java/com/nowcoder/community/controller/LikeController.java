package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

/**
 * @Author: XTY~
 * @CreateTime: 16/3/2023 下午12:28
 * @Description: 点赞表现层
 */
@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like",method = RequestMethod.POST) // 点赞需要传入一些信息，我们使用post
    @ResponseBody // 因为是异步请求
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();
        // 点赞
        likeService.like(user.getId(),entityType,entityId, entityUserId);
        // 点赞状态
        int status = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 点赞数量
        long count = likeService.findEntityLikeCount(entityType, entityId);
        // 将结果封装 返回给页面
        HashMap<String, Object> map = new HashMap<>();
        map.put("status",status);
        map.put("count",count);

        // 触发点赞事件
        if(status == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);
        }
        if(entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数，现将新帖放入redis中，即算分的缓存中，同时使用redis的set来防止短时间内多次点赞 导致缓存中有多个该条数据，从而计算时影响效率 比如缓存中是:A B A C A，这样就要计算三次A的分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }
        // 将结果map以JSON形式返回给前端
        return CommunityUtil.getJSONString(0,null,map);
    }


}
