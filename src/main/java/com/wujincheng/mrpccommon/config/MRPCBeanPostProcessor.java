package com.wujincheng.mrpccommon.config;

import com.wujincheng.mrpccommon.annotation.MRPCService;
import com.wujincheng.mrpccommon.init.MRPCInit;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class MRPCBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        MRPCService mrpcService=bean.getClass().getAnnotation(MRPCService.class);
        if(mrpcService!=null){
            MRPCInit.handleMRPCServiceSpringboot(bean.getClass(),bean);
        }
        MRPCInit.fieldsSpringboot(bean.getClass(),bean);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        return bean;
    }
}