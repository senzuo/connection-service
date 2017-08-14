package com.chh.ap.cs.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.chh.ap.cs.Runner;
import com.chh.ap.cs.util.DateTimeUtil;
import com.chh.ap.cs.util.cache.Cache;
import com.chh.ap.cs.util.cache.redis.RedisCache;
import com.chh.ap.cs.util.cache.redis.RedisHashCache;

public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    private static SessionManager instance = new SessionManager();
    /**
     * key session的hashCode
     * value Id
     */
    private static Map<IoSession, SessionInfo> sessionCodeMap;

    /**
     * key Id
     * value session对象
     */
    private static Map<String, IoSession> sessionMapById;
    
    /**
     * 
     */
    private Cache<List,Object> sessionCodeMapFixCache;

    /**
     * key Id
     * value session对象
     */
    private static Cache<String, Object> deviceSnExpireCache;
    
    
    private Cache<List,Object> deviceSnExpireFixCache;


    private SessionManager() {
        sessionCodeMap = new ConcurrentHashMap<IoSession, SessionInfo>();
        sessionMapById = new ConcurrentHashMap<String, IoSession>();
        deviceSnExpireCache = Runner.getBean("deviceSnExpireCache", RedisCache.class);
        sessionCodeMapFixCache = Runner.getBean("sessionCodeMapFixCache", RedisHashCache.class);
        deviceSnExpireFixCache = Runner.getBean("deviceSnExpireFixCache", RedisHashCache.class);
    }

    public static SessionManager getInstance() {
        return instance;
    }

//    public static Map<IoSession, SessionInfo> getSessionCodeMap() {
//        return sessionCodeMap;
//    }
//
//    public static void setSessionCodeMap(Cache<IoSession, SessionInfo> sessionCodeMap) {
//        SessionManager.sessionCodeMap = sessionCodeMap;
//    }

//    public static Map<String, IoSession> getSessionMapById() {
//        return sessionMapById;
//    }
//
//    public static void setSessionMapById(Cache<String, IoSession> sessionMapById) {
//        SessionManager.sessionMapById = sessionMapById;
//    }

    /**
     * 通过Id获取对应的IoSession
     *
     * @param Id
     * @return
     */
    public IoSession getSessionById(String Id) {
        return sessionMapById.get(Id);
    }

    public void refreshSession(SessionInfo sessInfo) {
    	//key(0)=设备类型名称 key(1)=设备sn
    	List key = new ArrayList<String>();
    	key.add(sessInfo.getClientType().getType());
    	key.add(sessInfo.getId());
    	deviceSnExpireFixCache.put(key, DateTimeUtil.toDateTimeString(new Date()));
    	
        deviceSnExpireCache.put(sessInfo.getId() + "|" + sessInfo.getClientType().getType(), sessInfo.getSn());
    }

    public SessionInfo getSessionInfo(IoSession session) {
        SessionInfo sessInfo = sessionCodeMap.get(session);
        return sessInfo;
    }

    /**
     * 添加client和服务端连接的IoSession
     *
     * @param session IoSession
     */
    public void putSession(SessionInfo sessInfo, IoSession session) {
        log.debug("sessionInfo:{},session:{}", sessInfo, session);
        //将之前会话踢下线
        IoSession oSess = sessionMapById.get(sessInfo.getId());
        if (oSess != null) {
            sessionCodeMap.remove(oSess);
            oSess.closeNow();
        }
        log.debug("sessionCodeMap:{},sessionMapById:{},deviceSnExpireCache:{}", sessionCodeMap, sessionMapById, deviceSnExpireCache);
        log.debug("sessInfo.id:{},sessInfo.sn:{},sessInfo.clientType:{}", sessInfo.getId(), sessInfo.getSn(), sessInfo.getClientType().getType());
        //保存新会话
        sessionCodeMap.put(session, sessInfo);
        sessionMapById.put(sessInfo.getId(), session);
        //2016-11-23
       /* list[0]=key(设备类型name) list[1]=设备Id;
        sessionCodeMapFixCache.put(key, value);*/
        List keyOne=new ArrayList();
        keyOne.add(ClientType.typeNameMap.get(sessInfo.getClientType().getType()));
        keyOne.add(sessInfo.getId());
        sessionCodeMapFixCache.put(keyOne, sessInfo.getSn());
        
        
        deviceSnExpireCache.put(sessInfo.getId() + "|" + sessInfo.getClientType().getType(), sessInfo.getSn());
        //key(0)=设备类型名称 key(1)=设备sn
    	List key = new ArrayList<String>();
    	key.add(sessInfo.getClientType().getType());
    	key.add(sessInfo.getId());
    	deviceSnExpireFixCache.put(key, System.currentTimeMillis());
        //计数+1
//		ClientType.increment(sessInfo.getClientType());
        sessInfo.clientType.increment();
    }

    /**
     * 删除client和服务器端连接的IoSession
     *
     * @param sess IoSession
     */
    public void delSession(IoSession sess) {
        SessionInfo sessInfo = sessionCodeMap.get(sess);
        if (sessInfo != null) {
            sessionCodeMap.remove(sess);
            if (sess == sessionMapById.get(sessInfo.getId())) {
                sessionMapById.remove(sessInfo.getId());
            }
          //2016-11-23
//            list[0]=key(设备类型name) list[1]=设备Id;
            List key=new ArrayList();
            key.add(ClientType.typeNameMap.get(sessInfo.getClientType().getType()));
            key.add(sessInfo.getId());
            sessionCodeMapFixCache.remove(key);
            
            
            
            //计数-1
//			ClientType.decrement(sessInfo.getClientType());
            sessInfo.clientType.decrement();


        }
    }

    /**
     * session信息
     */
    public static class SessionInfo {

        public static final int STATUS_ONLINE = 1;

        public static final int STATUS_LOST = 2;

        private String id;

        private String sn;
        //设备状态， 1:在线，2:失联
        private Integer status;
        
        private ClientType clientType;
        
        private byte dnaLastSeq = 1;

        public SessionInfo(String id) {
            this.id = id;
        }

        public SessionInfo(String id, String sn) {
            this.id = id;
            this.sn = sn;
        }


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public ClientType getClientType() {
            return clientType;
        }

        public void setClientType(ClientType clientType) {
            this.clientType = clientType;
        }

        public synchronized byte generateDnaSeq() {
            return ++dnaLastSeq;
        }

		public Integer getStatus() {
			return status;
		}

		public void setStatus(Integer status) {
			this.status = status;
		}
    }

}
