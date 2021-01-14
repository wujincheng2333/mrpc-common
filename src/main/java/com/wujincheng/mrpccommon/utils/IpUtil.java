package com.wujincheng.mrpccommon.utils;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class IpUtil  {

    private static final Logger logger= LoggerFactory.getLogger(IpUtil.class);
    private static String ip=null;

    private static String getUrl() {
        InetAddress address = null;
        try {
            address = NetUtils.getLocalAddress();
            return address.getHostAddress();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public static String getIp(){
        if(ip==null){
            ip=getUrl();
        }
        return ip;
    }
}