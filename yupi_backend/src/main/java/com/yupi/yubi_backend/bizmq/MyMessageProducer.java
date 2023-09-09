//package com.yupi.yubi_backend.bizmq;
//
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.boot.autoconfigure.cache.CacheProperties;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
//@Component // 使用该注解标记该类是一个组件，让Spring框架能够扫描并移交到Spring容器来管理
//public class MyMessageProducer {
//    @Resource
//    private RabbitTemplate rabbitTemplate;
//
//    /**
//     * 发送消息的方法
//     * @param exchange 交换机名称 指定消息要发送到哪个交换机
//     * @param routingKey 路由键 指定消息要根据什么规则路由到相应的队列
//     * @param message 消息内容，要发送的具体消息
//     */
//    public void sendMessage(String exchange,String routingKey,String message){
//        //使用rabbitTemple 的convertAndsend方法将消息发送到指定的交换机和路由键
//        rabbitTemplate.convertAndSend(exchange,routingKey,message);
//    }
//}
