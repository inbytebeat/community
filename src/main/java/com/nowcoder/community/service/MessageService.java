package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @Author: XTY~
 * @CreateTime: 15/3/2023 下午4:03
 * @Description: 私信业务层
 */
@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    /**
     * 新增私信
     * @param message 消息
     * @return 受影响的消息数
     */
    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    /**
     * 设置消息列表中的消息状态为已读
     * @param ids 消息列表中的ids
     * @return 受影响的消息数
     */
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids,1);
    }

    /**
     * 返回该主题最新的通知
     * @param userId 用户id
     * @param topic 话题类型
     * @return 通知
     */
    public Message findLastedNotice(int userId, String topic) {
        return messageMapper.selectLastedNotice(userId,topic);
    }

    /**
     * 返回某个主题通知的总数目
     * @param userId 用户id
     * @param topic 主题类型
     * @return 主题通指数
     */
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId,topic);
    }

    /**
     * 查询指定主题未读通知数，也可以查询所有主题未读通知数，只要topic为空即可
     * @param userId 用户id
     * @param topic 主题
     * @return 未读通知数
     */
    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId,topic);
    }

}
