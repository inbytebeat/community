package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @Author: XTY~
 * @CreateTime: 15/3/2023 下午12:51
 * @Description: 私信dao层
 */
@Mapper
public interface MessageMapper {

    /**
     * 查询当前用户的会话列表，每个会话只返回最新的一条私信（有可能是用户发送的，也有可能是用户接收的）
     * 实现思路：先查询该用户每个对话中最新消息的id，然后拿着id去查询消息，就得到了当前用户每个对话中的最新消息
     * @param userId 当前用户id
     * @param offset 当前页
     * @param limit 页大小
     * @return 当前用户每个对话中的最新消息
     */
    List<Message> selectConversations(int userId, int offset, int limit);

    /**
     * 查询用户的会话数量
     * 实现思路：先查询该用户每个对话中最新消息的id，然后直接对id统计即可，便是该用户的总会话数
     * @param userId 用户id
     * @return 用户会话数量
     */
    int selectConversationCount(int userId);

    /**
     * 查询某个会话所包含的所有消息
     * @param conversationId 会话id
     * @param offset 当前页
     * @param limit 页面大小
     * @return 指定会话所包含的所有消息
     */
    List<Message> selectLetters(String conversationId, int offset, int limit);

    /**
     *  查询某个会话所包含的私信数量
     * @param conversationId 会话id
     * @return 会话所包含的私信数量
     */
    int selectLetterCount(String conversationId);

    /**
     * 查询用户未读信息数量
     * 即可以查该用户的所有未读信息，也可以查该用户与某一个用户会话之间的未读信息数量，动态拼接conversationId即可
     * @param userId 用户id
     * @param conversationId 对话id
     * @return 用户未读信息数量
     */
    int selectLetterUnreadCount(int userId, String conversationId);

    /**
     * 新增私信
     * @param message 消息
     * @return 受影响的行数
     */
    int insertMessage(Message message);

    /**
     * 修改消息的状态 设置已读 or 未读 or 删除
     * @param ids 消息的id合集
     * @param status 要设置的状态
     * @return
     */
    int updateStatus(List<Integer> ids, int status);

    // 查询某个主题下最新的通知
    Message selectLastedNotice(int userId, String topic);

    // 查询某个主题所包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读的通知的数量
    int selectNoticeUnreadCount(int userId, String topic);

}
