package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @Author: XTY~
 * @CreateTime: 16/3/2023 上午11:30
 * @Description: redis配置类
 */
@Configuration
public class RedisConfig {

    @Bean
    // 通过形参自动注入工厂
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        // 给template设置工厂，这样template就有了访问数据库的能力
        template.setConnectionFactory(factory);

        // 指定redis序列化key的方式,设置序列化器为字符型
        template.setKeySerializer(RedisSerializer.string());
        // 指定value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        // 指定hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        // 指定hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());
        // 使得对template的设置生效
        template.afterPropertiesSet();
        return  template;
    }
}
