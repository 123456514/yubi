package com.yupi.yubi_backend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class rabbitmq_productor {
    public static void main(String[] args) throws IOException, TimeoutException {
        //创建链接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置参数
        factory.setHost("124.223.222.249");
        factory.setPort(5672); // 端口
        factory.setVirtualHost("/itcase");
        factory.setUsername("heima");
        factory.setPassword("heima");
        //创建链接
        Connection connection = factory.newConnection();
        //创建channel
        Channel channel = connection.createChannel();
        //直接创建队列，queue
        // queue: 队列名称
        // durable 是否持久化，当mq重启之后，还在
        //exclusive 是否独占 只能有一个消费者监听这个队列
        // 当connection 关闭的时候，是否删除队列
        // autoDelete : 是否自动删除，当没有 Consumer 时 自动删除掉
        // arguments : 参数
        //如果没有一个名字叫hello_world的队列，则会创建该队列，
        channel.queueDeclare("hello_world",true,false,false,null);
        //创建所发的消息
        String body = "hello rabbitMQ";
        // basicPublish(String exchange,String routingKey,BasicProperties props,byte []body)
        // exchange ： 交换机的名称，简单模式下交换使用默认的 ""
        //routingKey 路由名称
        // props：配置消息
        // body： 发送消息的数据
        channel.basicPublish("","hello_world",null,body.getBytes());
        // 发送消息给Queue
        channel.close();
        //关闭链接
        connection.close();
    }
}
