package com.yupi.yubi_backend.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
public class BIMqInitMain {
    public static void main(String[] args) {
        try{
            ConnectionFactory connectionFactory =  new ConnectionFactory();
            connectionFactory.setHost("124.223.222.249");
            connectionFactory.setUsername("admin");
            connectionFactory.setPassword("123");
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME = BIMqConstant.BI_EXCHANGE_NAME;
            channel.exchangeDeclare(EXCHANGE_NAME,"direct");
            String queueName = BIMqConstant.BI_QUEUE_NAME;
            channel.queueDeclare(queueName,true,false,false,null);
            channel.queueBind(queueName,EXCHANGE_NAME,BIMqConstant.BI_ROUTING_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
