package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: XTY~
 * @CreateTime: 14/3/2023 下午9:37
 * @Description: 评论dao层
 */
@Mapper
public interface CommentMapper {

    /**
     * 根据实体类型查询帖子数据
     * @param entityType 实体类型编号
     * @param entityId 实体类id
     * @param offset 当前页码
     * @param limit 当前页大小
     * @return 查询的帖子数据
     */
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 根据实体类型查询帖子数目
     * @param entityType 实体类型编号
     * @param entityId 实体类id
     * @return 查询的帖子条数
     */
    int selectCommentCountByEntity(int entityType, int entityId);

    /**
     * 新增评论
     * @param comment 评论数据
     * @return 影响的行数
     */
    int insertComment(Comment comment);

    /**
     * 根据id查询评论
     * @param id 评论id
     * @return 评论
     */
    Comment selectCommentById(int id);
}
