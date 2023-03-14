package com.nowcoder.community.util;

/**
 * @Author: XTY~
 * @CreateTime: 12/3/2023 下午11:26
 * @Description: 用户激活状态存储
 */

public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间 单位:S
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态下的登录凭证的超时时间 单位:S
     */
    int REMEMBERED_EXPIRED_SECONDS = 3600 * 24 * 7;
}