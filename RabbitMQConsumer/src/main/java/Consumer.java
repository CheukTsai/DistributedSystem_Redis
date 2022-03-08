import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import models.LiftRide;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer {
    private final static String QUEUE_NAME = "hello";
    private final static Integer THREADS = 10;
    private final static AtomicInteger count = new AtomicInteger(0);


    public static void main(String[] argv) throws Exception {
        ConcurrentHashMap<String, List<LiftRide>> map = new ConcurrentHashMap<>();
        ConnectionFactory factory = new ConnectionFactory();
        Gson gson = new Gson();
        factory.setHost("ec2-35-162-99-87.us-west-2.compute.amazonaws.com");
        factory.setVirtualHost("6650");
        factory.setUsername("zhuocaili");
        factory.setPassword("cs6650lzc");
//        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Runnable runnable = () -> {
            Channel channel;
            try{
                channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    LiftRide liftRide = gson.fromJson(message, LiftRide.class);
                    System.out.println(" [x] Received '" + liftRide.toString() + "'");
                    map.putIfAbsent(liftRide.getSkierID(), new ArrayList<>());
                    map.get(liftRide.getSkierID()).add(liftRide);
                };
                channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        for (int i = 0; i < THREADS; i++) {
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }
}
