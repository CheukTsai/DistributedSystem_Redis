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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Consumer {
    private final static String EXCHANGE_NAME = "lift_ride";
    private final static String QUEUE_NAME = "hello";
    private final static Integer THREADS = 1;
    private static JedisPool pool = null;
    private final static String REDIS_HOST = "localhost";
    private final static Integer REDIS_PORT = 6379;


    public static void main(String[] argv) throws Exception {
        ConcurrentHashMap<String, CopyOnWriteArrayList<LiftRide>> map = new ConcurrentHashMap<>();
        ConnectionFactory factory = new ConnectionFactory();
        Gson gson = new Gson();
        System.out.println("SkierDB Consumer Listening......");
        factory.setHost("ec2-50-112-215-119.us-west-2.compute.amazonaws.com");
        factory.setVirtualHost("6650");
        factory.setUsername("zhuocaili");
        factory.setPassword("cs6650lzc");
        Connection connection = factory.newConnection();
        Runnable runnable = () -> {
            Channel channel;
            try{
                channel = connection.createChannel();
                pool = new JedisPool(REDIS_HOST, REDIS_PORT);

//                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, EXCHANGE_NAME, "");
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    LiftRide liftRide = gson.fromJson(message, LiftRide.class);
                    System.out.println(" [x] Received '" + liftRide.toString() + "'");
                    try{
//                    map.putIfAbsent(liftRide.getSkierID(), new CopyOnWriteArrayList<>());
//                    map.get(liftRide.getSkierID()).add(liftRide);
                        addHash(liftRide);
                    } finally {
                        System.out.println(" [x] Done");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }
                };
                channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });

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
        String key = "s" + liftRide.getSkierID() + "_" + "d" + liftRide.getDayID() + ":" + "s" + liftRide.getSeasonID() +
                "d" + liftRide.getDayID() +"r" +  liftRide.getResortID()+"l" + liftRide.getLiftID() + "t" + liftRide.getTime();
        map.put("skierId", liftRide.getSkierID());
        map.put("resortId", liftRide.getResortID());
        map.put("seasonId", liftRide.getSeasonID());
        map.put("dayId", liftRide.getDayID());
        map.put("liftId", liftRide.getLiftID());
        map.put("vertical", String.valueOf(Integer.parseInt(liftRide.getLiftID()) * 10));
        map.put("time", liftRide.getTime());

        Jedis jedis = pool.getResource();
        try {
            //save to redis
            jedis.hmset(key, map);
//            Map<String, String> retrieveMap = jedis.hgetAll(key);
//            for (String keyMap : retrieveMap.keySet()) {
//                System.out.println(keyMap + " " + retrieveMap.get(keyMap));
//            }

        } catch (JedisException e) {
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            if (null != jedis)
                pool.returnResource(jedis);
        }
    }
}
