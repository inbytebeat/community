package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @Author: XTY~
 * @CreateTime: 12/3/2023 下午8:58
 * @Description:
 */

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    // 使用盐值 + MD5加密
    // 如果只是用md5的话，简单密码可以被黑客用常用密码库给破解掉，如果加上随机生成的盐值的话，则很难破解
    public static String md5(String key) {
        if(StringUtils.isAllBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
