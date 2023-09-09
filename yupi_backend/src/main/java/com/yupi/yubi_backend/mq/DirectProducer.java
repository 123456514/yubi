package com.yupi.yubi_backend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.apache.xmlbeans.impl.store.Saaj;

import java.util.Scanner;

public class DirectProducer {

  private static final String EXCHANGE_NAME = "direct-exchange";

  public static void main(String[] argv) throws Exception {
      //创建工厂
    ConnectionFactory factory = new ConnectionFactory();
     //设置连接工厂的主机地址为本地地址
    factory.setHost("localhost");
    try(Connection connection = factory.newConnection();
    Channel channel = connection.createChannel()){
        channel.exchangeDeclare(EXCHANGE_NAME,"direct");
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            //读取用户输入的一行内容，并以空格分隔
            String userInput = scanner.nextLine();
            String []strings = userInput.split(" ");
            //如果输入内容不符合要求，继续读取下一行
            if(strings.length < 1){
                continue;
            }
            //获取消息内容和路由键
            String message = strings[0];
            String routingKey = strings[1];
            //发布消息到直连交换机
            // 使用通道的basicPublish 方法将消息发布到交换机
            //EXCHANGE_NAME 表示要发布消息的交换机的名称
            //routingKey 表示消息的路由键，用于确定消息被路由到哪个队列
            //null 表示不适用额外的消息属性
            // message.getBytes("UTF-8") 将消息内容妆花为UTF-8编码的字节数组
            channel.basicPublish(EXCHANGE_NAME,routingKey,null,message.getBytes("UTF-8"));
            //打印成功发送的消息信息，包括消息内容和路由键
            System.out.println("[x] sent" + message + "with routing" + routingKey + ".") ;
        }
    }
  }
  //..
}
