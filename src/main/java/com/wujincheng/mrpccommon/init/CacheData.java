package com.wujincheng.mrpccommon.init;

import com.wujincheng.mrpccommon.common.Request;
import com.wujincheng.mrpccommon.entity.ChannelVO;
import com.wujincheng.mrpccommon.entity.RegisterVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class CacheData {
    // 注册的service信息
    public static List<RegisterVO> registerVOs=new ArrayList<>();

    public static final List<String> subscribedServices=new ArrayList<>();

    // ip对应的Channel
    // ip:port,Channel
    public static Map<String,ChannelVO> channelMap=new ConcurrentHashMap<>();

    // 接口名称对应的ip:port实例
    public static Map<String,List<String>> interfaceToIpPort=new ConcurrentHashMap<>();

    //引用的服务
    public static final List<String> interfaceReference=new ArrayList<>();
    public static final Map<String,Object> interfaceReferenceInstance=new ConcurrentHashMap<>();
    //暴露的服务
    public static final List<String> interfaceService=new ArrayList<>();
    public static final Map<String,Object> interfaceServiceInstance=new ConcurrentHashMap<>();
    public static final Map<String,Object> interfaceServiceInstanceNoWarpper=new ConcurrentHashMap<>();

    //缓存class
    public static final Map<String,Class<?>> classCache=new ConcurrentHashMap<>();

    //请求缓存
    public static final Map<Long,Request> requestMap=new ConcurrentHashMap<>();
    //请求id
    public static final AtomicLong id=new AtomicLong(1);

    //工作线程池
    public static final ExecutorService executor =new ThreadPoolExecutor(200, 200, 5, TimeUnit.SECONDS
            , new ArrayBlockingQueue<>(1000), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("mrpc-work");
            return t;
        }
    });
}