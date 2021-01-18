package com.wujincheng.mrpccommon.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class RedisClient {
    private static Jedis jedis=null;

    private static final Logger logger= LoggerFactory.getLogger(RedisClient.class);
    private static final Object obj=new Object();
    public static Jedis getRedisClient(){
        if(jedis!=null){
            return jedis;
        }

        String ip=Config.properties.getProperty("mrpc.redis.ip","127.0.0.1");
        String port=Config.properties.getProperty("mrpc.redis.port","6379");
        try {
            synchronized (obj){
                if(jedis!=null){
                    return jedis;
                }
                jedis = new Jedis(ip, Integer.valueOf(port));
                jedis.connect();
                logger.info("jedis 初始化完成");
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

        return jedis;
    }


    public static void close(){
        if(jedis==null){
            return;
        }
        jedis.close();
        jedis=null;
    }
}