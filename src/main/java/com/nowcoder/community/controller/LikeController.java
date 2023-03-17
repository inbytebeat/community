package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
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
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like",method = RequestMethod.POST) // 点赞需要传入一些信息，我们使用post
    @ResponseBody // 因为是异步请求
    public String like(int entityType, int entityId, int entityUserId) {
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
        // 将结果map以JSON形式返回给前端
        return CommunityUtil.getJSONString(0,null,map);
    }


}
