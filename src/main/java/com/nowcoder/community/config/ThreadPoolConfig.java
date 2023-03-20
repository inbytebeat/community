package com.nowcoder.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author: XTY~
 * @CreateTime: 20/3/2023 上午12:27
 * @Description: Spring线程池配置类
 */
@Configuration
@EnableScheduling
// 该注解是开启定时任务的
@EnableAsync
public class ThreadPoolConfig {
}
