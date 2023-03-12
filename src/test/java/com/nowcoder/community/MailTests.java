package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    MailClient mailClient;

    // 用于初始化我们的邮件模板
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void textSendMail(){
        mailClient.sendMail("1515577847@qq.com","邮箱模块测试","不要在意这是一条测试消息");
    }

    // 测试利用模板引擎，初始化我们的邮件模板 然后发送html邮件
    @Test
    public void testHtmlMail() {
        // 存储模板中的数据
        Context context = new Context();
        context.setVariable("username","shuaige");
        // 使用TemplateEngine模板引擎生成动态网页
        String content = templateEngine.process("/mail/demo",context);
        System.out.println(content);
        mailClient.sendMail("3153483595@qq.com","你的许哥喊你一起加油",content);
    }

}
