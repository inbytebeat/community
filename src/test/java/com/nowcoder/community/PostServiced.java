package com.nowcoder.community;

import com.nowcoder.community.controller.DiscussController;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: XTY~
 * @CreateTime: 19/3/2023 下午9:03
 * @Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class PostServiced {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private DiscussController discussController;

    @Test
    public void controllerTest() {
        discussController.setTop(289);
    }

    @Test
    public void serviceTest() {
        discussPostService.updateType(289,1);
    }

    @Test
    public void serviceTest2() {
        discussPostService.updateStatus(289,1);
    }

}
