package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @Author: XTY~
 * @CreateTime: 15/3/2023 下午12:22
 * @Description:
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加评论
     * @param discussPostId 贴子id
     * @param comment 评论内容
     * @return 页面
     */
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.insertComment(comment);

        // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);
        // 如果实体类型是帖子
        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.selectDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if(comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.selectCommentById(comment.getId());
            event.setEntityUserId(target.getUserId());
        }
        // 处理事件
        eventProducer.fireEvent(event);
        // 如果评论的类型是贴子，就触发发帖事件
        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            // 触发发帖事件
             event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            // 计算帖子分数，现将发生变化的贴子放入redis中，即算分的缓存中，同时使用redis的set来防止短时间内多次修改导致缓存中有多条该数据
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,discussPostId);
        }
        // 点击回帖 后台逻辑处理完之后 重定向到该帖子的详情页面中
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
