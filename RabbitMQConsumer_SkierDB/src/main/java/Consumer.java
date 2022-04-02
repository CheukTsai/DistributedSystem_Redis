import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import models.LiftRide;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Consumer {
    private final static String EXCHANGE_NAME = "lift_ride";
    private final static String QUEUE_NAME = "hello";
    private final static Integer THREADS = 100;


    public static void main(String[] argv) throws Exception {
        ConcurrentHashMap<String, CopyOnWriteArrayList<LiftRide>> map = new ConcurrentHashMap<>();
        ConnectionFactory factory = new ConnectionFactory();
        Gson gson = new Gson();
        System.out.println("Listening......");
        factory.setHost("ec2-54-200-24-157.us-west-2.compute.amazonaws.com");
        factory.setVirtualHost("6650");
        factory.setUsername("zhuocaili");
        factory.setPassword("cs6650lzc");
//        JedisMain jedisMain = new JedisMain("localhost", 6379);
//        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Runnable runnable = () -> {
            Channel channel;
            try{
                channel = connection.createChannel();

//                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, EXCHANGE_NAME, "");
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    LiftRide liftRide = gson.fromJson(message, LiftRide.class);
                    System.out.println(" [x] Received '" + liftRide.toString() + "'");
                    map.putIfAbsent(liftRide.getSkierID(), new CopyOnWriteArrayList<>());
                    map.get(liftRide.getSkierID()).add(liftRide);
//                    synchronized(LiftRide.class) {
//
//                    }

                };
                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        for (int i = 0; i < 1; i++) {
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }
}
