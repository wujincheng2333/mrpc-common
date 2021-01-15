package com.wujincheng.mrpccommon.config;

import com.wujincheng.mrpccommon.init.MRPCInit;
import org.springframework.boot.CommandLineRunner;

public class MyCommandLineRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        MRPCInit.initSpringbootAfter();
    }

}