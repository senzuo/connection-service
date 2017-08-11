package com.chh.ap.cs.handler.protocol;

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;


public class MinaProtocolCodecFactory extends DemuxingProtocolCodecFactory {
	public MinaProtocolCodecFactory(){
		super.addMessageEncoder(byte[].class, MinaServerProtocolEncoder.class);//编码
		super.addMessageDecoder(MinaServerProtocolDecoder.class);//tcp解码
	}
}
