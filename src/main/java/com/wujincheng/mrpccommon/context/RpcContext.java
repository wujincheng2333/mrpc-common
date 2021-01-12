package com.wujincheng.mrpccommon.context;

import java.util.HashMap;
import java.util.Map;

public class RpcContext {
    private static final ThreadLocal<Map<String,Object>> rpcContext=new ThreadLocal<>();

    public static void setValue(String key,Object value){
        Map<String,Object> map=rpcContext.get();
        if(map==null){
            map=new HashMap<>();
            rpcContext.set(map);
        }
        map.put(key,value);
    }

    public static Object getValue(String key){
        Map<String,Object> map=rpcContext.get();
        if(map==null){
            return null;
        }
        return map.get(key);
    }

    public static Map<String,Object> getValues(){
        Map<String,Object> map=rpcContext.get();
        if(map==null){
            map=new HashMap<>();
            rpcContext.set(map);
        }
        return map;
    }

    public static Object removeValue(String key){
        Map<String,Object> map=rpcContext.get();
        if(map==null){
            return null;
        }
        return map.remove(key);
    }

    public static void removeContext(){
        rpcContext.remove();
    }

    public static String toContextString(){
        return getValues().toString();
    }
}