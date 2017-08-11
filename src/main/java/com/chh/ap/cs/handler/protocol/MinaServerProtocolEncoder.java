package com.chh.ap.cs.handler.protocol;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;


public class MinaServerProtocolEncoder implements MessageEncoder<byte[]> {

	@Override
	public void encode(IoSession session, byte[] message, ProtocolEncoderOutput out) throws Exception {
		IoBuffer buffer = IoBuffer.allocate(message.length).setAutoExpand(true);
		buffer.put(message);
		buffer.flip();
		out.write(buffer);
	}
}
