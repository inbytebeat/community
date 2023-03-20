package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: XTY~
 * @CreateTime: 20/3/2023 上午10:37
 * @Description: 更新贴子分数的定时任务
 */

public class PostScoreRefreshJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 牛客纪元
     */
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败",e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if( operations.size() == 0) {
            logger.info("任务取消 没有需要刷新的贴子");
        }

        logger.info("任务开始 正在刷新帖子分数: " + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer)operations.pop());
        }
    }

    /**
     * 更新贴子分数
     * @param postId 贴子id
     */
    private void refresh(int postId) {
        DiscussPost post = discussPostService.selectDiscussPostById(postId);
        if (post == null) {
            logger.info("该帖子不存在 id = " + postId);
            return;
        }

        // 是否加精
        boolean wonderful = post.getStatus() == 1;

        // 评论数量
        int commentCount = post.getCommentCount();

        // 点赞数量
        long likeCount = 1;

        // 计算权重 加精则自动 + 75分
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 计算帖子分数 = 权重 + 天数
        double score = Math.log10(Math.max(w, 1)) + ((post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24));
        // 更新贴子的分数
        discussPostService.updateScore(postId,score);
        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
