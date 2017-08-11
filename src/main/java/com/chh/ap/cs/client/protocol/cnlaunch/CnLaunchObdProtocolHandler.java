package com.chh.ap.cs.client.protocol.cnlaunch;

import java.util.HashMap;
import java.util.Map;

import com.chh.ap.cs.client.ClientType;
import com.chh.ap.cs.client.SessionManager;
import com.chh.ap.cs.client.protocol.base.ProtocolHandler;
import com.chh.ap.cs.client.protocol.cnlaunch.packet.CnLaunchPacket;
import com.chh.ap.cs.push.PushMsg;
import com.chh.ap.cs.util.ByteArrayUtil;
import com.chh.ap.cs.util.StrUtil;
import com.chh.ap.cs.util.aes.Encryptor;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chh.ap.cs.dao.impl.DeviceDao;

public class CnLaunchObdProtocolHandler implements ProtocolHandler {

	public static final Logger log = LoggerFactory.getLogger(CnLaunchObdProtocolHandler.class); 
	private  byte[] aesToken = null;   //设备上传令牌
	private  byte[] aeskey = null;   //服务器解析的密钥
	public static Map<Integer,Integer> reqAndRespMap = new HashMap<Integer,Integer>();
	private static final int PROTOCOL_TYPE_HEARTBEAT_RESP = 0x1001;//心跳业务
	private static final int PROTOCOL_TYPE_LOGIN_RESP = 0x1002;//登录业务
	private static final int PROTOCOL_TYPE_POSITION_RESP = 0x1003;//位置业务 （统一在 打包业务中）
	private static final int PROTOCOL_TYPE_CARDATA_RESP = 0x1004;//车辆数据业务 （统一在打包业务中）
	private static final int PROTOCOL_TYPE_DTC_RESP = 0x1005;//故障码业务
	private static final int PROTOCOL_TYPE_CONFIG_RESP = 0x1006; //配置业务
	private static final int PROTOCOL_TYPE_ALARM_RESP = 0x1007; //报警业务
	private static final int PROTOCOL_TYPE_TRIP_RESP = 0x1008;  //行程业务
	private static final int PROTOCOL_TYPE_PACK_RESP = 0x1009; //打包业务
	private short dataLen;  //数据帧长度
	private String sn =null; //设备序列号  
	private boolean isLoginData =false; //是否为登录数据
	
	static{
		reqAndRespMap.put(0x0001, PROTOCOL_TYPE_HEARTBEAT_RESP);//心跳业务
		reqAndRespMap.put(0x0002, PROTOCOL_TYPE_LOGIN_RESP);//登录业务
		reqAndRespMap.put(0x0003, PROTOCOL_TYPE_POSITION_RESP);//位置业务 （统一在 打包业务中，一般不单独回复）
		reqAndRespMap.put(0x0004, PROTOCOL_TYPE_CARDATA_RESP);//车辆数据业务 （统一在打包业务中，一般不单独回复）
		reqAndRespMap.put(0x0005, PROTOCOL_TYPE_DTC_RESP);//故障码业务                          
		reqAndRespMap.put(0x0006, PROTOCOL_TYPE_CONFIG_RESP);//配置业务                                    
		reqAndRespMap.put(0x0007, PROTOCOL_TYPE_ALARM_RESP);//报警业务                                  
		reqAndRespMap.put(0x0008, PROTOCOL_TYPE_TRIP_RESP);//行程业务                           
		reqAndRespMap.put(0x0009, PROTOCOL_TYPE_PACK_RESP);//打包业务                                     
	}
	
	private DeviceDao deviceDao = new DeviceDao();
	
	@Override
	public MessageDecoderResult decodable(byte[] srcdata) {
		// log.debug("decodable 接收到srcdata 数据:"+StrUtil.bytesToHexString( srcdata));
		 dataLen = ByteArrayUtil.getUSHORT2(srcdata, CnLaunchPacket.INDEX_OF_DATA_LEN);
		//   通过业务id 和 固定位数中 密钥长度 判断为登录数据
		//带序列号帧头  判断情况
		if(judgeType(srcdata,CnLaunchPacket.PROTOCOL_TYPE)){
			isLoginData =true;
		}else{
			isLoginData =false;
		}
		//log.debug("【数据为登录数据】 ifloginData ="+isLoginData+" 数据长度：dataLen="+dataLen);
		if(srcdata.length>=dataLen) {
			return MessageDecoderResult.OK;
		} 
		return MessageDecoderResult.NEED_DATA;
	}
	
	private boolean judgeType(byte[] srcdata,boolean flag) {
		if(flag){ //(5+2+1+4+4+2+1+32+12)=63
			return (ByteArrayUtil.getUSHORT2(srcdata, CnLaunchPacket.INDEX_OF_PROTOCOL_TYPE)==2&&ByteArrayUtil.getUSHORT2(srcdata, 63)==162);
		}else{//(18+2+1+4+4+2+1+32+12)=76
			return (ByteArrayUtil.getUSHORT2(srcdata,CnLaunchPacket.INDEX_OF_PROTOCOL_TYPE)==2&&ByteArrayUtil.getUSHORT2(srcdata, 76)==162);	
		}
	}

	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		//注意，在decode方法进行读取操作，会影响数据包的大小，decode需要判断协议中哪些已经decode完，哪些还没decode。当decode完成后，
		//调用ProtocolDecoderOutput的write进行输出，并返回MessageDecoderResult.OK表示已经decode完成。
		//接收到的完整数据包
		if(dataLen>0){
		byte[] serviceData = null;
		try {
			serviceData = new byte[dataLen+ CnLaunchPacket.FRAME_TAIL_AND_HEAD_LENGTH];
			//log.debug("serviceData length :=["+ serviceData.length + "]");
			in.get(serviceData);
			//log.debug("decode serviceData:=["+ StrUtil.bytesToHexString(serviceData) + "]");
				int pos = 0;
				byte[] frameIdentifier = new byte[CnLaunchPacket.INDEX_OF_DATA_LEN]; // 帧标识
				System.arraycopy(serviceData, pos, frameIdentifier, 0, CnLaunchPacket.INDEX_OF_DATA_LEN);
				pos += CnLaunchPacket.INDEX_OF_DATA_LEN;
				int frameLength = ByteArrayUtil.getUSHORT2(serviceData, pos); // 帧长
				pos += 2;
				byte[] headMessage = new byte[CnLaunchPacket.Middle_LENGTH]; // 厂商标识+消息标识+扩展字段
				System.arraycopy(serviceData, pos, headMessage, 0, CnLaunchPacket.Middle_LENGTH);
				pos += CnLaunchPacket.Middle_LENGTH;
				// 加密数据段(业务ID + 协议号 + 数据区)，登录响应未加密 【17  即 除去 帧头帧尾  非加密部分 长度】
				byte[] body = new byte[frameLength - 17];
				System.arraycopy(serviceData, pos, body, 0, body.length);
				pos += body.length;
				byte[] tail = new byte[CnLaunchPacket.TAIL_LENGTH]; // 尾部 时间戳+校验码 +帧尾巴
				System.arraycopy(serviceData, pos, tail, 0, CnLaunchPacket.TAIL_LENGTH);
				if (isLoginData) 
				{		
						obdLoginData(body); // 登录成功之后，需要对 sim 软硬件版本好进行保存的 ，再重新定义抽取
				} else // 密文
				{
					//  这里需要传入 aesToken 字节数据 和 会话id 必须一致 【即 元征obd 每次登录 aesToken 不一致】
					if (this.aeskey != null) {
						byte[] newBody=null;
						try {
							 newBody = Encryptor.decrypt(this.aeskey, body);// 使用RSA密钥解密业务数据
						//	log.debug("【接收到解密应答 (数据区)】:"+ StrUtil.bytesToHexString(newBody));
						} catch (Exception e) {
							e.printStackTrace();
							log.error("RSA解密失败:", e);
						}
						// 解密数据后重新组包，新完整数据长度， 26为除去(业务ID + 协议号 + 数据区)的长度 
						byte[] newData = new byte[newBody.length + CnLaunchPacket.FRAME_OTHER_LENGTH];
						int index =0;
						System.arraycopy(frameIdentifier, 0, newData, index, CnLaunchPacket.INDEX_OF_DATA_LEN);// 帧标识
						index+=CnLaunchPacket.INDEX_OF_DATA_LEN;
					    ByteArrayUtil.getUSHORT2(newData, index); // 帧长
						index+=2; //帧长长度
						System.arraycopy(headMessage, 0, newData, index, CnLaunchPacket.Middle_LENGTH); // 厂商标识+消息标识+扩展字段
						index+=CnLaunchPacket.Middle_LENGTH;
						System.arraycopy(newBody, 0, newData, index,newBody.length); // （解密后得出）业务ID+协议号+业务数据
						index+=newBody.length;
						System.arraycopy(tail, 0, newData,index, CnLaunchPacket.TAIL_LENGTH);// 尾部 时间戳+校验码
						
						serviceData=newData;
					} else {
						log.error("aesToken ==null");
					}
				//	log.debug("【接收到服务器的解密数据】:" + StrUtil.bytesToHexString(serviceData));
				}

			} catch (Exception e) {
				e.printStackTrace();
				log.error("解析数据异常", e);
			}
			out.write(serviceData);
			return MessageDecoderResult.OK;
		} else {
			return MessageDecoderResult.NEED_DATA;
		}
	}

	/**
	 * 登录数据 在接入服务器 就 需要单独解析，响应回去
	 * @param serviceData
	 */
	private void obdLoginData(byte[] serviceData) {
		//2 + 1 + 32 + 12 +2+ 162 + 15 + 8 +20 = 246;
		//log.debug("【登录数据 数组】:" + StrUtil.bytesToHexString(serviceData));
		try {
			int off = 0;
			byte[] businessId = new byte[2]; 	//业务ID// businessId
			System.arraycopy(serviceData, off, businessId, 0, 2);
			off += 2;
			byte[] protocolNumber = new byte[1]; 	//协议号// Protocol number
			System.arraycopy(serviceData, off, protocolNumber, 0, 1);
			off += 1;
			byte[] ak = new byte[32]; // ak
			System.arraycopy(serviceData, off, ak, 0, 32);
			off += 32;
			byte[] sn = new byte[12]; // sn
			System.arraycopy(serviceData, off, sn, 0, 12);
			this.sn= new String(sn, "UTF-8");
			off += 12;
			int keyLength = ByteArrayUtil.getUSHORT2(serviceData, off); // 密钥长度
			off += 2;
			byte[] aesToken = new byte[keyLength]; // 密钥 数组
			System.arraycopy(serviceData, off, aesToken, 0, aesToken.length);
			this.aesToken =aesToken;
			off += aesToken.length;
			byte[] imei = new byte[15]; // 设备IMEI码
			System.arraycopy(serviceData, off, imei, 0, 15);
			off += 15;
			byte[] sim = new byte[20]; // SIM卡号
			System.arraycopy(serviceData, off, sim, 0, 20);
			off += 20;
			// dpuString 类型格式 长度获取
			byte[] hvLength = new byte[1]; // 硬件版本号数组
			System.arraycopy(serviceData, off, hvLength, 0, 1);
			off += 1;
			int hardwareVersionLength = Integer.parseInt(StrUtil.bytesToHexString(hvLength), 16); // 密钥长度
			byte[] hardwareVersion = new byte[hardwareVersionLength]; // 硬件版本号
			System.arraycopy(serviceData, off, hardwareVersion, 0,hardwareVersionLength);
			off += hardwareVersionLength;
			byte[] svLength = new byte[1]; // 软件版本号数组
			System.arraycopy(serviceData, off, svLength, 0, 1);
			off += 1;
			int softwareVersionLength = Integer.parseInt(StrUtil.bytesToHexString(svLength), 16); // 密钥长度
			byte[] softwareVersion = new byte[softwareVersionLength]; // 软件版本号
			System.arraycopy(serviceData, off, softwareVersion, 0,softwareVersionLength);
			//log.info("【aesToken 数组】:" + StrUtil.bytesToHexString(aesToken));
			//log.info("【元征obd序列号】:" + this.sn);
		} catch (Exception e) {
				e.printStackTrace();
				log.error("解析元征obd登录数据异常", e);
			}
	}

	@Override
	public SessionManager.SessionInfo login(IoSession ioSession, byte[] message) {
		//String deviceId = ByteArrayUtil.byte2str(message, CnLaunchPacket.INDEX_OF_OBD_SN, 24);
		SessionManager.SessionInfo sess = null;
		try {
			sess = deviceDao.login(sn, ClientType.CLIENT_TYPE_OBD_YUANZHEN);
			if(sess==null) {
				log.info("设备[ID:{}]未入库，登录失败。",sn);
			}
		} catch (Exception e) {
			log.error("设备[ID:{}]登录失败，发生异常。",sn, e);
		}
		return sess;
	}

	@Override
	public void messageHandler(IoSession ioSess, byte[] message) {
        //大端模式 （protocolType/业务id）
		int  cmdId = ByteArrayUtil.getUSHORT2(message, CnLaunchPacket.INDEX_OF_PROTOCOL_TYPE);
		//消息标识 起始从第 8位开始
		int messageID=ByteArrayUtil.getUINT4(message, CnLaunchPacket.INDEX_OF_MESSAGE_IDENTIFIER);
		//设备主动上行消息，才响应  
		Integer respCmd = reqAndRespMap.get(cmdId);
		if(respCmd!=null) {
			//响应消息
			CnLaunchPacket p = new CnLaunchPacket(ioSess,respCmd);
			byte[] content=null;
			//消息内容填充
			switch (respCmd) {
			case PROTOCOL_TYPE_HEARTBEAT_RESP:{//心跳
				content = new  byte [3];//2+1（无内容）
				ByteArrayUtil.setUSHORT2(content, 0, PROTOCOL_TYPE_HEARTBEAT_RESP);
				content[2] = 1; 
			//	log.debug("心跳响应-响应内容"+StrUtil.bytesToHexString(content));
				break;
			}
			case PROTOCOL_TYPE_LOGIN_RESP:{//登录
			//	log.debug("登录响应");	
				//封装响应时间
				content = new byte[146];
				//(2+1)+1+1+1+1+1+1+1+1+1+1+1+128+4=146
				int index = 0;
		        //messageHandler 是查询数据库之后才能执行到
				ByteArrayUtil.setUSHORT2(content, index, PROTOCOL_TYPE_LOGIN_RESP);	
				index +=2;
				content[index++] = 1; 
				
				// TODO  是否注册  和 激活 该数据必须是 app端 入库配置之后 获取到 
				//  已经有判断是否登录 的情况下 这里直接设置 注册和激活 为 1
				content[index++] = 1; // 是否注册
				content[index++] = 10; // 心跳周期
				content[index++] = 1; // 是否已经激活
				content[index++] = 10; // 位置信息采样周期
				content[index++] = 10; // OBD(总线)数据采集周期
				content[index++] = 10; // OBD(总线)数据传输周期
				content[index++] = 10; // 打包数据传输周期
				content[index++] = 32; //打包数据容量
				content[index++] = 1; //打包数据是否需要应答
				content[index++] = 46; // 各模块全开 （根据协议 进制排列处理）
				//【62（00111110） 全开】【  30（00011110） 不开gps】【46（00101110）不开wifi】 等等
				content[index++] = 15; // 传感器唤醒阀值
				byte[] aeskeytemp=null;
				try {
					aeskey=Encryptor.getAesKey(this.aesToken);
					aeskeytemp = Encryptor.encrypt(Encryptor.getPublicKey(this.aesToken), aeskey);
				} catch (Exception e) {
					e.printStackTrace();
					log.error("使用RSA公钥加密Aes密钥异常", e);
				}
				System.arraycopy(aeskeytemp, 0, content, index, aeskeytemp.length); // 令牌
				index += aeskeytemp.length;
				// TODO 该数据必须是 app端 入库配置之后 获取到
				int carId = 4; //目前暂写固定的 丰田车型
				ByteArrayUtil.setUINT4(content, index, carId); // 车型id
				break;
			}
			case PROTOCOL_TYPE_DTC_RESP:{//故障码
				content = new  byte [3];//2+1 （无内容） 
				ByteArrayUtil.setUSHORT2(content, 0, PROTOCOL_TYPE_DTC_RESP);
				content[2] = 1; 
			//	log.debug("故障码响应-响应内容"+StrUtil.bytesToHexString(content));
				break;
			}
			case PROTOCOL_TYPE_CONFIG_RESP:{ //配置  服务器是push ；接头端 应答的 （应答失败可根据业务再push）
			//TODO 	元征盒子不主动 发 配置业务 （配置业务是 服务器主动发起的）
				break;
			}
			case PROTOCOL_TYPE_ALARM_RESP:{//报警
				//“响应的业务id”+“协议号”+“数据内容”  加密 发送  
				content = new  byte [6];//2+1+2+1
				int index = 0;
				ByteArrayUtil.setUSHORT2(content, index, PROTOCOL_TYPE_ALARM_RESP);//业务ID
				index +=2;
				content[index++] = 1;  //协议号
				System.arraycopy(message, CnLaunchPacket.INDEX_OF_PROTOCOL_TYPE+3, content, index, 2); // 报警类型 
				index +=2;
				content[index]=1;//标记为响应成功
				//log.debug("报警响应-响应内容"+StrUtil.bytesToHexString(content));
				break;
			}
			case PROTOCOL_TYPE_TRIP_RESP:{//行程
				content = new  byte [9]; //2+1+1+4+1
				int index = 0;
				ByteArrayUtil.setUSHORT2(content, index, PROTOCOL_TYPE_TRIP_RESP);//业务ID
				index +=2;
				content[index++] = 1;  //协议号
				//行程标志(1 byte)	行程序号（4 byte）	结果(1 byte)
				System.arraycopy(message, CnLaunchPacket.INDEX_OF_PROTOCOL_TYPE+3, content, index, 5); // 报警类型 
				index +=5;
				content[index]=1;//标记为响应成功
				//log.debug("行程响应-响应内容"+StrUtil.bytesToHexString(content));
				break;
			}
			case PROTOCOL_TYPE_PACK_RESP:{ //打包业务
				content = new  byte [3];
				ByteArrayUtil.setUSHORT2(content, 0, PROTOCOL_TYPE_PACK_RESP);//业务ID
				content[2] = 1; 
				//log.debug("打包业务响应-响应内容"+StrUtil.bytesToHexString(content));
				break;
			}
			}
			p.setMessageID(messageID); //元征盒子  messageID 响应返回
			
			if(content!=null){
			if(respCmd==PROTOCOL_TYPE_LOGIN_RESP){
				p.setContent(content);
			}else{//加密之后进行发送  
				//“响应的业务id”+“协议号”+“数据内容”  加密 发送  
				try {
					aeskey=Encryptor.getAesKey(this.aesToken);
					byte [] encryptcontent=Encryptor.encrypt(aeskey,content);
				//	log.debug("加密后内容"+StrUtil.bytesToHexString(encryptcontent));
					p.setContent(encryptcontent);
				} catch (Exception e) {
					e.printStackTrace();
					log.error("加密内容异常", e);
				}
			}
			ioSess.write(p.toBytes());
			}
		}
	}

	@Override
	public void pushMsg(IoSession ioSession, PushMsg msg) {

		byte[] content=null;
		byte[] data=msg.getMsgData()==null? new byte[0]:msg.getMsgData() ;
		
		content = new  byte [2+1+1+data.length];//2+1+4
		int pop=0;
		ByteArrayUtil.setUSHORT2(content, pop, PROTOCOL_TYPE_CONFIG_RESP);//业务ID
		pop+=2;
		content[pop++] = 1;  //协议号
		content[pop++] = (byte)msg.getMsgType(); 
		System.arraycopy(data, 0, content, pop, data.length); 

		//log.debug("配置数据内容："+StrUtil.bytesToHexString(content));

			CnLaunchPacket p = new CnLaunchPacket(ioSession,PROTOCOL_TYPE_CONFIG_RESP); //判断下发命令固定是 x01006 (4102)
				//“响应的业务id”+“协议号”+“数据内容”  加密 发送  
				try {
					aeskey=Encryptor.getAesKey(this.aesToken);
					byte [] encryptcontent=Encryptor.encrypt(aeskey,content);
//				/	log.debug("加密后内容"+StrUtil.bytesToHexString(encryptcontent));
					p.setContent(encryptcontent);
				} catch (Exception e) {
					e.printStackTrace();
					log.error("加密内容异常", e);
				}
			//	log.debug("配置下发完整内容："+StrUtil.bytesToHexString(p.toBytes()));
			ioSession.write(p.toBytes());
			
			//TODO 调试 下发车型 接头有正常解密收到 车型id 
			//完善 INSERT INTO `t_device_push_msg` VALUES ('5', '1973490000034', '1', 0x00000004, '2016-12-30 10:35:12', '1');
			//需要 web后台管理页面 设置之后插入  t_device_push_msg 表 
			//日志上传调试 sql
			//	INSERT INTO `t_device_push_msg` VALUES ('55', '4973490000034', '18', 0x5876e98f5877a05f0038687474703a2f2f3139322e3136382e3137332e313a383038302f646174612d63656e7465722d6f70656e2f7765622f6c6f6755706c6f6164, '2016-12-30 10:35:12', '1');
			
	}
}
