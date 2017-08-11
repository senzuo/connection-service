package com.chh.ap.cs.client.protocol.htwx.packet;

import com.chh.ap.cs.client.SessionManager;
import com.chh.ap.cs.client.protocol.base.BasePacket;
import com.chh.ap.cs.util.ByteArrayUtil;
import org.apache.mina.core.session.IoSession;

public class HtwxPacket extends BasePacket {

	/**
	 * 数据长度字段位置
	 * 协议长度，=(1)+(2)+(3)+(4)+(5)+(6)+(7)+(8)的字节总长度
	 */
	public static final int INDEX_OF_DATA_LEN = 2;
	/**
	 * obd_id 起始位置
	 */
	public static final int INDEX_OF_OBD_ID = 5;
	/**
	 * 信息类型, 高字节表示主标识，低字节表示子标识
主标识在0x00~0x7F的属
NEW OBDII SMART 通讯协议
CB212-C1005 Rev. 4.24
12
于上行协议
主标识在0x80~0xFF的属于下行协议
不同的信息类型,(6)的数据不一样
该字段传输使用大端字节序
	 */
	public static final int INDEX_OF_PROTOCOL_TYPE = 25;
	
	/**
	 * 航天无线OBD盒子帧长度，不包括可变的数据长度
	 */
    public static final int CLIENT_FRAME_LENGTH=31;
    
    
    
    /**
     * 设备编号：（OBDII产品序列号）
     * 长度20Byte
     * 每个终端的唯一标识
     */
    private String obdId;
    
    /**
     * 信息类型
     * 信息类型, 高字节表示主标识，低字节表示子标识
主标识在0x00~0x7F的属
于上行协议
主标识在0x80~0xFF的属于下行协议
不同的信息类型,(6)的数据不一样
该字段传输使用大端字节序
     */
    private int protocolType;
    /**
     * 协议内容
     */
    private byte[] content;
    
    
	public HtwxPacket(IoSession ioSession,int cmdId){
		SessionManager.SessionInfo sess = SessionManager.getInstance().getSessionInfo(ioSession);
		this.obdId = sess.getSn();
		this.protocolType = cmdId;
	}
    
    
	public String getObdId() {
		return obdId;
	}




	public void setObdId(String obdId) {
		this.obdId = obdId;
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




	@Override
	public byte[] toBytes() {

		int dataLen = content==null?0:content.length;
		int packetLen = dataLen+CLIENT_FRAME_LENGTH;
		
		byte[] packet = new byte[packetLen];
		int index = 0;
		packet[index++] = (byte) 0x40;
		packet[index++] = (byte) 0x40;
//		packetLen
		ByteArrayUtil.setLeUSHORT2(packet, index, packetLen);
		index +=2;
//		protocol_version
		packet[index++] = 0x03;
//		obd_id
		byte[] obdIdBytes = obdId.getBytes();
		System.arraycopy(obdIdBytes, 0, packet, index, obdIdBytes.length);
		index +=20;
//		protocol_type   大头模式
		ByteArrayUtil.setUSHORT2(packet, index, protocolType);
		index += 2;
//		content
		if(dataLen!=0){
			System.arraycopy(content, 0, packet, index, dataLen);
			index += dataLen;
		}
//		crc
		int crc = generateXOR(packet, 0, packetLen-4);
		ByteArrayUtil.setLeUSHORT2(packet, index, crc);
		
//		protocol_tail
		packet[index++] = (byte) 0x0D;
		packet[index] = (byte) 0x0A;
		return packet;
	
	}

	/**
	 * 生成校验位
	 * @param res
	 * @param start
	 * @param length
	 * @return
	 */
	public static int generateXOR(byte[] res,int start,int length){
//		TODO
//		
		return 0;
	}
	
}
