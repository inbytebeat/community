package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: XTY~
 * @CreateTime: 20/3/2023 下午12:50
 * @Description:
 */

@Controller
public class ShareController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    // 使用异步的方式 生成图片，用事件驱动丢给kafka，kafka异步实现即可，如果硬等的话 时间太长影响体验
    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @RequestMapping(path = "/share", method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl) {
        // 文件名
        String fileName = CommunityUtil.generateUUID();

        // 异步生成长图
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                .setData("suffix", ".png");
        eventProducer.fireEvent(event);

        // 返回访问路径
        Map<String, Object> map = new HashMap<>();
//        map.put("shareUrl",  + fileName);
        map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);

        return CommunityUtil.getJSONString(0, null, map);
    }

    // 获取长图
    @RequestMapping(path = "/share/image/{fileName}", method = RequestMethod.GET)
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        if(StringUtils.isBlank(fileName)) {
            throw  new IllegalArgumentException("文件名不能为空");
        }

        response.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + fileName + ".png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buff = new byte[1024];
            int b = 0;
            while ((b = fis.read(buff)) != -1) {
                outputStream.write(buff, 0 , b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败：" + e.getMessage());
        }
    }


}
