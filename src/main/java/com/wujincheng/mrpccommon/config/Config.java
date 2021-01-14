package com.wujincheng.mrpccommon.config;

import com.wujincheng.mrpccommon.utils.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final Logger logger= LoggerFactory.getLogger(Config.class);

    public static Properties properties=getConfig();

    private static Properties getConfig() {
        Properties properties = new Properties();
        //jar同级目录下存在配置文件，则使用jar下的配置文件
        File file = new File("config.properties");
        try {
            InputStream is=null;
            if(file.exists()){
                is = new FileInputStream(file);
            }else{
                String location = "config.properties";
                String path=Config.class.getClassLoader().getResource(location).getFile();
                logger.info("path:[{}]",path);
                file= new File(path);
                is = new FileInputStream(file);
            }
            properties.load(is);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
            return null;
        }
        if("".equals(properties.getProperty("mrpc.ip",""))){
            properties.setProperty("mrpc.ip", IpUtil.getIp());
        }
        return properties;
    }
}