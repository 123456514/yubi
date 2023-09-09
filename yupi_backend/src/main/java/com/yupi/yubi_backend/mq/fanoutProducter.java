package com.yupi.yubi_backend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

public class fanoutProducter {

  private static final String EXCHANGE_NAME = "fanout-exchange";

  public static void main(String[] argv) throws Exception {
      //创建连接工厂
    ConnectionFactory factory = new ConnectionFactory();
    //设置连接工程的主机地址
    factory.setHost("localhost");
    //创建链接和通道
    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
        //声明fanout 类型交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String message = scanner.nextLine();
            //将消息发送到指定的交换机
            channel.basicPublish(EXCHANGE_NAME,"",null,message.getBytes("UTF-8"));
            System.out.println("[x] sent" + " " + message + "'");
        }
    }
  }
}