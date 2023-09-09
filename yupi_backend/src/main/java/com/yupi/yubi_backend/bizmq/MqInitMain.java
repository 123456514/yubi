//package com.yupi.yubi_backend.bizmq;
//
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.ConnectionFactory;
//
//import java.io.IOException;
//import java.util.concurrent.TimeoutException;
//
//public class MqInitMain {
//    public static void main(String[] args) {
//        try{
//            ConnectionFactory connectionFactory =  new ConnectionFactory();
//            connectionFactory.setHost("localhost");
//            Connection connection = connectionFactory.newConnection();
//            Channel channel = connection.createChannel();
//            String EXCHANGE_NAME = "code_change";
//            channel.exchangeDeclare(EXCHANGE_NAME,"direct");
//            String queueName = "code_queue";
//            channel.queueDeclare(queueName,true,false,false,null);
//            channel.queueBind(queueName,EXCHANGE_NAME,"my_routingKey");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
