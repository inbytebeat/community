package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

}
