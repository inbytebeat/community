package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: XTY~
 * @CreateTime: 13/3/2023 下午2:21
 * @Description: 管理用户登录凭证接口
 */
@Mapper
public interface LoginTicketMapper {

    /**
     * 新增用户登录状态，存储该用户的登录信息
     * @param ticket 用户登录凭证
     * @return
     */
    int insertLoginTicket(LoginTicket ticket);

    /**
     * 更新用户登录状态
     * @param ticket 用户登录凭证
     * @param status 用户登录状态
     * @return 受影响的行数 用于判断结果
     */
    int updateStatus(String ticket, int status);

    /**
     * 查询指定用户的登录状态
     * @param ticket 用户登录凭证的ticket字段，即登录状态的唯一标识
     * @return 用户登录凭证
     */
    LoginTicket selectByTicket(String ticket);


}
