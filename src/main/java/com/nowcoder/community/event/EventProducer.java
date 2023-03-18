package com.nowcoder.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author: XTY~
 * @CreateTime: 18/3/2023 下午4:17
 * @Description: 生产者
 */
@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件
     * @param event
     */
    public void fireEvent(Event event) {
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
