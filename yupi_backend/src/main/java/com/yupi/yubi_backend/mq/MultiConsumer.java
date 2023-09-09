package com.yupi.yubi_backend.mq;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class MultiConsumer {
        //声明队列名称为  "multi_queue"
        private static final String TASK_QUEUE_NAME = "multi_queue";
        public static void main(String[] argv) throws Exception {
            //创建一个新的连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            //创建连接工程的主机地址
            factory.setHost("localhost");
            //从工厂获得一个新的连接
            final Connection connection = factory.newConnection();
            for(int i = 0;i < 2;i++){
                //从连接获取一个新的通道
                final Channel channel = connection.createChannel();
                //声明一个队列，并设置属性，队列名称，持久化，非排他，自动删除
                channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
                // 在控制态中打印等待消息的提示信息
                System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
                //这个先注释，设置预计计数为1，这样RabbitMQ就会在给消费者信消息之前等待先前的消息被确认
                channel.basicQos(1);
                int finalI = i;
                //创建消息接收回调函数，以便接收消息
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    //将接受到的消息转为字符串
                    String message = new String(delivery.getBody(), "UTF-8");
                    try{
                        System.out.println("【x】 Received" + "编号" + finalI + " :" + message);
                        //处理工作，模拟处理消息所花费的时间，机器处理能力有限，接受一条消息，20s之后接受下一条消息
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
                        Thread.sleep(20000);
                        // 不使用dowork 模拟消息处理工作
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        //发生异常之后，对取到的消息 拒绝
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
                    }finally {
                        //打印出完成消息处理的时间
                        System.out.println("[x] Done");
                        //手动发送应答，告诉RabbitMQ消息已经被处理
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
                    }
                };
                //开始消费消息，队列名称，是否自动确认，投递回调和消费者取消回调
                channel.basicConsume(TASK_QUEUE_NAME,false,deliverCallback,consumerTag -> {});
            }
        }
}
