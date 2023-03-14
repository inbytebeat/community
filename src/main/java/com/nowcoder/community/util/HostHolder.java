package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @Author: XTY~
 * @CreateTime: 13/3/2023 下午6:58
 * @Description: 存储用户信息，用于代替session对象，并且是线程隔离的
 */

@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    /**
     * 根据线程来对应存储数据
     * @return 用户数据
     */
    public void setUser(User user) {
        users.set(user);
    }

    /**
     * 根据线程来对应获取数据
     * @return 用户数据
     */
    public User getUser() {
        return users.get();
    }

    /**
     * 当一个对话结束之后 就清除ThreadLocal中，对应线程中所存储的user数据
     */
    public void clear() {
        users.remove();
    }

}
