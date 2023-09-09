//package com.yupi.yubi_backend.bizmq;
//
//import com.rabbitmq.client.Channel;
//
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.support.AmqpHeaders;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//public class MyMesageConsumer {
//    /**
//     *  接收消息的方法
//     * @param message 接收到的消息内容，是一个字符串类型
//     * @param channel 消息所在的通道，可以通过该通道与RabbitMQ进行交互，例如手动确认消息
//     * @param deliveryTag 消息的投递标签，用于唯一便是一条信息
//     */
//    @SneakyThrows
//    @RabbitListener(queues = {"code_queue"},ackMode = "MANUAL")
//    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
//        log.info("receiveMessage message = {}",message);
//        //投递标签是一个数字表示，他在消息消费者接收到消息后用于向RabbitMQ确认消息的处理状态
//        //手动确认消息的接收，向RabbitMQ 发送确认信息
//        channel.basicAck(deliveryTag,false);
//    }
//}
