package com.wujincheng.mrpccommon.config;


import com.wujincheng.mrpccommon.init.MRPCInit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MRPCAutoConfigure{

    public static ApplicationContext applicationContext=null;

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    public MRPCAutoConfigure(ApplicationContext context) {
        applicationContext = context;
        MRPCInit.init();
    }

    @Bean
    public MRPCBeanPostProcessor mrpcBeanPostProcessor(){
        MRPCBeanPostProcessor mrpcBeanPostProcessor=new MRPCBeanPostProcessor();
        autowireCapableBeanFactory.autowireBean(mrpcBeanPostProcessor);
        return mrpcBeanPostProcessor;

    }

    @Bean
    public MyCommandLineRunner myCommandLineRunner(){
        return new MyCommandLineRunner();
    }

}