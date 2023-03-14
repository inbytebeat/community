package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @Author: XTY~
 * @CreateTime: 14/3/2023 下午6:33
 * @Description:
 */

@Controller
@RequestMapping("/discuss")
public class DiscussController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    /**
     * 用于获取当前用户数据
     */
    @Autowired
    private HostHolder hostHolder;

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
        return CommunityUtil.getJSONString(0, "帖子发布成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    // 因为查询的结果是返回给模板 也就是返回页面而不是字符串 所以这里就不加@responseBody
    public String selectDiscussPostById(@PathVariable("discussPostId") int discussPostId, Model model) {
        DiscussPost post = discussPostService.selectDiscussPostById(discussPostId);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("post",post);
        model.addAttribute("user",user);
        // 返回的是模板路径
        return "/site/discuss-detail";
    }

}
