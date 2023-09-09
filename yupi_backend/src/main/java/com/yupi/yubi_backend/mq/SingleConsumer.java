package com.yupi.yubi_backend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.Queue;

public class SingleConsumer {
    private final static String QUEUE_NAME = "hello";
    public static void main(String []args) throws Exception{
        //创建链接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //创建队列
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        System.out.println("Waiting for message to exit press");
        //定义了如何处理消息
        DeliverCallback deliverCallback = (consumerTeg,delivery)->{
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(message);
        };
        //消费队列会持续等待
        channel.basicConsume(QUEUE_NAME,true,deliverCallback,consumerTag ->{ });
    }
}
