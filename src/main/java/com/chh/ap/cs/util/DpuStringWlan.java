package com.chh.ap.cs.util;


/**
 * DpuStringWlan类
 * 元征协议类型中的一种特殊类型
 */
public class DpuStringWlan {
	private String string;
	private byte length;

	public DpuStringWlan(String str) {
		string = str;
		length = (byte) str.getBytes().length;
	}

	public int getTotalLen() {
		return length + 1;// 总长度为<长度字节>+<字符串>
	}

	public byte[] getBytes() {
		byte[] strdata = string.getBytes();
		byte[] data = new byte[length + 1];
		data[0] = (byte) (length & 0xFF);
		System.arraycopy(strdata, 0, data, 1, length);
		return data;
	}
}
