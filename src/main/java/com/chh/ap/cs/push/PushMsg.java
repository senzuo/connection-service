package com.chh.ap.cs.push;

import java.io.Serializable;
import java.sql.Timestamp;

public class PushMsg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 432306977644629634L;

	private Long id;
	
	private String deviceId;
	
	private int msgType;
	
	private byte[] msgData;
	
	private Timestamp createTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public byte[] getMsgData() {
		return msgData;
	}

	public void setMsgData(byte[] msgData) {
		this.msgData = msgData;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	/**
	 * 查询_CAN数据定时回传参数
	 */
	public static final int MSG_TYPE_QUERY_CAN_PARAM = 50;
	/**
	 * 设置_CAN数据定时回传参数
	 */
	public static final int MSG_TYPE_SET_CAN_PARAM = 81;
	
// 数据库表配置 msgType 无效 接头直接忽略
//	/**
//	 * 0x01	配置车系(数据流)	车型ID，4Bytes，Uint32，注意：小端模式的整数
//	 */
//	public static final int MSG_TYPE_SET_CAR_ID = 1;
//	
//	/**
//	 * 2	0x02	心跳周期	Uint8，单位为秒
//	 */
//	public static final int MSG_TYPE_SET_HEARTBEAT_CYCLE = 2;
//	
//	/**
//	 *3	0x03	是否已经激活	Uint8,0=未激活，1=已激活
//	 */
//	public static final int MSG_TYPE_SET_IS_ACTIVATION = 3;
//	
//	/**
//	 *4	0x04	位置信息采样周期	Uint8，单位为秒
//	 */
//	public static final int MSG_TYPE_SET_POSITION_CYCLE = 4;
//	
//	/**
//	 *5	0x05	OBD(总线)数据采样周期	Uint8，单位为秒
//	 */
//	public static final int MSG_TYPE_SET_DATA_CYCLE = 5;
//	
//	/**
//	 * 6	0x06	OBD(总线)数据传输周期	Uint8，单位为秒  
//	 */
//	public static final int MSG_TYPE_SET_PASSING_DATA_CYCLE = 6;
//	
//	/**
//	 * 7	0x07	打包数据传输周期	Uint8，单位为秒
//	 */
//	public static final int MSG_TYPE_SET_PACKAGE_DATA_CYCLE = 7;
//	
//	/**
//	 * 8	0x08	打包数据容量 capacity	Uint8，打包数据的最大字节数=打包数据*32，如：打包数据容量为32，则打包数据的最大字节数为1024Bytes，终端每次上传的打包数据不能超过1024Bytes
//	 */
//	public static final int MSG_TYPE_SET_PACKAGE_DATA_CAPACITY = 8;
//	
//	/**
//	 *9	0x09	打包数据是否需要应答 Uint8,0=不需要应答，1=需要应答
//	 */
//	public static final int MSG_TYPE_SET_PACKAGE_DATA_ANSWER = 9;
//	
//	/**
//	 *10	0x0a	GPS模块开关	Uint8，0=关，1=开
//	 */
//	public static final int MSG_TYPE_SET_GPS_SWITCH = 10;
//	
//	/**
//	 * 	11	0x0b	WIFI模块开关	Uint8，0=关，1=开
//	 */
//	public static final int MSG_TYPE_SET_WIFI_SWITCH = 11;
//	
//	/**
//	 *12	0x0c	蓝牙模块开关  	Uint8，0=关，1=开
//	 */ 
//	public static final int MSG_TYPE_SET_BLUETOOTH_SWITCH = 12;
//	
//	/**
//	 * 13	0x0d	OBD(总线)数据采集模块开关	Uint8，0=关，1=开
//	 */
//	public static final int MSG_TYPE_SET_DATA_SWITCH = 13;
//	
//	/**
//	 *14	0x0e	传感器开关	Uint8，0=关，1=开
//	 */
//	public static final int MSG_TYPE_SET_SENSOR_SWITCH = 14;
//	
//	/**
//	 * 15	0x0f	传感器唤醒阀值	Uint8，1-10个级别
//	 */
//	public static final int MSG_TYPE_SET_SENSOR_VALUE = 15;
//	
//	/**
//	 *18	0x12	日志上传指令	4+4+N （以下说明）
//	 */
//	public static final int MSG_TYPE_SET_LOG_UPLOAD = 18;
//	
//	/**
//	 *19	0x13	修改Wifi密码 	DPU String  
//	 */
//	public static final int MSG_TYPE_MODIFY_WIFI_PASSWORD = 19;
//	
//	/**
//	 *20	0x14	查询Wifi密码 	空
//	 */
//	public static final int MSG_TYPE_QUERY_WIFI_PASSWORD = 20;
//	
//	/**
//	 * 21	0x15	查询wifi名称	空
//	 */
//	public static final int MSG_TYPE_QUERY_WIFI_NAME = 21;
//	
//	/**
//	 *22	0x16	查询wifi 状态	空
//	 */
//	public static final int MSG_TYPE_SET_QUERY_WIFI_STATE = 22;
	
	
}
