package com.chh.ap.cs.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chh.ap.cs.util.cache.Cache;
import com.chh.ap.cs.util.cache.redis.RedisHashCache;

public class MissWarningRemedyTask implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(MissWarningRemedyTask.class);
	
	private int clientTypeId;

	private static Cache<String, Object> deviceSnExpireCache;

	private Cache<List, Object> deviceSnExpireFixCache;

	public static Map<Integer, String> clientMapALarm;

	private Cache<String, Object> alarmExporter;

	static {
		clientMapALarm = new HashMap<Integer, String>();
		clientMapALarm.put(ClientType.CLIENT_TYPE_OBD_HTWX, "htwx_warning_msg");
	}

	@Override
	public void run() {
		try {
			log.info("失联告警丢失处理线程会话开始");
			// 比较
			// 从deviceSnExpireFixCache中取出所有的fields
			// deviceSnExpireFixCache 判断类型是不是 RedisHashCache，不是的话写错误日志
			RedisHashCache<List, Object> fixCache = (RedisHashCache<List, Object>) deviceSnExpireFixCache;
			//String clientKey = ClientType.typeNameMap.get(clientTypeId); // ClientType.getClientTypeName();
			Map<String, String> fields = fixCache.getAllFields(clientTypeId);
			String deviceUid = null;
			String warningTime = null;
			for (Map.Entry<String, String> entry : fields.entrySet()) {
				deviceUid = String.valueOf(entry.getKey());
				warningTime = String.valueOf(entry.getValue());
				// key(0)=设备类型名称 key(1)=设备sn
				String ExpireValue = (String) deviceSnExpireCache.get(deviceUid + "|"
						+ clientTypeId);
//				System.out.println("----------------ExpireValue---------------"+ExpireValue);
				if (ExpireValue == null) {
					//输出报警
					Map<String, Object> alarm = new HashMap<String, Object>();
					/*alarm.put("device_id", sn);
					alarm.put("utctime", new Date());
					alarm.put("alarm_type", "missing");*/
					//alarm.put("device_uid", clientMapALarm.get(clientTypeId)+deviceUid);
					alarm.put("device_uid", deviceUid);
		            alarm.put("warning_type", 65);
		            alarm.put("warning_value", warningTime);
		            alarm.put("warning_desc", "失联报警");
		            alarm.put("collection_time", new Date());
					
					
					alarmExporter.put(clientMapALarm.get(clientTypeId), alarm);

					// key(0)=设备类型名称 key(1)=设备sn
					List key = new ArrayList<String>();
					key.add(clientTypeId);
					key.add(deviceUid);
					deviceSnExpireFixCache.remove(key);
				}
			}
		   } catch (Exception e) {
			log.error("线程失联告警丢失处理异常", e);
		}
	}

	public int getClientTypeId() {
		return clientTypeId;
	}

	public void setClientTypeId(int clientTypeId) {
		this.clientTypeId = clientTypeId;
	}

	public static Cache<String, Object> getDeviceSnExpireCache() {
		return deviceSnExpireCache;
	}

	public static void setDeviceSnExpireCache(
			Cache<String, Object> deviceSnExpireCache) {
		MissWarningRemedyTask.deviceSnExpireCache = deviceSnExpireCache;
	}

	public Cache<List, Object> getDeviceSnExpireFixCache() {
		return deviceSnExpireFixCache;
	}

	public void setDeviceSnExpireFixCache(
			Cache<List, Object> deviceSnExpireFixCache) {
		this.deviceSnExpireFixCache = deviceSnExpireFixCache;
	}

	public static Map<Integer, String> getClientMapALarm() {
		return clientMapALarm;
	}

	public static void setClientMapALarm(Map<Integer, String> clientMapALarm) {
		MissWarningRemedyTask.clientMapALarm = clientMapALarm;
	}

	public Cache<String, Object> getAlarmExporter() {
		return alarmExporter;
	}

	public void setAlarmExporter(Cache<String, Object> alarmExporter) {
		this.alarmExporter = alarmExporter;
	}

}
