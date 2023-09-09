package com.yupi.yubi_backend.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component // 使用该注解标记该类是一个组件，让Spring框架能够扫描并移交到Spring容器来管理
public class BiMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息的方法
     */
    public void sendMessage(String message){
        //使用rabbitTemple 的convertAndsend方法将消息发送到指定的交换机和路由键
        rabbitTemplate.convertAndSend(BIMqConstant.BI_EXCHANGE_NAME,BIMqConstant.BI_ROUTING_KEY,message);
    }
}
