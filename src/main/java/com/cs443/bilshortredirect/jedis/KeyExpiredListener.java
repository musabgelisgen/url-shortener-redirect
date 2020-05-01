package com.cs443.bilshortredirect.jedis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPubSub;

@Component
public class KeyExpiredListener extends JedisPubSub {

    @Autowired
    private JedisPublisher jedisPublisher;

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        System.out.println("onPSubscribe: " + pattern + ", Sub count: " + subscribedChannels);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        System.out.println("onPMessage: Pattern: " + pattern + ", Channel: " + channel + " - " + message);

        if (channel.endsWith("expired")){
            int startIndex = message.indexOf(':') + 1;
            String code = message.substring(startIndex);
            jedisPublisher.syncDatabaseWithRedis(code);
        }
    }





}
