import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import models.LiftRide;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Consumer {
    private final static String QUEUE_NAME = "hello";
    private final static Integer THREADS = 1;
    private final static String EXCHANGE_NAME = "lift_ride";
    private static JedisPool pool = null;
    private final static String REDIS_HOST = "localhost";
    private final static Integer REDIS_PORT = 6379;

    public static void main(String[] argv) throws Exception {
        ConcurrentHashMap<String, CopyOnWriteArrayList<LiftRide>> map = new ConcurrentHashMap<>();
        ConnectionFactory factory = new ConnectionFactory();
        Gson gson = new Gson();
        System.out.println("Listening......");
        factory.setHost("ec2-54-200-24-157.us-west-2.compute.amazonaws.com");
        factory.setVirtualHost("6650");
        factory.setUsername("zhuocaili");
        factory.setPassword("cs6650lzc");
//        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        pool = new JedisPool(REDIS_HOST, REDIS_PORT);
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
//                    map.putIfAbsent(liftRide.getSkierID(), new CopyOnWriteArrayList<>());
//                    map.get(liftRide.getSkierID()).add(liftRide);
                    addHash(liftRide);
                };
                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        for (int i = 0; i < THREADS; i++) {
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }

    public static void addHash(LiftRide liftRide) {

        Map<String, String> map = new HashMap<>();
        String key = liftRide.getSkierID() + liftRide.getResortID() + liftRide.getSeasonID() + liftRide.getDayID() + liftRide.getLiftID() + liftRide.getTime() ;
        map.put("skierId", liftRide.getSkierID());
        map.put("resortId", liftRide.getResortID());
        map.put("seasonId", liftRide.getSeasonID());
        map.put("dayId", liftRide.getDayID());
        map.put("liftId", liftRide.getLiftID());
        map.put("time", liftRide.getTime());

        Jedis jedis = pool.getResource();
        try {
            //save to redis
            jedis.hmset(key, map);

            //after saving the data, lets retrieve them to be sure that it has really added in redis
            Map<String, String> retrieveMap = jedis.hgetAll(key);
//            for (String keyMap : retrieveMap.keySet()) {
//                System.out.println(keyMap + " " + retrieveMap.get(keyMap));
//            }

        } catch (JedisException e) {
            //if something wrong happen, return it back to the pool
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            ///it's important to return the Jedis instance to the pool once you've finished using it
            if (null != jedis)
                pool.returnResource(jedis);
        }
    }
}
