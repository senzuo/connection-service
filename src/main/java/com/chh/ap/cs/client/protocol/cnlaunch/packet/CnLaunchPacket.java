package com.chh.ap.cs.client.protocol.cnlaunch.packet;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chh.ap.cs.client.SessionManager;
import com.chh.ap.cs.client.SessionManager.SessionInfo;
import com.chh.ap.cs.client.protocol.base.BasePacket;
import com.chh.ap.cs.util.ByteArrayUtil;
import com.chh.ap.cs.util.CRC16;

/**
 * 数据报文格式定义 参考文档 《元征OBD网络通信协议》
 * 
 * 不带序列号帧头格式
 *  * 数据报文格式：帧标识（5字节）+帧长（2byte）+厂商标识（1字节）+消息标识（4字节）+扩展字段（4字节）+业务ID（2byte）
 * +协议号（1byte）+数据（n byte）+时间戳（4byte）+校验码（2byte） +帧尾巴（4字节）网络传输采用大端格式
 * 
 * 带序列号 帧头协议格式
 * 数据报文格式：帧标识（18字节）+帧长（2byte）+厂商标识（1字节）+消息标识（4字节）+扩展字段（4字节）+业务ID（2byte）
 * +协议号（1byte）+数据（n byte）+时间戳（4byte）+校验码（2byte）网络传输采用大端格式
 **/
public class CnLaunchPacket extends BasePacket {
	public static final Logger log = LoggerFactory.getLogger(CnLaunchPacket.class);

	protected byte[] frameIdentifier = { (byte) (0xFF), (byte) (0x49),(byte) (0x43), (byte) (0x41), (byte) (0x52) };// 帧标识
	protected int frameLength; // 帧长 不包括帧标识
	protected byte manufactureID = (byte) 0x01; // 厂商标识
	public int messageID = 0;// 消息标识
	protected int extensionField = 0;// 扩展字段
	public int protocolType;// 业务ID
	protected byte version = 0x01; // 协议号
	protected byte[] content; // 业务数据
	public int time;// 时间戳
	protected int crc; // 校验码
	public final static byte[] frameITail = { (byte) (0xAA), (byte) (0xBB),(byte) (0x5C), (byte) (0x6E) };// 帧尾

	/**
	 * 不带序列号帧头格式 true / 带序列号 帧头协议格式 false 【沿用 元征 同方协议 带序列号前缀协议文档】
	 */
	public static boolean PROTOCOL_TYPE = false;  //ClientRecognizerImpl  判断器中也需要修改判断类型
	
	/**
	 * 数据帧长 起始位置 （等同于 帧标识长度）
	 */
	public static final int INDEX_OF_DATA_LEN = PROTOCOL_TYPE ? 5 : 18;

	/**
	 * [协议类型]业务id 起始位置 (等同于 加密位置起始)
	 */
	public static final int INDEX_OF_PROTOCOL_TYPE = PROTOCOL_TYPE ? 16 : 29;

	/**
	 * 消息标识 起始从第 8位开始
	 */
	public static final int INDEX_OF_MESSAGE_IDENTIFIER = PROTOCOL_TYPE ? 8 : 21;

	/**
	 * 帧长到 加密区域 中间部分长度 [一样] （ 厂商标识+消息标识+扩展字段）
	 */
	public static final int Middle_LENGTH = 9;

	/**
	 * 帧头帧尾长度
	 */

	public static final int FRAME_TAIL_AND_HEAD_LENGTH = PROTOCOL_TYPE ? 9 : 18;//（：18 只有帧头18）

	/**
	 * 帧尾长度
	 */
	public static final int FRAME_TAIL_LENGTH = 4;

	/**
	 * 内容（加密数据）以外长度
	 */
	public static final int FRAME_OTHER_LENGTH = PROTOCOL_TYPE ? 26 : 35;

	/**
	 * 加密数据之后长度 （ 尾部 时间戳+校验码 +帧尾巴）
	 */

	public static final int TAIL_LENGTH = PROTOCOL_TYPE ? 10 : 6;

	/**
	 * 设备编号：（OBDII产品序列号） 长度24Byte 每个终端的唯一标识
	 */
	private String obdSn;

	public CnLaunchPacket(IoSession ioSession, int cmdId) {
		SessionInfo sess = SessionManager.getInstance().getSessionInfo(
				ioSession);
		this.obdSn = sess.getSn();
		this.protocolType = cmdId;
		//配置下发命令 MessageID 增加1
		if(cmdId==4102){
			setMessageID(getMessageID()+1);
		}
	}
	
	public String getObdSn() {
		return obdSn;
	}

	public void setObdSn(String obdId) {
		this.obdSn = obdId;
	}

	public int getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(int protocolType) {
		this.protocolType = protocolType;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public int getMessageID() {
		return messageID;
	}

	public void setMessageID(int messageID) {
		this.messageID = messageID;
	}

	@Override
	public byte[] toBytes() {

		int dataLen = content == null ? 0 : content.length;
		int packetLen = dataLen + FRAME_OTHER_LENGTH ; 

		byte[] packet = new byte[packetLen];
		int index = 0;
		if(PROTOCOL_TYPE){
		System.arraycopy(frameIdentifier, 0, packet, index, INDEX_OF_DATA_LEN);// 无序列号帧标识
		}else{
			byte[] tempframeIdentifier = new byte[INDEX_OF_DATA_LEN];
			byte[] OEMID = { (byte) (0x02) };// 厂商id
			System.arraycopy(OEMID, 0, tempframeIdentifier, 0, 1);
			System.arraycopy(obdSn.getBytes(), 0, tempframeIdentifier, 1, 12);
			System.arraycopy(frameIdentifier, 0, tempframeIdentifier, 13, 5);
			System.arraycopy(tempframeIdentifier, 0, packet, index, INDEX_OF_DATA_LEN);//带序列号 帧标识	
		}
		index += INDEX_OF_DATA_LEN;
		frameLength=packetLen-FRAME_TAIL_AND_HEAD_LENGTH; //总包长度减去 帧头帧尾
		ByteArrayUtil.setUSHORT2(packet, index, frameLength); // 帧长
		index += 2;

		packet[index++] = manufactureID; // 厂商标识 目前固定为 0x01

		ByteArrayUtil.setUINT4(packet, index, messageID); // 消息标识
		index += 4;

		ByteArrayUtil.setUINT4(packet, index, extensionField); // 扩展字段
		index += 4;

		if(content!=null){//部分响应数据 内容可以为空
		System.arraycopy(content, 0, packet, index, content.length); // 业务数据
		index += content.length;
		}
		
		time = (int)(System.currentTimeMillis() / 1000);

		ByteArrayUtil.setUINT4(packet, index, time); // 时间戳
		index += 4;

		try {
			byte[] crcData = new byte[packetLen - 6];// 指针移动到时间戳之后
			System.arraycopy(packet, 0, crcData, 0, packetLen - 6);
			crc = CRC16.getInstance().getCrc(crcData); // 获取校验码
			ByteArrayUtil.setUSHORT2(packet, index, crc); // 帧长
			index += 2;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("获取验证码错误", e);
		}
		
		//带序列号帧头 格式无帧尾
        if(PROTOCOL_TYPE){
		System.arraycopy(frameITail, 0, packet, index, FRAME_TAIL_LENGTH);// 帧尾
        }

		return packet;

	}
}
