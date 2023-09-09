package com.yupi.yubi_backend.mq;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;

public class MuiltProducter {
        private static final String TASK_QUEUE_NAME = "multi_queue";
        public static void main(String[] argv) throws Exception {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {
                channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
                //创建一个输入扫描器，用于读取控制台中的输入
                Scanner scanner = new Scanner(System.in);
                //使用循环，每当用户在控制态中输入一行文本，就将其作为消息发送
                while(scanner.hasNext()){
                    String message = scanner.nextLine();
                    channel.basicPublish("", TASK_QUEUE_NAME,
                            MessageProperties.PERSISTENT_TEXT_PLAIN,
                            message.getBytes("UTF-8"));
                    System.out.println(" [x] Sent '" + message + "'");
                }
            }
        }
}
