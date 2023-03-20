package com.nowcoder.community.config;

import com.nowcoder.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @Author: XTY~
 * @CreateTime: 20/3/2023 上午1:00
 * @Description: 将quartz的配置信息写入数据库，然后执行定时任务的时候调用
 */
@Configuration
public class QuartzConfig {

    // FactoryBean可简化Bean的实例化过程:
    // 1.通过FactoryBean封装Bean的实例化过程.
    // 2.将FactoryBean装配到Spring容器里.
    // 3.将FactoryBean注入给其他的Bean.
    // 4.该Bean得到的是FactoryBean所管理的对象实例.

    /**
     * 刷新贴子分数的任务
     * @return
     */
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }


    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshDetail);
        factoryBean.setName("postRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        // 设置定时任务的执行时间是五分钟为一次
        factoryBean.setRepeatInterval(1000 * 60 * 5);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
