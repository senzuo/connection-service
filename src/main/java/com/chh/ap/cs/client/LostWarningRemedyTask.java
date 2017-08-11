package com.chh.ap.cs.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chh.ap.cs.util.cache.Cache;
import com.chh.ap.cs.util.cache.redis.RedisHashCache;

public class LostWarningRemedyTask implements Runnable{
	
	private static final Logger log = LoggerFactory.getLogger(LostWarningRemedyTask.class);
	private int sessionTime;
	
	private int clientTypeId;
	
	//
	private Cache<List,Object> sessionCodeMapFixCache;
	
	private Cache<Object,Object> notificationCache;
	

	
	@Override
	public void run() {
		try {
			log.info("线程断链启动开始等待,时间为:"+sessionTime +"秒");
			//1.等待Time
			Thread.sleep(sessionTime * 1000);
			log.info("线程断链启动开始");
			RedisHashCache<List,Object> fixCache =(RedisHashCache<List,Object>)sessionCodeMapFixCache;
			String key=ClientType.typeNameMap.get(clientTypeId);
			Map<String,String> fields = fixCache.getAllFields(key);
			Iterator iterator=fields.entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry<String, String> entry=(Entry<String, String>) iterator.next();
				String Id=String.valueOf(entry.getKey());
				IoSession ioSession =SessionManager.getInstance().getSessionById(Id);
				//如果得到的会话为null，那么输出报警和删除redis缓存会话
				if(ioSession==null){
					//输出报警
					Map<String, Object> map = new HashMap<String, Object>();
		            /*map.put("device_id", Id);
		            map.put("utctime", new Date());
		            map.put("alarm_type", "lost");*/
//		            ClientType clientType = sessInfo.getClientType();
//		            String clientTypeName = clientType.getClientTypeName();
					//map.put("device_uid", clientTypeId+Id);
					map.put("device_uid", Id);
					map.put("warning_type", 17);
					map.put("warning_value", System.currentTimeMillis());
					map.put("warning_desc", "五分钟断链告警(内部告警,请无视)");
					map.put("collection_time", new Date());
		            notificationCache.put(SessionMonitor.clientMapALarm.get(clientTypeId), map);
		           
		            //从sessionCodeMapFixCache删除对应的记录
					List keyFile= new ArrayList();
					keyFile.add(ClientType.typeNameMap.get(clientTypeId));
					keyFile.add(Id);
					fixCache.remove(keyFile);
				}
				
			}
			log.info("线程断链启动并且处理完毕");
		} catch (InterruptedException e) {
			log.error("线程断链异常", e);
		}
		//比较
		//从sessionCodeMapFixCache中取出所有的fields
		//sessionCodeMapFixCache 判断类型是不是 RedisHashCache，不是的话写错误日志
//		RedisHashCache<List,Object> fixCache =(RedisHashCache<List,Object>)sessionCodeMapFixCache;
//		String key = "HTWX_OBD"; //ClientType.getTypeName();
//		String key=ClientType.typeNameMap.get(clientTypeId);
//		Map<String,String> fields = fixCache.getAllFields(key);
		
		/*for(fields){
			//field
			//从sesionMap中判断是否存在
			//SessionManager.getInstance().getSessionById(Id);
			//如果不存在
			
			//输出报警
			Map<String, Object> map = new HashMap<String, Object>();
            map.put("device_id", sessInfo.getId());
            map.put("utctime", new Date());
            map.put("alarm_type", "lost");
            ClientType clientType = sessInfo.getClientType();
            String clientTypeName = clientType.getClientTypeName();
            notificationCache.put(SessionMonitor.clientMapALarm.get(clientType.getType()), map);
			
			//从sessionCodeMapFixCache删除对弈的记录
			List key = new ArrayList<String,String>();
			key.add(设备类型);
			key.add(设备Id);
			fixCache.remove(key);
			
		}*/
		
		
	}
	
	

	public int getClientTypeId() {
		return clientTypeId;
	}

	public void setClientTypeId(int clientTypeId) {
		this.clientTypeId = clientTypeId;
	}


	public int getSessionTime() {
		return sessionTime;
	}


	public void setSessionTime(int sessionTime) {
		this.sessionTime = sessionTime;
	}
	
	public Cache<List, Object> getSessionCodeMapFixCache() {
		return sessionCodeMapFixCache;
	}

	public void setSessionCodeMapFixCache(Cache<List, Object> sessionCodeMapFixCache) {
		this.sessionCodeMapFixCache = sessionCodeMapFixCache;
	}

	public Cache<Object, Object> getNotificationCache() {
		return notificationCache;
	}

	public void setNotificationCache(Cache<Object, Object> notificationCache) {
		this.notificationCache = notificationCache;
	}

	
}
