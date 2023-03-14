package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: XTY~
 * @CreateTime: 14/3/2023 下午10:06
 * @Description: 评论业务层
 */
@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

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
}
