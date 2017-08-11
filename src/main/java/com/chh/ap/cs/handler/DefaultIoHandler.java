package com.chh.ap.cs.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.chh.ap.cs.client.ClientType;
import com.chh.ap.cs.client.SessionManager;
import com.chh.ap.cs.client.SessionMonitor;
import com.chh.ap.cs.util.Constant;
import com.chh.ap.cs.util.cache.Cache;
import com.chh.ap.cs.util.cache.redis.RedisStackCache;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chh.ap.cs.util.DateTimeUtil;

/**
 * 继承mina的handler，在数据进过filter chain过滤后，最后会传递到此来进行实际数据处理<br/>
 * <p>
 * Created by Niow on 2016/6/22.
 */
public class DefaultIoHandler implements IoHandler {


    public static final Logger log = LoggerFactory.getLogger(DefaultIoHandler.class);

    public static final Logger log4Data = LoggerFactory.getLogger("log4data");
    
//    /**
//     * Map<ClientType,Parser>
//     */
//    private Map<Integer, Parser> parserMap;


//    /**
//     * Map<ClientType,LoginHandler>
//     */
//    private Map<Integer, LoginHandler> loginHandlerMap;
//
//    /**
//     * Map<ClientType,Responsor>
//     */
//    private Map<Integer, Responsor> responsorMap;

//    private ClientRecognizer clientRecognizer;

    private Cache<String, byte[]> upDataCache;

    private Cache<String, Map<String, Object>> notificationCache;

//    /**
//     * 不同clientType的session最大空闲时长配置,单位：ms
//     */
//    private Map<Integer, Integer>  sessionIdleMap;

    public DefaultIoHandler() {
    }


    public void sessionCreated(IoSession ioSession) throws Exception {
        log.info("有设备接入,IP[{}],sessionCode[{}]", ioSession.getRemoteAddress().toString(), ioSession.hashCode());
    }

    public void sessionOpened(IoSession ioSession) throws Exception {

    }

    public void sessionClosed(IoSession ioSession) throws Exception {
        //获取不到RemoteAddress，可创建时放到ioSession获取
//    	log.info("有设备断开,IP[{}],sessionCode[{}]",ioSession.getRemoteAddress().toString(),ioSession.hashCode());
        log.info("有设备断开,sessionCode[{}]", ioSession.hashCode());
        SessionManager.getInstance().delSession(ioSession);
    }

    public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) throws Exception {
        //设备连接超时，服务器未收到数据
        //断开连接
        log.info("设备连接超时，将被断开连接,IP[{}],sessionCode[{}]", ioSession.getRemoteAddress().toString(), ioSession.hashCode());
        //发送连接超时通知给解析
        try {
            SessionManager.SessionInfo sessInfo = SessionManager.getInstance().getSessionInfo(ioSession);
            if (sessInfo != null&&sessInfo.getClientType()!=null) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("device_uid", sessInfo.getId());
				map.put("warning_type", 17);
				map.put("warning_value", System.currentTimeMillis());
				map.put("warning_desc", "五分钟断链告警(内部告警,请无视)");
				map.put("collection_time", new Date());
                ClientType clientType = sessInfo.getClientType();
                String clientTypeName = clientType.getClientTypeName();
                notificationCache.put(SessionMonitor.clientMapALarm.get(clientType.getType()), map);
            }else {
                log.warn("会话群中无法找到此会话IP[{}],sessionCode[{}]", ioSession.getRemoteAddress().toString(), ioSession.hashCode());
            }
        } catch (Exception e){
        	log.warn("sessionIdle抛异常了。。。",e);
        }
        ioSession.closeNow();
    }

    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
    	//当客户端主动断开的时候 “远程主机强迫关闭了一个现有的连接。” 这里  ioSession.getRemoteAddress() 会报空指针异常
        log.error("会话异常，IP[" + ioSession.getRemoteAddress().toString() + "],sessionCode[" + ioSession.hashCode() + "]", throwable);
    }

    /**
     * @param ioSession
     * @param message
     * @throws Exception
     */
    public void messageReceived(IoSession ioSession, Object message) throws Exception {
        ClientType clientType = (ClientType) ioSession.getAttribute(Constant.SESSION_CLIENT_TYPE);
        //1.判断是否登录，做登录验证
        SessionManager.SessionInfo sessInfo = SessionManager.getInstance().getSessionInfo(ioSession);
        if (sessInfo == null) {
            sessInfo = clientType.getProtocolHandler().login(ioSession, (byte[]) message);
            if (sessInfo == null) {
                log.warn("设备登录失败,设备类型[{}],IP[{}],消息内容[{}]", clientType.getClientTypeName(), ioSession.getRemoteAddress().toString(), message);
                ioSession.closeNow();
                return;
            } else {
                //登录成功
                sessInfo.setClientType(clientType);
                SessionManager.getInstance().putSession(sessInfo, ioSession);
                //当前状态为失联状态，推送取消失联告警
                if(sessInfo.getStatus() == 2){
                	Map<String, Object> alarm = new HashMap<String, Object>();
                	Date now = new Date();
					alarm.put("device_uid", sessInfo.getId());
		            alarm.put("warning_type", 18);
		            alarm.put("warning_value", DateTimeUtil.toDateTimeString(now));
		            alarm.put("warning_desc", "取消失联告警");
		            alarm.put("collection_time", now);
		            notificationCache.put(SessionMonitor.clientMapALarm.get(clientType.getType()),alarm);
                }
                log.info("设备登录成功,设备ID[{}],设备类型[{}],IP[{}]", sessInfo.getId(), clientType.getClientTypeName(), ioSession.getRemoteAddress().toString());
            }
        }

        //刷新未接收到消息的
        SessionManager.getInstance().refreshSession(sessInfo);

        log4Data.info("id:[{}],type:[{}],data:[{}]", sessInfo.getId(), sessInfo.getClientType().getClientTypeName(), message);
        //2.解析数据包
        //响应客户端,只有上行消息才响应
        clientType.getProtocolHandler().messageHandler(ioSession, (byte[]) message);


        //TODO 3.放入缓存
//        String key = sessInfo.getSn()+'|'+sessInfo.getClientType().getType()+"|"+System.currentTimeMillis();
//        upDataCache.put(key, (byte[]) message);

        if (upDataCache instanceof RedisStackCache) {
            upDataCache.put(clientType.getClientTypeName(), (byte[]) message);
        } else {
            String key = sessInfo.getId() + '|' + sessInfo.getClientType().getClientTypeName() + "|" + System.currentTimeMillis();
            upDataCache.put(key, (byte[]) message);
        }

//        Parser parser = parserMap.get(clientType);
//        Message msg = parser.parse(ioSession, message);
//        if (msg.getType() == Message.TYPE_HEART) {
//            Responsor responsor = responsorMap.get(clientType);
//            responsor.reHeartbeat(ioSession, msg.getMessage());
//            return;
//        }
//        if (msg.getType() == Message.TYPE_LOGIN) {
//            LoginHandler loginHandler = loginHandlerMap.get(clientType);
//            boolean loginResult = loginHandler.login(ioSession, msg.getMessage());
//            if (loginResult == false) {
//                log.warn("设备登录失败,设备类型[{}],IP[{}],消息内容[{}]", clientType, ioSession.getRemoteAddress().toString(), message);
//                ioSession.closeNow();
//            } else {
//            	//登录成功
//            	Long id = 1L;
//            	SessionInfo sess = new SessionInfo(id);
////            	sess.setId(id);
////            	sess.setSn(sn);
//            	sess.setClientType(clientType);
//            	SessionManager.getInstance().addSession(sess, ioSession);
//            }
//            return;
//        }
//        if (msg.getType() == Message.TYPE_DATA) {
//            //TODO 接收到的数据放入缓存
//            log.debug("接收到来自客户端数据:"+msg.getMessage());
//        }
//        if (msg.getType() == Message.TYPE_UNKNOW) {
//            //TODO 未知的输入写入异常数据日志
//            log.debug("未知数据:"+msg.getMessage());
//        }
    }

    public void messageSent(IoSession ioSession, Object message) throws Exception {

    }

    public void inputClosed(IoSession ioSession) throws Exception {

    }

    //    public Map<Integer, LoginHandler> getLoginHandlerMap() {
//        return loginHandlerMap;
//    }
//
//    public void setLoginHandlerMap(Map<Integer, LoginHandler> loginHandlerMap) {
//        this.loginHandlerMap = loginHandlerMap;
//    }
    public Cache<String, byte[]> getUpDataCache() {
        return upDataCache;
    }


    public void setUpDataCache(Cache<String, byte[]> upDataCache) {
        this.upDataCache = upDataCache;
    }


    public Cache<String, Map<String, Object>> getNotificationCache() {
        return notificationCache;
    }


    public void setNotificationCache(
            Cache<String, Map<String, Object>> notificationCache) {
        this.notificationCache = notificationCache;
    }

//	public Map<Integer, Parser> getParserMap() {
//        return parserMap;
//    }
//
//    public void setParserMap(Map<Integer, Parser> parserMap) {
//        this.parserMap = parserMap;
//    }

//    public ClientRecognizer getClientRecognizer() {
//        return clientRecognizer;
//    }
//
//    @Autowired
//    public void setClientRecognizer(ClientRecognizer clientRecognizer) {
//        this.clientRecognizer = clientRecognizer;
//    }

//    public Map<Integer, Responsor> getResponsorMap() {
//        return responsorMap;
//    }
//
//    public void setResponsorMap(Map<Integer, Responsor> responsorMap) {
//        this.responsorMap = responsorMap;
//    }


//	public Map<Integer, Integer> getSessionIdleMap() {
//		return sessionIdleMap;
//	}
//
//
//	public void setSessionIdleMap(Map<Integer, Integer> sessionIdleMap) {
//		this.sessionIdleMap = sessionIdleMap;
//	}

}
