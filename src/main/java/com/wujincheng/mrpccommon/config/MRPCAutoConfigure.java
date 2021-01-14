package com.wujincheng.mrpccommon.config;


import com.wujincheng.mrpccommon.init.MRPCInit;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MRPCAutoConfigure {

    public static ApplicationContext applicationContext=null;

    public MRPCAutoConfigure(ApplicationContext context) {
        applicationContext = context;
        MRPCInit.init();
    }
}