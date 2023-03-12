package com.nowcoder.community.util;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @Author: XTY~
 * @CreateTime: 12/3/2023 下午6:21
 * @Description: 用于邮箱发送的工具类
 */
@Component
public class MailClient {
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送邮件
     * @param to 收件人
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public void sendMail(String to,String subject,String content){

        try {
            // 创建邮箱模板
            MimeMessage message = mailSender.createMimeMessage();
            // MimeMessageHelper用于帮助我们构建邮箱模板
            MimeMessageHelper helper = new MimeMessageHelper(message);
            // 发送者
            helper.setFrom(from);
            // 接收者
            helper.setTo(to);
            // 邮件主题
            helper.setSubject(subject);
            // 邮件内容,第二个参数true表示支持html格式
            helper.setText(content,true);
            // 发送构建好的邮件
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败: " + e.getMessage());
        }
    }

}
