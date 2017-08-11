package com.chh.ap.cs.client.protocol.base;

import com.chh.ap.cs.client.SessionManager;
import com.chh.ap.cs.push.PushMsg;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

/**
 * 协议解析器
 * @author fulr
 *
 */
public interface ProtocolHandler {

	/**
	 * 判断是否满足decode条件
	 * @param remainingBytes
	 * @return
	 */
	public MessageDecoderResult decodable(byte[] remainingBytes);
	
	/**
	 * 数据帧解析器
	 * @param session
	 * @param in
	 * @param out
	 * @return
	 * @throws Exception
	 */
	public MessageDecoderResult decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception;
	
	/**
	 *  未登录设备，做登录校验
	 * @param ioSession
	 * @param message
	 * @return
	 */
	public SessionManager.SessionInfo login(IoSession ioSession, byte[] message);
	
	/**
	 * 登录成功后，数据处理
	 * @param ioSession
	 * @param message
	 */
	public void messageHandler(IoSession ioSession, byte[] message);
	
	/**
	 * 推送消息
	 * @param ioSession
	 * @param msg
	 */
	public void pushMsg(IoSession ioSession, PushMsg msg);
}
