package com.nowcoder.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;


/**
 * @Author: XTY~
 * @CreateTime: 15/3/2023 下午4:07
 * @Description: 私信控制层
 */
@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 获取当前用户所有对话
     * @param model 用于存放数据
     * @param page 用于分页
     * @return 对话数据
     */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        // 获取当前用户
        User user = hostHolder.getUser();
        // 设置分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // 查询该用户的所有会话数据
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            // 遍历每一个会话数据，将会话数据，指定会话的消息总数，未读消息数存入map中,发送私信的用户头像 封装起来
            for (Message message : conversationList) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                // 如果当前用户是这一会话最后一条消息的发送者，那么目标(target)就应该是接收id，反之亦然。
                int targetId = user.getId() == message.getFormId() ? message.getToId() :message.getFormId();
                // 将目标对象存入map中，之后用于显示这个会话的头像
                map.put("target",userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        // 查询用户总的未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        // 查询用户所有通知的未读数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "/site/letter";
    }

    /**
     * 用于显示指定对话全部消息-对话详情
     * @param conversationId 对话id
     * @param page 用于分页
     * @param model 用于存储数据
     * @return 制定对话的全部消息
     */
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 设置分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        for (Message message : letterList) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("letter",message);
            map.put("fromUser",userService.findUserById(message.getFormId()));
            letters.add(map);
        }
        model.addAttribute("letters",letters);

        // 添加私信目标到model，返回给会话详情页面
        model.addAttribute("target",getLetterTarget(conversationId));

        // 设置消息已读
        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    /**
     * 获取一个会话中的所有未读消息id
     * @param letterList 会话中所有的所有消息
     * @return 未读消息id
     */
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        for (Message message : letterList) {
            if(message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0) {
                ids.add(message.getId());
            }
        }
        return ids;
    }

    /**
     * 发送私信
     * @param toName 接收人
     * @param content 私信内容
     * @return 结果
     */
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter (String toName,  String content) {
        User target = userService.selectUsrByUserName(toName);
        if(target == null) {
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFormId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        message.setContent(content);
        if (message.getFormId() > message.getToId()) {
            message.setConversationId(message.getToId() + "_" + message.getFormId());
        }else {
            message.setConversationId(message.getFormId() + "_" + message.getToId());
        }
        message.setCreatTime(new Date());
        messageService.addMessage(message);
        System.out.println(CommunityUtil.getJSONString(0));
        return CommunityUtil.getJSONString(0);
    }


    /**
     * 获取与登录用户对话的对象
     * @param conversationId 对话id
     * @return 对话对象
     */
    private User getLetterTarget (String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if(id0 == hostHolder.getUser().getId()) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    @RequestMapping(path = "notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();
        // 查询评论类通知
        Message message = messageService.findLastedNotice(user.getId(), TOPIC_COMMENT);
        Map<String,Object> messageVO = new HashMap<>();
        if(message != null) {
            messageVO.put("message", message);
            String content = HtmlUtils.htmlEscape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);
        }
        model.addAttribute("commentNotice", messageVO);

        // 查询点赞类
        message = messageService.findLastedNotice(user.getId(), TOPIC_LIKE);
        messageVO = new HashMap<>();
        if(message != null) {
            messageVO.put("message", message);
            String content = HtmlUtils.htmlEscape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);
        }
        model.addAttribute("likeCount", messageVO);

        // 查询关注类通知
        message = messageService.findLastedNotice(user.getId(), TOPIC_FOLLOW);
        messageVO = new HashMap<>();
        if(message != null) {
            messageVO.put("message", message);
            String content = HtmlUtils.htmlEscape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);
        }
        model.addAttribute("followService", messageVO);

        // 查询所有的未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }


}
