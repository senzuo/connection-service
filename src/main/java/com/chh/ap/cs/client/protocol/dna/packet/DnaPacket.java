package com.chh.ap.cs.client.protocol.dna.packet;

import com.chh.ap.cs.util.ByteArrayUtil;
import org.apache.mina.core.session.IoSession;

import com.chh.ap.cs.client.SessionManager;
import com.chh.ap.cs.client.SessionManager.SessionInfo;
import com.chh.ap.cs.client.protocol.base.BasePacket;

public class DnaPacket extends BasePacket {
	
    /**
     * 迪娜OBD盒子帧长度，不包括可变的数据长度
     */
    public static final int CLIENT_FRAME_LENGTH=14;
    
	/**
	 * 数据长度字段位置
	 */
	public static final int INDEX_OF_DATA_LEN = 10;
	
	public static final int INDEX_OF_DEVICE_ID = 4;
	
	private byte seq;
	
	private byte cmdId;
	
	private String deviceSn;
	
	private byte[] data;
	
	public DnaPacket(IoSession ioSession,byte cmdId){
		SessionInfo sess = SessionManager.getInstance().getSessionInfo(ioSession);
		//1.生成包序列号
		seq = sess.generateDnaSeq();
		this.cmdId = cmdId;
		this.deviceSn = sess.getSn();
		
	}

	public byte getSeq() {
		return seq;
	}

	public void setSeq(byte seq) {
		this.seq = seq;
	}

	public byte getCmdId() {
		return cmdId;
	}

	public void setCmdId(byte cmdId) {
		this.cmdId = cmdId;
	}

	public String getDeviceSn() {
		return deviceSn;
	}

	public void setDeviceSn(String deviceSn) {
		this.deviceSn = deviceSn;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	@Override
	public byte[] toBytes(){
		int dataLen = data==null?0:data.length;
		int packetLen = dataLen+CLIENT_FRAME_LENGTH;
		
		byte[] packet = new byte[packetLen];
		int index = 0;
		packet[index++] = (byte) 0xFA;
		packet[index++] = (byte) 0xFA;
		packet[index++] = seq;
		packet[index++] = cmdId;
		ByteArrayUtil.setULONG6(packet, 4, Long.parseLong(deviceSn));
		index += 6;
		ByteArrayUtil.setUSHORT2(packet, 10, dataLen);
		index += 2;
		if(dataLen!=0){
			System.arraycopy(data, 0, packet, index, dataLen);
			index += dataLen;
		}
		packet[index++] = generateXOR(packet, 2, packetLen-4);
		packet[index] = (byte) 0xFB;
		return packet;
	}
	
	/**
	 * 生成校验位
	 * @param res
	 * @param start
	 * @param length
	 * @return
	 */
	public static byte generateXOR(byte[] res,int start,int length){
		byte r = res[start];
		for (int i=start+1;i<start+length;i++){
			r = (byte) (r^res[i]);
		}
		return r;
	}
}
