package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子dao层
 */
@Mapper
public interface DiscussPostMapper {

    /**
     * 新增帖子
     * @param discussPost 贴子数据
     * @return 受影响的行数
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 查询所有帖子数据
     * @param userId 用户id
     * @param offset 索引
     * @param limit 每页容量
     * @return 分页后的贴子数据
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    /**
     * 根据帖子id查询帖子详情
     * @param id 帖子id
     * @return 帖子详情
     */
    DiscussPost selectDiscusPostById(int id);


    /**
     // @Param注解用于给参数取别名,
     // 如果只有一个参数,并且用于sql语句中的<if>,则必须加别名.
     * 查询指定用户帖子条数
     * @param userId 用户id
     * @return 帖子条数
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 更新帖子的评论数量
     * @param id 帖子id
     * @param commentCount 帖子的评论数量
     * @return 新的帖子的评论数量
     */
    int updateCommentCount(int id, int commentCount);

    /**
     * 修改帖子类型
     * @param id 帖子id
     * @param type 帖子要修改成为的类型
     * @return 操作结果
     */
    int updateType(int id, int type);

    /**
     * 修改帖子状态
     * @param id 帖子id
     * @param status 帖子状态
     * @return 操作结果
     */
    int updateStatus(int id, int status);

}
