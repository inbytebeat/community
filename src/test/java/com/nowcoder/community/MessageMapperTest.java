package com.nowcoder.community;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Author: XTY~
 * @CreateTime: 15/3/2023 下午3:27
 * @Description: 私信接口测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MessageMapperTest {
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectConversations() {
        List<Message> messages = messageMapper.selectConversations(111, 0, 10);
        for (Message ms :
                messages) {
            System.out.println(ms.getContent());
        }
    }

    @Test
    public void selectConversationCount() {
        int cout = messageMapper.selectConversationCount(111);
        System.out.println("用户111的会话数是：" + cout);
    }

    @Test
    public void selectLetters() {
        List<Message> messages = messageMapper.selectLetters("111_112", 0, 10);
        System.out.println(messages);
    }

    @Test
    public void selectLetterCount() {
        int cout = messageMapper.selectLetterCount("111_112");
        System.out.println(cout);
    }

    @Test
    public void unreadCount() {
        int cout = messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(cout);
    }

}
