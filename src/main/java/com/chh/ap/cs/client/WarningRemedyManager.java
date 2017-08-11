package com.chh.ap.cs.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarningRemedyManager {
	private static final Logger log = LoggerFactory.getLogger(WarningRemedyManager.class);
	
	private Map<String, Runnable> taskMap;
	
	
	public void start(){
	    log.info("开始启动各服务");
		for (Map.Entry<String, Runnable> entry : taskMap.entrySet()) {
            String key = entry.getKey();
            Runnable task = entry.getValue();
            try {
            	(new Thread(task)).start();
            	log.info("服务[{}]启动完毕", key);
            } catch (Exception e) {
                log.info("服务[" + key + "]启动失败", e);
            }
        }
	}
	
	
	public Map<String, Runnable> getTaskMap() {
		return taskMap;
	}

	public void setTaskMap(Map<String, Runnable> taskMap) {
		this.taskMap = taskMap;
	}

}
