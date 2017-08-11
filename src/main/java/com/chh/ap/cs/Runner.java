package com.chh.ap.cs;

import java.util.Map;

import com.chh.ap.cs.client.WarningRemedyManager;
import com.chh.ap.cs.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.chh.ap.cs.client.SessionMonitor;

/**
 * 服务启动类
 * <p>
 * Created by Niow on 2016/6/22.
 */
@Configuration
@ImportResource({"classpath:spring.xml"})
public class Runner {
	
    private static final Logger log = LoggerFactory.getLogger(Runner.class);

    private static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        log.info("Connection Server开启启动加载");
        applicationContext = SpringApplication.run(Runner.class, args);
        log.info("Spring注入完毕");
        //开始 断链告警丢失处理、失联告警丢失处理 监控进程
        log.info("开始启动各服务");
        WarningRemedyManager warningRemedyManager = getBean("warningRemedyManager", WarningRemedyManager.class);
        warningRemedyManager.start();
        
        
        Map<String, Server> serverMap = (Map<String, Server>) applicationContext.getBean("serverMap");
        
        log.info("开始启动各服务");
        SessionMonitor sessionMonitor = getBean("sessionMonitor", SessionMonitor.class);
        sessionMonitor.start();
        for (Map.Entry<String, Server> entry : serverMap.entrySet()) {
            String serverName = entry.getKey();
            Server server = entry.getValue();
            try {
                if (!server.isRunning()) {
                    server.start();
                }
                log.info("服务[{}]启动完毕", serverName);
            } catch (Exception e) {
                log.info("服务[" + serverName + "]启动失败", e);
            }
        }
        
    }


    public static final <T> T getBean(String beanName, Class<T> clazz) {
        return applicationContext.getBean(beanName, clazz);
    }
}
