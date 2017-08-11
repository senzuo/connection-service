package com.chh.ap.cs.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.chh.ap.cs.client.protocol.base.ProtocolHandler;

/**
 * client类别
 *
 */
public class ClientType {
	
	
	
    /**
     * 未知
     */
    public static final int CLIENT_TYPE_UNKNOWN = 0;

    /**
     * OBD盒子：元征
     */
    public static final int CLIENT_TYPE_OBD_YUANZHEN = 1;
	
    /**
     * OBD盒子:迪娜
     */
    public static final int CLIENT_TYPE_OBD_DNA = 2;
    
    /**
     * OBD盒子：航天无线
     */
    public static final int CLIENT_TYPE_OBD_HTWX = 3;
    
    /**
     *  最大协议头长度
     *  TODO 动态判断
     */
    public static final int MAX_HEAD_LEN = 5;

    /**
     * 计数统计
     */
	public static Map<Integer,AtomicInteger> typeCountMap = new ConcurrentHashMap<Integer,AtomicInteger>();
	/**
	 * 类型描述
	 */
	public static Map<Integer,String> typeNameMap = new HashMap<Integer,String>();
	
	
	static {
		//init type 
		//TODO spring注入
		typeCountMap.put(ClientType.CLIENT_TYPE_UNKNOWN, new AtomicInteger(0));
		typeCountMap.put(ClientType.CLIENT_TYPE_OBD_YUANZHEN, new AtomicInteger(0));
		typeCountMap.put(ClientType.CLIENT_TYPE_OBD_DNA, new AtomicInteger(0));
		typeCountMap.put(ClientType.CLIENT_TYPE_OBD_HTWX, new AtomicInteger(0));
		
		typeNameMap.put(ClientType.CLIENT_TYPE_OBD_YUANZHEN, "YZ_OBD");
		typeNameMap.put(ClientType.CLIENT_TYPE_OBD_DNA, "DNA_OBD");
		typeNameMap.put(ClientType.CLIENT_TYPE_OBD_HTWX, "HTWX_OBD");
		typeNameMap.put(ClientType.CLIENT_TYPE_UNKNOWN, "UNKNOWN");
	}
	
	/**
	 * 计数器+1
	 * 线程安全
	 */
	public void increment(){
		typeCountMap.get(type).incrementAndGet();
	}
	
	/**
	 * 计数器-1
	 * 线程安全
	 */
	public void decrement(){
		typeCountMap.get(type).decrementAndGet();
	}
	
	/**
	 * 获取指定类别client的在线数
	 * @param type
	 * @return
	 */
	public static int getOnlineSize(int type){
		if(typeCountMap.get(type)!=null){
			return typeCountMap.get(type).get();
		}
		return 0;
	}
	
	
	/**
	 * 类型ID
	 */
	private int type;
	/**
	 * session最大空闲时长配置,单位：s
	 */
	private int sessionIdle;
	/**
	 * 协议处理器
	 */
	private ProtocolHandler protocolHandler;
	
	public ClientType() {
		
	}
	
	public ClientType(int type, int sessionIdle, ProtocolHandler protocolHandler) {
		this.type = type;
		this.sessionIdle = sessionIdle;
		this.protocolHandler = protocolHandler;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSessionIdle() {
		return sessionIdle;
	}

	public void setSessionIdle(int sessionIdle) {
		this.sessionIdle = sessionIdle;
	}

	public ProtocolHandler getProtocolHandler() {
		return protocolHandler;
	}

	public void setProtocolHandler(ProtocolHandler protocolHandler) {
		this.protocolHandler = protocolHandler;
	}

	public String getClientTypeName(){
		return typeNameMap.get(type);
	}
}
