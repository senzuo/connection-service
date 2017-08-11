package com.chh.ap.cs.client.protocol.htwx;

import java.util.HashMap;
import java.util.Map;

import com.chh.ap.cs.client.ClientType;
import com.chh.ap.cs.util.ByteArrayUtil;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chh.ap.cs.client.SessionManager.SessionInfo;
import com.chh.ap.cs.client.protocol.base.ProtocolHandler;
import com.chh.ap.cs.client.protocol.htwx.packet.HtwxPacket;
import com.chh.ap.cs.dao.impl.DeviceDao;
import com.chh.ap.cs.push.PushMsg;

public class HtwxOBD2ProtocolHandler implements ProtocolHandler {

	public static final Logger log = LoggerFactory.getLogger(HtwxOBD2ProtocolHandler.class);    
    
	public static Map<Integer,Integer> reqAndRespMap = new HashMap<Integer,Integer>();
	
	private static final int PROTOCOL_TYPE_LOGIN_RESP = 0x9001;
	private static final int PROTOCOL_TYPE_HEARTBEAT_RESP = 0x9003;
	private static final int PROTOCOL_TYPE_TEXT_MSG_RESP = 0xB006;
	private static final int PROTOCOL_TYPE_ALARM_RESP = 0xC007;
	private static final int PROTOCOL_TYPE_RFID_RESP = 0xC00C;
	
	static{
		reqAndRespMap.put(0x1001, PROTOCOL_TYPE_LOGIN_RESP);//登录
		reqAndRespMap.put(0x1002, null);//注销
		reqAndRespMap.put(0x1003, PROTOCOL_TYPE_HEARTBEAT_RESP);//心跳
//		reqAndRespMap.put(0x2001, 0xA001);//设置//下行
//		reqAndRespMap.put(0x2002, 0xA002);//查询//下行
//		reqAndRespMap.put(0x3001, 0xB001);//车辆点名
		//..
		reqAndRespMap.put(0x3006, PROTOCOL_TYPE_TEXT_MSG_RESP);//文字信息
		
		reqAndRespMap.put(0x4001,null);//GPS数据                                        
		reqAndRespMap.put(0x4002,null);//工况数据                                       
		reqAndRespMap.put(0x4003,null);//GSensor数据                                    
		reqAndRespMap.put(0x4004,null);//支持的数据流                                   
		reqAndRespMap.put(0x4005,null);//快照数据                                       
		reqAndRespMap.put(0x4006,null);//乘用车故障信息                                 
		reqAndRespMap.put(0x400B,null);//商用车故障信息                                 
		reqAndRespMap.put(0x4007,PROTOCOL_TYPE_ALARM_RESP);//警情变化下行警情接收确认                 
		reqAndRespMap.put(0x4008,null);//基站定位上传                                   
		reqAndRespMap.put(0x4009,null);//睡眠状态下的GPS数据                            
		reqAndRespMap.put(0x400C,PROTOCOL_TYPE_RFID_RESP);//RFID卡号上传下行 RFID卡号上传确认        
		
		reqAndRespMap.put(0x5101, null);//AGPS数据请求
	}
	
	private DeviceDao deviceDao = new DeviceDao();
	
	@Override
	public MessageDecoderResult decodable(byte[] ba) {
		//协议长度，=(1)+(2)+(3)+(4)+(5)+(6)+(7)+(8)的字节总长度
		short dataLen = ByteArrayUtil.getLeUSHORT2(ba, HtwxPacket.INDEX_OF_DATA_LEN);
		if(ba.length>=dataLen) {
			return MessageDecoderResult.OK;
		} 
		return MessageDecoderResult.NEED_DATA;
	}

	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		byte[] msgPacket = null;
		//数据帧结构体
		byte[] msgFrame = new byte[HtwxPacket.CLIENT_FRAME_LENGTH];
		in.get(msgFrame);
		//业务数据包
		short dataLen = ByteArrayUtil.getLeUSHORT2(msgFrame, HtwxPacket.INDEX_OF_DATA_LEN);
		int packetLen = dataLen-HtwxPacket.CLIENT_FRAME_LENGTH;
		if(packetLen>0) {
			//完整的数据包
			msgPacket = new byte[dataLen];
			
			System.arraycopy(msgFrame, 0, msgPacket, 0, msgFrame.length);
			in.get(msgPacket, msgFrame.length, packetLen);
		} else {
			msgPacket = msgFrame;
		}
		
		out.write(msgPacket);
		return MessageDecoderResult.OK;
	}

	@Override
	public SessionInfo login(IoSession ioSession, byte[] message) {
		String deviceId = ByteArrayUtil.byte2str(message, HtwxPacket.INDEX_OF_OBD_ID, 20);
		
		SessionInfo sess = null;
		try {
			sess = deviceDao.login(deviceId, ClientType.CLIENT_TYPE_OBD_HTWX);
			if(sess==null) {
				log.info("设备[ID:{}]未入库，登录失败。",deviceId);
			}
		} catch (Exception e) {
			log.error("设备[ID:{}]登录失败，发生异常。",deviceId, e);
		}
		return sess;
	}

	@Override
	public void messageHandler(IoSession ioSess, byte[] message) {
//		byte cmdId = message[3];
//		大头模式
		int cmdId = ByteArrayUtil.getUSHORT2(message, 25);
		
		//设备主动上行消息，才响应
		Integer respCmd = reqAndRespMap.get(cmdId);
		if(respCmd!=null) {
			//响应消息
			HtwxPacket p = new HtwxPacket(ioSess,respCmd);
			byte[] content=null;
			//消息内容填充
			switch (respCmd) {
			case PROTOCOL_TYPE_LOGIN_RESP:{
				content = new byte[10];
				int dateTime = (int) (System.currentTimeMillis()/1000);
				ByteArrayUtil.setLeUINT4(content, 6, dateTime);
				break;
			}
			case PROTOCOL_TYPE_HEARTBEAT_RESP:{
				break;
			}
			case PROTOCOL_TYPE_TEXT_MSG_RESP:{
				break;
			}
			case PROTOCOL_TYPE_ALARM_RESP:{
				break;
			}
			case PROTOCOL_TYPE_RFID_RESP:{
				break;
			}
			}
			p.setContent(content);
			
			ioSess.write(p.toBytes());
		}
	}

	@Override
	public void pushMsg(IoSession ioSession, PushMsg msg) {
		// TODO Auto-generated method stub

	}

}
