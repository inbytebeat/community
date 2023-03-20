package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @Author: XTY~
 * @CreateTime: 14/3/2023 下午6:33
 * @Description: 帖子控制层
 */

@Controller
@RequestMapping("/discuss")
public class DiscussController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 用于获取当前用户数据
     */
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增帖子
     * @param title 帖子标题
     * @param content 帖子内容
     * @return 操作响应
     */
    @RequestMapping(path = "add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user =  hostHolder.getUser();
        if(user == null) {
            return CommunityUtil.getJSONString(403,"你还没有登录，请先登录再发帖");
        }
        // 初始化帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setCreateTime(new Date());
        post.setContent(content);
        discussPostService.addDiscussPost(post);
        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);
        // 计算帖子分数，现将新帖放入redis中，即算分的缓存中，同时使用redis的set来防止短时间内多次点赞 导致缓存中有多个该条数据，从而计算时影响效率 比如缓存中是:A B A C A，这样就要计算三次A的分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        return CommunityUtil.getJSONString(0, "帖子发布成功");
    }

    /**
     * 查询帖子详情数据（包括评论）
     * @param discussPostId 帖子id
     * @param model 视图
     * @return 模板路径
     */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    // 因为查询的结果是返回给模板 也就是返回页面而不是字符串 所以这里就不加@responseBody
    // 只要是参数中包括bean对象，最终springmvc都会将该bean存入model中，所以我们最终可以通过model来获取bean的数据，也就是page
    public String selectDiscussPostById(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 查询帖子和用户数据，并且装入model中返回给页面
        DiscussPost post = discussPostService.selectDiscussPostById(discussPostId);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("post",post);
        model.addAttribute("user",user);
        // 点赞信息
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);
        // 点赞状态 如果用户未登录 点赞状态就是未点赞
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(user.getId(),ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeStatus",likeStatus);
        // 进行帖子详情中评论的分页显示
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论：给帖子的回复叫做评论
        // 回复：给评论的评论叫做回复
        // 获取指定帖子的所有评论列表
        List<Comment> commentList = commentService.selectCommentByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 评论的VO列表
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        // 对评论进行遍历 然后将获取到的数据 封装进commentVoList 用于给页面展示
        if(commentList != null) {
            for (Comment comment:commentList) {
                // 一个评论的VO
                Map<String, Object> commentVo = new HashMap<>();
                // 向评论VO中 存入评论
                commentVo.put("comment",comment);
                // 向评论VO中，存入发出评论的用户数据
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                // 点赞个数
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                // 点赞状态 如果用户未登录 点赞状态就是未点赞
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(user.getId(),ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus",likeStatus);
                // 评论的回复列表(评论的评论)
                List<Comment> replaceList = commentService.selectCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复Vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replaceList != null) {
                    for (Comment reply:replaceList) {
                        Map<String,Object>  replyVo = new HashMap<>();
                        // 存入回复
                        replyVo.put("reply",reply);
                        // 存入回复者的数据
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        // 点赞个数
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        // 点赞状态 如果用户未登录 点赞状态就是未点赞
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(user.getId(),ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus",likeStatus);
                        // 获取被回复的目标
                        User target = reply.getTargetId() == 0 ? null :userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);
                // 回复数量
                int replyCount = commentService.selectCommentCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        // 返回的是模板路径
        return "/site/discuss-detail";
    }

    /**
     * 置顶帖子
     * @param id 帖子id
     * @return 操作结果
     */
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);
        User user = hostHolder.getUser();
        // 触发修改事件 将修改的帖子数据添加到elactisearch服务器中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 加精帖子
     * @param id 帖子id
     * @return 操作结果
     */
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String updateStatus(int id) {
        discussPostService.updateStatus(id, 1);
        // 触发修改事件 将修改的帖子数据添加到elactisearch服务器中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数，现将新帖放入redis中，即算分的缓存中，同时使用redis的set来防止短时间内多次点赞 导致缓存中有多个该条数据，从而计算时影响效率 比如缓存中是:A B A C A，这样就要计算三次A的分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 删除帖子
     * @param id 帖子id
     * @return 操作结果
     */
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);
        // 触发删帖事件 将修改的帖子数据添加到elactisearch服务器中
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
