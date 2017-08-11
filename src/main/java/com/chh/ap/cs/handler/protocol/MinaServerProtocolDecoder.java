package com.chh.ap.cs.handler.protocol;


import com.chh.ap.cs.client.ClientType;
import com.chh.ap.cs.server.tcp.MinaSocketServer;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chh.ap.cs.util.Constant;

public class MinaServerProtocolDecoder implements MessageDecoder {
	
    public static final Logger log = LoggerFactory.getLogger(MinaServerProtocolDecoder.class);
	
//    /**
//     * 不同clientType的session最大空闲时长配置,单位：ms
//     */
//    private Map<Integer, Integer>  sessionIdleMap;
    
//    private ClientRecognizer clientRecognizer;
    
//    /**
//     * 设备解析器Map
//     * Map<ClientType,ProtocolHandler>
//     */
//    private Map<Integer, ProtocolHandler> protocolHandlerMap;
//    private Map<Integer, ClientType> clientTypeMap;
    
	/**
	 * 该方法相当于预读取,用于判断是否是可用的解码器,这里对IoBuffer读取不会影响数据包的大小
	 * 该方法结束后IoBuffer会复原,所以不必担心调用该方法时,position已经不在缓冲区起始位置
	 */
	@Override
	public MessageDecoderResult decodable(IoSession ioSession, IoBuffer in) {
		ClientType clientType = (ClientType) ioSession.getAttribute(Constant.SESSION_CLIENT_TYPE);
        byte[] remainingBytes = new byte[in.remaining()];
        in.get(remainingBytes);
      //  log.debug("MessageDecoderResult decodable:=["+ StrUtil.bytesToHexString(remainingBytes) + "]");
        
        if (clientType == null) {
        	//识别设备类型
        	//最大数据帧协议头长度
    		if(remainingBytes.length < ClientType.MAX_HEAD_LEN){
    	    //表示当前的读入的数据不够判断是否能够使用这个解码器解码，然后再次调用decodable()方法检查其它解码器，如果都是NEED_DATA,则等待下次输入；
    			return MessageDecoderResult.NEED_DATA;  
    		}
    		
//    		msgFrame = new byte[in.remaining()];
//    		in.get(msgFrame);
    		//通过接收到的第一条消息来判断接入客户端的类型
            int typeId = MinaSocketServer.getClientRecognizer().getClient(remainingBytes);
            //未知设备类型禁止接入
            if (typeId == ClientType.CLIENT_TYPE_UNKNOWN) {
                log.warn("未知设备接入,IP[{}],消息内容[{}]", ioSession.getRemoteAddress().toString(), remainingBytes);
                ioSession.closeNow();
                return MessageDecoderResult.NOT_OK;
            } else {
            	//设置类型
            	clientType = MinaSocketServer.getClientTypeMap().get(typeId);
                ioSession.getConfig().setIdleTime(IdleStatus.READER_IDLE, clientType.getSessionIdle());
                ioSession.setAttribute(Constant.SESSION_CLIENT_TYPE, clientType);
                log.info("sessionCode[{}],接入设备类型[{}]",ioSession.hashCode(),clientType.getClientTypeName());
            }
        } 
        //根据客户端类型，分包
        return clientType.getProtocolHandler().decodable(remainingBytes);
		
	}

	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		ClientType clientType = (ClientType) session.getAttribute(Constant.SESSION_CLIENT_TYPE);
		return clientType.getProtocolHandler().decode(session,in,out);
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
		//暂时什么都不做
	}
	
//	public Map<Integer, Integer> getSessionIdleMap() {
//		return sessionIdleMap;
//	}
//
//
//	public void setSessionIdleMap(Map<Integer, Integer> sessionIdleMap) {
//		this.sessionIdleMap = sessionIdleMap;
//	}
	
//    public ClientRecognizer getClientRecognizer() {
//        return clientRecognizer;
//    }
//
//    @Autowired
//    public void setClientRecognizer(ClientRecognizer clientRecognizer) {
//        this.clientRecognizer = clientRecognizer;
//    }
}
