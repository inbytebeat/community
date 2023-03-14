package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
     * @param offset 当前页
     * @param limit 每页容量
     * @return 分页后的贴子数据
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);



    /**
     // @Param注解用于给参数取别名,
     // 如果只有一个参数,并且用于sql语句中的<if>,则必须加别名.
     * 查询指定用户帖子条数
     * @param userId 用户id
     * @return 帖子条数
     */
    int selectDiscussPostRows(@Param("userId") int userId);

}
