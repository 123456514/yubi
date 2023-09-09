package com.yupi.yubi_backend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
//发单个消息
public class SingleSend {
    public final static String QUEUE_NAME = "hello";
    public static void main(String []args) throws Exception{
        //创建链接工程
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        //建立连接，创建频道
        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()){
            channel.queueDeclare(QUEUE_NAME,false,false,false,null);
            //发送消息
            String message = "hello world";
            channel.basicPublish("",QUEUE_NAME,null,message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] sent '" + message + "'");
        }
    }
}
