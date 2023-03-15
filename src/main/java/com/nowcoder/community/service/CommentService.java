package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @Author: XTY~
 * @CreateTime: 14/3/2023 下午10:06
 * @Description: 评论业务层
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 根据实体类型查询帖子评论
     * @param entityType 实体类型编号
     * @param entityId 实体类id
     * @param offset 当前页码
     * @param limit 当前页大小
     * @return 查询的帖子数据
     */
    public List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    /**
     * 根据实体类型查询帖子评论数目
     * @param entityType 实体类型编号
     * @param entityId 实体类id
     * @return 查询的帖子评论条数
     */
    public int selectCommentCountByEntity(int entityType, int entityId) {
        return commentMapper.selectCommentCountByEntity(entityType,entityId);
    }

    /**
     * 新增评论
     * @param comment 评论内容
     * @return 受影响行数
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int insertComment(Comment comment) {
        if(comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 过滤评论中的html标签
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 过滤评论敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);
        // 判断该评论是否是给帖子的评论 如果是则更新该帖子的评论数
        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            int commentCount = commentMapper.selectCommentCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostMapper.updateCommentCount(comment.getEntityId(),commentCount);
        }
        return rows;
    }
}
