package com.wujincheng.mrpccommon.init;

import com.google.gson.reflect.TypeToken;
import com.wujincheng.mrpccommon.annotation.MRPCReference;
import com.wujincheng.mrpccommon.annotation.MRPCReferenceNative;
import com.wujincheng.mrpccommon.annotation.MRPCService;
import com.wujincheng.mrpccommon.annotation.MRPCServiceNative;
import com.wujincheng.mrpccommon.common.*;
import com.wujincheng.mrpccommon.config.Config;
import com.wujincheng.mrpccommon.config.MRPCAutoConfigure;
import com.wujincheng.mrpccommon.config.RedisClient;
import com.wujincheng.mrpccommon.context.RpcContext;
import com.wujincheng.mrpccommon.entity.ChannelVO;
import com.wujincheng.mrpccommon.entity.Heartbeat;
import com.wujincheng.mrpccommon.entity.RegisterVO;
import com.wujincheng.mrpccommon.filter.Filter;
import com.wujincheng.mrpccommon.filter.RemoveRpcContextFliter;
import com.wujincheng.mrpccommon.invoke.Invocation;
import com.wujincheng.mrpccommon.invoke.MyDefaultFuture;
import com.wujincheng.mrpccommon.invoke.MyInvocationHandler;
import com.wujincheng.mrpccommon.invoke.MyInvoker;
import com.wujincheng.mrpccommon.proxy.JavassistProxyFactory;
import com.wujincheng.mrpccommon.proxy.ProxyFactory;
import com.wujincheng.mrpccommon.proxy.Wrapper;
import com.wujincheng.mrpccommon.serviceImpl.MyNettyCommonService1207Impl;
import com.wujincheng.mrpccommon.serviceImpl.MyNettyCommonService1207Native;
import com.wujincheng.mrpccommon.utils.ClassUtils;
import com.wujincheng.mrpccommon.utils.GsonUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

public class MRPCInit {

    private static final Logger logger= LoggerFactory.getLogger(MRPCInit.class);

    private static final ProxyFactory javassistProxyFactory=new JavassistProxyFactory();

    private static boolean springboot=false;

    private static boolean start=false;

    private static final Object objJedis=new Object();

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("mrpc-register");
            return t;
        }
    });
    private static void springbot(){
        try {
            Class.forName("org.springframework.boot.autoconfigure.EnableAutoConfiguration");
            springboot=true;
        }catch (Exception e){
            //
        }
    }
    public static void init(){
        if(start){
            return;
        }
        synchronized (javassistProxyFactory){
            if(start){
                return;
            }
            start=true;
        }
        springbot();
        getSubscribedServices();
        getRegisterVOs();
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    getRegisterVOs();
                }catch (Exception e){
                    //
                }
            }
        },10000L, 10000L,TimeUnit.MILLISECONDS);
        if(!springboot){
            initClass();
            createServer(Config.properties.getProperty("mrpc.port","20800"));
            register();
        }
    }

    public static void initSpringbootAfter(){
        Set<Class<?>> classSet= new HashSet<>(2);
        classSet.add(MyNettyCommonService1207Impl.class);
        classSet.add(MyNettyCommonService1207Native.class);
        initClass(classSet);
        createServer(Config.properties.getProperty("mrpc.port","20800"));
        register();
    }

    private static void getSubscribedServices(){
        CacheData.subscribedServices.clear();
        String str=Config.properties.getProperty("mrpc.subscribedServices","");
        String[] strs=str.split(",");
        if(strs.length>0){
           for(String s:strs){
               if("".equals(s.trim())){
                   continue;
               }
               CacheData.subscribedServices.add(s);
           }
        }
    }

    private static void initClass(Set<Class<?>> classSet){
        for(Class<?> clz:classSet){
            if(clz.getName().contains("$")){
                continue;
            }
            if(clz.getAnnotation(MRPCService.class)!=null){
                handleMRPCService(clz);
            }
            if(clz.getAnnotation(MRPCServiceNative.class)!=null&&!springboot){
                handleMRPCServiceNative(clz);
            }else{
                if("com.wujincheng.mrpccommon.serviceImpl.MyNettyCommonService1207Native".equals(clz.getName())){
                    handleMRPCServiceNative(clz);
                }
            }

        }

        for(Class<?> clz:classSet){
            if(clz.getName().contains("$")){
                continue;
            }
            if(clz.getAnnotation(MRPCService.class)!=null){
                Object obj= CacheData.interfaceServiceInstanceNoWarpper.get(clz.getName());
                fields(clz,obj);
                continue;
            }
            if(springboot&&!"com.wujincheng.mrpccommon.serviceImpl.MyNettyCommonService1207Native".equals(clz.getName())){
                Object obj= CacheData.interfaceServiceInstanceNoWarpper.get(clz.getName());
                if(obj==null&&!clz.isInterface()){
                    obj= MRPCAutoConfigure.applicationContext.getBean(clz);
                    fields(clz,obj);
                }
                continue;
            }
            if(clz.getAnnotation(MRPCServiceNative.class)!=null&&!springboot){
                Object obj= CacheData.interfaceServiceInstance.get(clz.getName());
                fields(clz,obj);
            }else{
                if("com.wujincheng.mrpccommon.serviceImpl.MyNettyCommonService1207Native".equals(clz.getName())){
                    Object obj= CacheData.interfaceServiceInstance.get(clz.getName());
                    fields(clz,obj);
                }
            }

        }
    }

    private static void initClass(){
        String packageName=Config.properties.getProperty("mrpc.packageName","");
        if("".equals(packageName)){
            return;
        }
        Set<Class<?>> classSet= ClassUtil.getClassSet(packageName);
        if(classSet==null||classSet.size()==0){
            return;
        }
        classSet.add(MyNettyCommonService1207Impl.class);
        classSet.add(MyNettyCommonService1207Native.class);
        initClass(classSet);
    }

    public static void fieldsSpringboot(Class<?> clz,Object object){
        try {
            Field[] fields=clz.getDeclaredFields();
            if(fields==null||fields.length==0){
                return;
            }
            for(Field f:fields){
                Class fc=f.getType();
                if(f.getAnnotation(MRPCReference.class)==null){
                    continue;
                }
                if(CacheData.interfaceReferenceInstance.containsKey(fc.getName())){
                    f.setAccessible(true);
                    f.set(object,CacheData.interfaceReferenceInstance.get(fc.getName()));
                    continue;
                }
                MyInvoker myInvoker=new MyInvoker() {
                    @Override
                    public Class getInterface() {
                        return fc;
                    }

                    @Override
                    public Object invoke(Invocation invocation) {

                        String ipport=(String) RpcContext.getValue(Common.RPC_INVOKE_IP_PORT);
                        if(ipport==null){
                            ipport=getIpPort(invocation.getClassName());
                        }
                        if(ipport==null){
                            throw new RuntimeException("无法找到客户端");
                        }
                        Response response=sendRPC02(invocation,ipport);
                        if(response==null){
                            throw new RuntimeException("请求异常");
                        }
                        Map<String,Object> attachments=RpcContext.getValues();
                        if(response.getAttachments()!=null&&!response.getAttachments().isEmpty()){
                            attachments.putAll(response.getAttachments());
                        }
                        if(response.getHasExecption()){
                            Throwable throwable=response.getThrowable();
                            if(throwable!=null){
                                throw new RuntimeException(throwable);
                            }else{
                                throw new RuntimeException("请求异常");
                            }

                        }
                        return response.getData();
                    }
                };
                List<Filter> filterList=new ArrayList<>();
                filterList.add(new RemoveRpcContextFliter());
                final MyInvoker myFilterInvoker=getFilterInvoker(myInvoker,filterList);
                Object fcProxy=javassistProxyFactory.getProxy(fc, new MyInvocationHandler(myFilterInvoker));

                f.setAccessible(true);
                f.set(object,fcProxy);

                CacheData.interfaceReferenceInstance.put(fc.getName(),fcProxy);
                CacheData.interfaceReference.add(fc.getName());
            }
        }catch (Throwable e){
            //
        }
    }

    private static void fields(Class<?> clz,Object object){
        try {
            Field[] fields=clz.getDeclaredFields();
            if(fields==null||fields.length==0){
                return;
            }
            for(Field f:fields){
                Class fc=f.getType();
                if(f.getAnnotation(MRPCReferenceNative.class)!=null){
                    Object objNative=CacheData.interfaceServiceInstance.get(fc.getName());
                    if(objNative==null){
                        continue;
                    }
                    f.setAccessible(true);
                    f.set(object,objNative);
                    continue;
                }
                if(f.getAnnotation(MRPCReference.class)==null){
                    continue;
                }
                if(CacheData.interfaceReferenceInstance.containsKey(fc.getName())){
                    f.setAccessible(true);
                    f.set(object,CacheData.interfaceReferenceInstance.get(fc.getName()));
                    continue;
                }
                MyInvoker myInvoker=new MyInvoker() {
                    @Override
                    public Class getInterface() {
                        return fc;
                    }

                    @Override
                    public Object invoke(Invocation invocation) {

                        String ipport=(String) RpcContext.getValue(Common.RPC_INVOKE_IP_PORT);
                        if(ipport==null){
                            ipport=getIpPort(invocation.getClassName());
                        }
                        if(ipport==null){
                            throw new RuntimeException("无法找到客户端");
                        }
                        Response response=sendRPC02(invocation,ipport);
                        if(response==null){
                            throw new RuntimeException("请求异常");
                        }
                        Map<String,Object> attachments=RpcContext.getValues();
                        if(response.getAttachments()!=null&&!response.getAttachments().isEmpty()){
                            attachments.putAll(response.getAttachments());
                        }
                        if(response.getHasExecption()){
                            Throwable throwable=response.getThrowable();
                            if(throwable!=null){
                                throw new RuntimeException(throwable);
                            }else{
                                throw new RuntimeException("请求异常");
                            }

                        }
                        return response.getData();
                    }
                };
                List<Filter> filterList=new ArrayList<>();
                filterList.add(new RemoveRpcContextFliter());
                final MyInvoker myFilterInvoker=getFilterInvoker(myInvoker,filterList);
                Object fcProxy=javassistProxyFactory.getProxy(fc, new MyInvocationHandler(myFilterInvoker));

                f.setAccessible(true);
                f.set(object,fcProxy);

                CacheData.interfaceReferenceInstance.put(fc.getName(),fcProxy);
                CacheData.interfaceReference.add(fc.getName());
            }
        }catch (Throwable e){
            //
        }

    }

    public static void handleClientRPC(Response response,ChannelHandlerContext ctx){
        if(CacheData.requestMap.containsKey(response.getId())){
            if(MyDefaultFuture.containsFuture(response.getId())){
                MyDefaultFuture.received(response);
            }
        }
    }

    public static void handleServerRPC(Response response,ChannelHandlerContext ctx){
        CacheData.executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String,Object> attachments=RpcContext.getValues();
                    attachments.put(Common.CHANNEL_CONTEXT,ctx);
                    Request request= rpc(response);
                    if(request!=null){
                        ctx.writeAndFlush(request);
                    }
                }catch (Exception e){
                    logger.error(e.getMessage(),e);
                }
            }
        });
    }

    private static Request rpc(Response response){
        Request request =new Request(response.getId(), response.getType(),"");
        request.setClassName(response.getClassName());
        request.setHasExecption(response.getHasExecption());
        request.setParameter(response.getParameter());
        request.setParameterClassType(response.getParameterClassType());
        request.setThrowable(response.getThrowable());
        request.setMethodName(response.getMethodName());
        request.setAttachments(response.getAttachments());
        MyInvoker myInvoker=(MyInvoker)CacheData.interfaceServiceInstance.get(request.getClassName());
        try {
            Invocation invocation=new Invocation();
            invocation.setClassName(request.getClassName());
            invocation.setParameterClassType(request.getParameterClassType());
            invocation.setParameter(request.getParameter());
            invocation.setMethodName(request.getMethodName());

            Map<String,Object> attachments=RpcContext.getValues();
            if(request.getAttachments()!=null&&!request.getAttachments().isEmpty()){
                attachments.putAll(request.getAttachments());
            }

            //invocation.setAttachments(attachments);
            Object object=myInvoker.invoke(invocation);
            Map<String,String> reqAttachments=new HashMap<>();
            for(Map.Entry<String,Object> m:invocation.getAttachments().entrySet()){
                if(m.getValue() instanceof String){
                    reqAttachments.put(m.getKey(),(String)m.getValue());
                }
            }
            request.setAttachments(reqAttachments);
            request.setData(object);
        }catch (Throwable t){
            t.getStackTrace();
            request.setHasExecption(true);
            request.setThrowable(t);
        }
        return request;
    }


    private static Response sendRPC02(Invocation invocation,String ipport){
        boolean simpleClient=(boolean)invocation.getAttachments().getOrDefault(Common.SIMPLE_CLIENT,false) ;
        ChannelVO simpleChannelVO=null;
        Channel channel=null;
        if(simpleClient){
            String[] str=ipport.split(":");
            simpleChannelVO=createSimpleClientChannel(str[0],str[1]);
            if(simpleChannelVO==null){
                throw new RuntimeException("simpleChannelVO无法连接客户端:"+ipport);
            }
            channel=simpleChannelVO.channel;
        }else {
            if(!CacheData.channelMap.containsKey(ipport)){
                synchronized (ipport.intern()){
                    if(!CacheData.channelMap.containsKey(ipport)){
                        String[] str=ipport.split(":");
                        ChannelVO channelVO=createClientChannel(str[0],str[1]);
                        if(channelVO!=null){
                            CacheData.channelMap.put(ipport,channelVO);
                        }
                    }
                }
                if(!CacheData.channelMap.containsKey(ipport)){
                    throw new RuntimeException("无法找到客户端:"+ipport);
                }
            }
            channel=CacheData.channelMap.get(ipport).channel;
        }


        Request request=new Request(CacheData.id.getAndIncrement(),Common.RPC,"");
        request.setClassName(invocation.getClassName());
        request.setMethodName(invocation.getMethodName());
        request.setParameterClassType(invocation.getParameterClassType());
        request.setParameter(invocation.getParameter());
        Map<String,String> reqAttachments=new HashMap<>();
        for(Map.Entry<String,Object> m:invocation.getAttachments().entrySet()){
            if(m.getValue() instanceof String){
                reqAttachments.put(m.getKey(),(String)m.getValue());
            }
        }
        request.setAttachments(reqAttachments);
        try {
            CacheData.requestMap.put(request.getId(),request);
            Future<Object> future= MyDefaultFuture.newFuture(request.getId(),30000);
            channel.writeAndFlush(request);
            try {
                Object obj=future.get(30000,TimeUnit.MILLISECONDS);
                return (Response)obj;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(),e.getCause());
            }
        }finally {
            CacheData.requestMap.remove(request.getId());
            if(simpleClient){
                closeClientChannel(simpleChannelVO);
            }
        }
    }

    private static String getIpPort(String clzName){
        List<String> ipports=CacheData.interfaceToIpPort.get(clzName);
        if(ipports==null||ipports.size()==0){
            return null;
        }
        if(ipports.size()==1){
            return ipports.get(0);
        }
        return ipports.get(ThreadLocalRandom.current().nextInt(ipports.size()));
    }

    private static void handleMRPCServiceNative(Class<?> clz){
        if(clz.getAnnotation(MRPCService.class)!=null){
           return;
        }
        try {
            Object obj=clz.newInstance();
            //fields(clz,obj);
            CacheData.interfaceServiceInstance.put(clz.getName(),obj);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }



    public static void handleMRPCServiceSpringboot(Class<?> clz,Object fobj){

        Class<?>[] interfaces=clz.getInterfaces();
        if(interfaces==null||interfaces.length==0){
            return;
        }

        try {
            Class<?> interfaceClass= ClassUtils.forName(interfaces[0].getName());
            if(CacheData.interfaceServiceInstance.containsKey(interfaceClass.getName())){
                return;
            }
            //fields(clz,obj);
            CacheData.interfaceServiceInstanceNoWarpper.put(clz.getName(),fobj);
            final Wrapper wrapperObject= Wrapper.getWrapper(clz);
            MyInvoker myWrapperInvoker=new MyInvoker() {
                @Override
                public Class getInterface() {
                    return interfaceClass;
                }

                @Override
                public Object invoke(Invocation invocation) {
                    Class<?>[] parameterTypes=new Class<?>[invocation.getParameterClassType().length];
                    int i=0;
                    for(String cn:invocation.getParameterClassType()){
                        try {
                            Class<?> c=CacheData.classCache.get(cn);
                            if(c==null){
                                synchronized (CacheData.classCache){
                                    c=CacheData.classCache.get(cn);
                                    if(c==null){
                                        c=ClassUtils.forName(cn);
                                        CacheData.classCache.putIfAbsent(cn,c);
                                    }
                                }
                            }
                            parameterTypes[i]=CacheData.classCache.get(cn);
                        } catch (ClassNotFoundException e) {
                            throw new  RuntimeException(e.getMessage(),e.getCause());
                        }
                        i++;
                    }
                    try {

                        return wrapperObject.invokeMethod(fobj,invocation.getMethodName(),parameterTypes,invocation.getParameter());
                    } catch (InvocationTargetException e) {
                        throw new  RuntimeException(e.getMessage(),e.getCause());
                    }

                }
            };
            List<Filter> filterList=new ArrayList<>();
            filterList.add(new RemoveRpcContextFliter());
            final MyInvoker myFilterWrapperInvoker=getFilterInvoker(myWrapperInvoker,filterList);
            CacheData.interfaceServiceInstance.put(interfaceClass.getName(),myFilterWrapperInvoker);
            CacheData.interfaceService.add(interfaceClass.getName());
        } catch (Exception e) {
            //
        }
    }

    private static void handleMRPCService(Class<?> clz){

        Class<?>[] interfaces=clz.getInterfaces();
        if(interfaces==null||interfaces.length==0){
            return;
        }

        try {
            Class<?> interfaceClass= ClassUtils.forName(interfaces[0].getName());
            if(CacheData.interfaceServiceInstance.containsKey(interfaceClass.getName())){
                return;
            }
            Object obj=null;
            if(springboot&&!"com.wujincheng.mrpccommon.service.MyNettyCommonService1207".equals(interfaceClass.getName())){
                obj= MRPCAutoConfigure.applicationContext.getBean(interfaceClass);
            }else{
                obj=clz.newInstance();
            }
            final Object fobj=obj;
            //fields(clz,obj);
            CacheData.interfaceServiceInstanceNoWarpper.put(clz.getName(),fobj);
            final Wrapper wrapperObject= Wrapper.getWrapper(clz);
            MyInvoker myWrapperInvoker=new MyInvoker() {
                @Override
                public Class getInterface() {
                    return interfaceClass;
                }

                @Override
                public Object invoke(Invocation invocation) {
                    Class<?>[] parameterTypes=new Class<?>[invocation.getParameterClassType().length];
                    int i=0;
                    for(String cn:invocation.getParameterClassType()){
                        try {
                            Class<?> c=CacheData.classCache.get(cn);
                            if(c==null){
                                synchronized (CacheData.classCache){
                                    c=CacheData.classCache.get(cn);
                                    if(c==null){
                                        c=ClassUtils.forName(cn);
                                        CacheData.classCache.putIfAbsent(cn,c);
                                    }
                                }
                            }
                            parameterTypes[i]=CacheData.classCache.get(cn);
                        } catch (ClassNotFoundException e) {
                            throw new  RuntimeException(e.getMessage(),e.getCause());
                        }
                        i++;
                    }
                    try {

                        return wrapperObject.invokeMethod(fobj,invocation.getMethodName(),parameterTypes,invocation.getParameter());
                    } catch (InvocationTargetException e) {
                        throw new  RuntimeException(e.getMessage(),e.getCause());
                    }

                }
            };
            List<Filter> filterList=new ArrayList<>();
            filterList.add(new RemoveRpcContextFliter());
            final MyInvoker myFilterWrapperInvoker=getFilterInvoker(myWrapperInvoker,filterList);
            CacheData.interfaceServiceInstance.put(interfaceClass.getName(),myFilterWrapperInvoker);
            CacheData.interfaceService.add(interfaceClass.getName());
        } catch (Exception e) {
           //
        }
    }

    private static void register(){
        Jedis jedis= RedisClient.getRedisClient();
        RegisterVO registerVO=new RegisterVO();
        registerVO.setInterfaces(CacheData.interfaceService);
        registerVO.setTime(System.currentTimeMillis());
        registerVO.setIp(Config.properties.getProperty("mrpc.ip"));
        registerVO.setPort(Config.properties.getProperty("mrpc.port","20800"));
        registerVO.setServiceName(Config.properties.getProperty("mrpc.serviceName"));
        String jsonStr=GsonUtils.gson.toJson(registerVO);
        synchronized (objJedis){
            jedis.hset(Common.REGISTER_KEY,registerVO.getServiceName()+"@"+registerVO.getIp()+":"+registerVO.getPort(),jsonStr);
        }
        logger.info("注册成功:[{}]",jsonStr);


        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    registerVO.setTime(System.currentTimeMillis());
                    String jsonStr=GsonUtils.gson.toJson(registerVO);
                    synchronized (objJedis){
                        jedis.hset(Common.REGISTER_KEY,registerVO.getServiceName()+"@"+registerVO.getIp()+":"+registerVO.getPort(),jsonStr);
                    }
                }catch (Exception e){
                    //
                }
            }
        },10000L, 10000L,TimeUnit.MILLISECONDS);
    }

    private static void getRegisterVOs(){
        //CacheData.registerVOs.clear();
        Jedis jedis= RedisClient.getRedisClient();
        Map<String, String> map =null;
        synchronized (objJedis){
            map = jedis.hgetAll(Common.REGISTER_KEY);
        }
        if(map==null||map.isEmpty()){
            return;
        }
        RegisterVO registerVO=null;

        Map<String,RegisterVO> registerVOMap=new HashMap<>();

        Map<String,RegisterVO> registerVOMapNew=new HashMap<>();
        Map<String,RegisterVO> registerVOMapAdd=new HashMap<>();

        for(RegisterVO v:CacheData.registerVOs){
            registerVOMap.put(v.getIp()+":"+v.getPort(),v);
        }
        for(Map.Entry<String,String> m:map.entrySet()){
            if(m.getKey().startsWith(Config.properties.getProperty("mrpc.serviceName")+"@")){
                continue;
            }
            String value=m.getValue();
            registerVO= GsonUtils.gson.fromJson(value,new TypeToken<RegisterVO>(){}.getType());
            if(registerVO==null){
                continue;
            }
            if(CacheData.subscribedServices.size()>0&&!CacheData.subscribedServices.contains(registerVO.getServiceName())){
                continue;
            }
            if(System.currentTimeMillis()-registerVO.getTime()>=15000){
                continue;
            }
            registerVOMapNew.put(registerVO.getIp()+":"+registerVO.getPort(),registerVO);
            if(registerVOMap.containsKey(registerVO.getIp()+":"+registerVO.getPort())){
                registerVOMap.remove(registerVO.getIp()+":"+registerVO.getPort());
            }else{
                registerVOMapAdd.put(registerVO.getIp()+":"+registerVO.getPort(),registerVO);
            }

            List<String> interfaces=registerVO.getInterfaces();
            for(String interfaceName:interfaces){
                List<String> ipports=CacheData.interfaceToIpPort.get(interfaceName);
                if(ipports==null){
                    ipports=new ArrayList<>();
                    CacheData.interfaceToIpPort.put(interfaceName,ipports);
                }
                if(ipports.contains(registerVO.getIp()+":"+registerVO.getPort())){
                    continue;
                }
                ipports.add(registerVO.getIp()+":"+registerVO.getPort());
            }
        }
        List<RegisterVO> list=new ArrayList<>(registerVOMapNew.size());
        for(Map.Entry<String,RegisterVO> m:registerVOMapNew.entrySet()){
            list.add(m.getValue());
        }
        CacheData.registerVOs=list;

        if(!registerVOMap.isEmpty()){
            for(Map.Entry<String,RegisterVO> m:registerVOMap.entrySet()){
                List<String> interfaces=m.getValue().getInterfaces();
                for(String in:interfaces){
                    List<String> ipports=CacheData.interfaceToIpPort.get(in);
                    ipports.remove(m.getValue().getIp()+":"+m.getValue().getPort());
                }

                ChannelVO channelVO=CacheData.channelMap.remove(m.getValue().getIp()+":"+m.getValue().getPort());
                closeClientChannel(channelVO);
                logger.info("移除服务:[{}]",m.getValue().toString());
            }
        }

        for(Map.Entry<String,RegisterVO> m:registerVOMapAdd.entrySet()){
            logger.info("新增服务:[{}]",m.getValue().toString());
        }
    }
    private static void createServer(String port){
       final CountDownLatch countDownLatch=new CountDownLatch(1);
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //接受请求线程组
                    EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("MRPCNttyServerBoss", true));
                    //工作线程组
                    EventLoopGroup workerGroup = new NioEventLoopGroup(Math.min(Runtime.getRuntime().availableProcessors() + 1, 32),
                            new DefaultThreadFactory("MRPCNettyServerWorker", true));
                    try {

                        //服务端开发相关对象
                        ServerBootstrap serverBootstrap = new ServerBootstrap();
                        //绑定两个线程组
                        serverBootstrap.group(bossGroup, workerGroup)
                                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                                .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)

                                //绑定channel模式 通过反射完成实例化
                                .channel(NioServerSocketChannel.class)
                                //.option(ChannelOption.SO_BACKLOG, 1024)
                                //构造初始化
                                .childHandler(new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                                        ChannelPipeline channelPipeline = socketChannel.pipeline();
                                        channelPipeline.addLast(new MyDecoder());
                                        channelPipeline.addLast(new MyEncoder());
                                        //业务处理
                                        channelPipeline.addLast(new SocketServerHandler());
                                    }
                                });
                        //同步绑定端口并启动监听
                        ChannelFuture channelFuture = serverBootstrap.bind("0.0.0.0",Integer.valueOf(port)).sync();
                        //logger.info("监听8080...");
                        logger.info("监听[{}]...",port);
                        countDownLatch.countDown();
                        //同步监听关闭 这里阻塞
                        channelFuture.channel().closeFuture().sync();
                    } catch (Throwable e) {
                        logger.error("服务器异常停止",e);
                    } finally {
                        //退出优雅关闭线程组
                        bossGroup.shutdownGracefully();
                        workerGroup.shutdownGracefully();
                    }
                    System.exit(1);
                }catch (Exception e){
                    //
                }
            }
        });
        t.setDaemon(true);
        t.start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            //
        }
    }
    private static ChannelVO createClientChannel(String ip, String port){
        try {
            EventLoopGroup eventLoopGroup=new NioEventLoopGroup(Math.min(Runtime.getRuntime().availableProcessors() + 1, 32), new DefaultThreadFactory("MRPCNettyClientWorker", true));
            Bootstrap bootstrap=new Bootstrap();
            bootstrap.group(eventLoopGroup)

                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)

                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline channelPipeline = ch.pipeline();
                            channelPipeline.addLast(new MyDecoder());
                            channelPipeline.addLast(new MyEncoder());
                            channelPipeline.addLast(new SocketClientHandler());
                        }
                    });
            ChannelFuture channelFuture=bootstrap.connect(ip,Integer.valueOf(port)).sync();
            return new ChannelVO(channelFuture.channel(),eventLoopGroup);
        }catch (Exception e){
            //logger.error(e.getMessage(),e);
        }
        return null;
    }

    private static ChannelVO createSimpleClientChannel(String ip, String port){
        try {
            EventLoopGroup eventLoopGroup=new NioEventLoopGroup(1, new DefaultThreadFactory("MRPCNettySimpleClientWorker", true));
            Bootstrap bootstrap=new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline channelPipeline = ch.pipeline();
                            channelPipeline.addLast(new MyDecoder());
                            channelPipeline.addLast(new MyEncoder());
                            channelPipeline.addLast(new SocketSimpleClientHandler());
                        }
                    });
            ChannelFuture channelFuture=bootstrap.connect(ip,Integer.valueOf(port)).sync();
            //channelFuture.addListener(ChannelFutureListener.CLOSE);
            return new ChannelVO(channelFuture.channel(),eventLoopGroup);
        }catch (Exception e){
            //logger.error(e.getMessage(),e);
        }
        return null;
    }

//    private static ScheduledExecutorService schedulerCloseClientChannel = Executors.newScheduledThreadPool(1, new ThreadFactory() {
//        @Override
//        public Thread newThread(Runnable r) {
//            Thread t = new Thread(r);
//            t.setDaemon(true);
//            t.setName("mrpc-schedulerCloseClientChannel");
//            return t;
//        }
//    });

    public static void closeClientChannel(ChannelVO channelVO){
        if(channelVO==null){
            return;
        }
        try {
//            schedulerCloseClientChannel.submit(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        channelVO.channel.close();
//                        channelVO.eventLoopGroup.shutdownGracefully();
//                    }catch (Exception e){
//                        logger.error(e.getMessage(),e);
//                    }
//                }
//            });
            channelVO.channel.close();
            channelVO.eventLoopGroup.shutdownGracefully();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

    }

    private static MyInvoker getFilterInvoker(MyInvoker lastInvoker,List<Filter> filters){
        for(int i=filters.size()-1;i>=0;i--){
            final Filter filter=filters.get(i);
            final MyInvoker nextInvoker=lastInvoker;
            lastInvoker=new MyInvoker() {
                @Override
                public Class getInterface() {
                    return nextInvoker.getInterface();
                }

                @Override
                public Object invoke(Invocation invocation) {
                    return filter.invoke(nextInvoker,invocation);
                }
            };
        }
        return lastInvoker;
    }

    public static void startHeartbeat(String ip,String port){
        Heartbeat heartbeat=new Heartbeat();
        heartbeat.setIp(Config.properties.getProperty("mrpc.ip"));
        heartbeat.setPort(Config.properties.getProperty("mrpc.port"));
        Runnable r=new Runnable() {
            @Override
            public void run() {
                try {
                    MyNettyCommonService1207Native myNettyCommonService1207=(MyNettyCommonService1207Native)CacheData.interfaceServiceInstance.get(MyNettyCommonService1207Native.class.getName());
                    RpcContext.setValue(Common.RPC_INVOKE_IP_PORT,ip+":"+port);
                    myNettyCommonService1207.heatbeat(heartbeat);
                    scheduler.schedule(this,15000,TimeUnit.MILLISECONDS);
                }catch (Exception e){
                    //
                    logger.info("心跳退出:[{}]",ip+":"+port);
                }
            }
        };
        scheduler.schedule(r,15000,TimeUnit.MILLISECONDS);
    }
}