package com.chh.ap.cs.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chh.ap.cs.util.cache.redis.JedisFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chh.ap.cs.util.cache.Cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by Niow on 2016/10/28.
 */
public class SessionMonitor extends Thread {

    private static final Logger log = LoggerFactory.getLogger(SessionMonitor.class);

    public static Map<Integer,String> clientMapALarm;
    
    private Cache<List,Object> deviceSnExpireFixCache;

    static {
        clientMapALarm = new HashMap<Integer, String>();
        clientMapALarm.put(ClientType.CLIENT_TYPE_OBD_HTWX,"htwx_warning_msg");
    }

    private JedisFactory jedisFactory;

    private Cache<String,Object> alarmExporter;

    private boolean keepRunning = true;

    private int redisDbIndex = 0;

    @Override
    public void run() {
        while (keepRunning) {
            Jedis jedis = null;
            try {
                jedis = jedisFactory.getJedis();
                jedis.configSet("notify-keyspace-events", "Kx");
                jedis.select(redisDbIndex);
                log.info("SessionMonitor监控会话开始");
                jedis.psubscribe(new SessionStatusSub(), "__keyspace@" + redisDbIndex + "__:*");
            } catch (Exception e) {
                log.error("监控会话过期出错",e);
                try {
                    Thread.sleep(1000L);
                } catch (Exception ex) {}
            } finally {
                jedisFactory.returnBackJedis(jedis);
            }
        }
    }

    public JedisFactory getJedisFactory() {
        return jedisFactory;
    }

    public void setJedisFactory(JedisFactory jedisFactory) {
        this.jedisFactory = jedisFactory;
    }

    public boolean isKeepRunning() {
        return keepRunning;
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }

    public int getRedisDbIndex() {
        return redisDbIndex;
    }

    public void setRedisDbIndex(int redisDbIndex) {
        this.redisDbIndex = redisDbIndex;
    }

    public Cache<String, Object> getAlarmExporter() {
        return alarmExporter;
    }

    public void setAlarmExporter(Cache<String, Object> alarmExporter) {
        this.alarmExporter = alarmExporter;
    }

	public Cache<List, Object> getDeviceSnExpireFixCache() {
		return deviceSnExpireFixCache;
	}

	public void setDeviceSnExpireFixCache(Cache<List, Object> deviceSnExpireFixCache) {
		this.deviceSnExpireFixCache = deviceSnExpireFixCache;
	}


	private class SessionStatusSub extends JedisPubSub {
        @Override
        public void onPMessage(String pattern, String channel, String message) {
            //__keyspace@1__:deviceSnExpireCache:3213GD2016000988|3
        	String[] split = channel.split(":");
            String[] ds = split[2].split("\\|");
            String deviceUid = ds[0];
            Integer clientType =  Integer.parseInt(ds[1]);
            Map<String, Object> alarm = new HashMap<String, Object>();
            /*alarm.put("device_id", sn);
            alarm.put("utctime", new Date());
            alarm.put("alarm_type", "missing");*/
           //key(0)=设备类型名称 key(1)=设备sn
        	List<Object> key = new ArrayList<Object>();
        	key.add(clientType);
        	key.add(deviceUid);
            String warningTime = String.valueOf(deviceSnExpireFixCache.get(key));
            
            //alarm.put("device_uid", clientMapALarm.get(clientType)+deviceUid);
            alarm.put("device_uid", deviceUid);
            alarm.put("warning_type", 65);
            alarm.put("warning_value", warningTime);
            alarm.put("warning_desc", "失联报警");
            alarm.put("collection_time", new Date());
            
            alarmExporter.put(clientMapALarm.get(clientType),alarm);
            
        	deviceSnExpireFixCache.remove(key);
        }
    }
}
