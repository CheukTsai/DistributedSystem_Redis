package models;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.*;

public class ChannelPool {
    private Connection connection;
    private BlockingQueue<Channel> pool;
    private final static String QUEUE_NAME = "hello";

    public ChannelPool() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("ec2-54-200-24-157.us-west-2.compute.amazonaws.com");
        factory.setVirtualHost("6650");
        factory.setUsername("zhuocaili");
        factory.setPassword("cs6650lzc");
//        factory.setHost("localhost");
        this.connection = factory.newConnection();
        this.pool = new LinkedBlockingQueue<>();
        int i = 0;
        while(i++ < 512) {
            Channel channel = connection.createChannel();
            pool.add(channel);
        }
        System.out.println(pool.size());
    }

    public Channel getChannel() throws IOException, InterruptedException {
        Channel channel = pool.poll(100, TimeUnit.MILLISECONDS);
        if(channel == null) {
            channel = connection.createChannel();
        }
        return channel;
    }

    public void add(Channel channel) {
        pool.add(channel);
    }
    public BlockingQueue<Channel> getPool() {
        return pool;
    }
}
