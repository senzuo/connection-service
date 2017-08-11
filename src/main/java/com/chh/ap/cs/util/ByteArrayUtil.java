package com.chh.ap.cs.util;

public class ByteArrayUtil {

	public static int getUINT4(byte[] ba, int start) {
		if (ba.length <= start + 3)
			return 0;

		int r = 0;
		r |= 0x00FF & ba[start];
		r = r << 8;
		r |= 0x00FF & ba[start + 1];
		r = r << 8;
		r |= 0x00FF & ba[start + 2];
		r = r << 8;
		r |= 0x00FF & ba[start + 3];
		return r;
	}

	public static void setUINT4(byte[] ba, int start, int value) {
		if (ba.length <= start + 3)
			return;
		ba[start] = (byte) (value >> 24 & 0xFF);
		ba[start + 1] = (byte) (value >> 16 & 0xFF);
		ba[start + 2] = (byte) (value >> 8 & 0xFF);
		ba[start + 3] = (byte) (value & 0xFF);
	}

	public static short getUSHORT2(byte[] ba, int start) {
		if (ba.length <= start + 1)
			return 0;

		short r = 0;
		r |= 0x00FF & ba[start];
		r = (short) (r << 8);
		r |= 0x00FF & ba[start + 1];
		return r;
	}

	public static void setUSHORT2(byte[] ba, int start, int value) {
		if (ba.length <= start + 1)
			return;
		ba[start] = (byte) (value >> 8 & 0xFF);
		ba[start + 1] = (byte) (value & 0xFF);
	}

	public static long getULONG6(byte[] ba, int start) {
		if (ba.length <= start + 5)
			return 0;

		long r = 0;
		r |= 0x00FF & ba[start];
		r = r << 8;
		r |= 0x00FF & ba[start + 1];
		r = r << 8;
		r |= 0x00FF & ba[start + 2];
		r = r << 8;
		r |= 0x00FF & ba[start + 3];
		r = r << 8;
		r |= 0x00FF & ba[start + 4];
		r = r << 8;
		r |= 0x00FF & ba[start + 5];
		return r;
	}
	
	public static void setULONG6(byte[] ba, int start,long value) {
		if (ba.length <= start + 5)
			return ;
		
		ba[start] = (byte) (value >> 40 & 0xFF);
		ba[start + 1] = (byte) (value >> 32 & 0xFF);
		ba[start + 2] = (byte) (value >> 24 & 0xFF);
		ba[start + 3] = (byte) (value >> 16 & 0xFF);
		ba[start + 4] = (byte) (value >> 8 & 0xFF);
		ba[start + 5] = (byte) (value & 0xFF);
	}
	
	public static String byte2str(byte[] ba, int start, int len) {
		if (ba.length <= start + len - 1)
			return null;
		String r = new String();
		for (int i = 0; i < len; i++) {
			if (ba[start + i] == '\0')
				break;
			r += (char) ba[start + i];
		}
		return r;
	}
	
	/**
	 *  little-ending版本
	 * @param ba
	 * @param start
	 * @return
	 */
	public static int getLeUINT4(byte[] ba, int start) {
		if (ba.length <= start + 3)
			return 0;

		int r = 0;
		r |= 0x00FF & ba[start + 3];
		r = r << 8;
		r |= 0x00FF & ba[start + 2];
		r = r << 8;
		r |= 0x00FF & ba[start + 1];
		r = r << 8;
		r |= 0x00FF & ba[start];
		return r;
	}

	/**
	 * little-ending版本
	 * @param ba
	 * @param start
	 * @param value
	 */
	public static void setLeUINT4(byte[] ba, int start, int value) {
		if (ba.length <= start + 3)
			return;
		ba[start] = (byte) (value & 0xFF);
		ba[start + 1] = (byte) (value >> 8 & 0xFF);
		ba[start + 2] = (byte) (value >> 16 & 0xFF);
		ba[start + 3] = (byte) (value >> 24 & 0xFF);
		
	}

	/**
	 * little-ending版本
	 * @param ba
	 * @param start
	 * @return
	 */
	public static short getLeUSHORT2(byte[] ba, int start) {
		if (ba.length <= start + 1)
			return 0;

		short r = 0;
		r |= 0x00FF & ba[start + 1];
		r = (short) (r << 8);
		r |= 0x00FF & ba[start];
		return r;
	}

	/**
	 * little-ending版本
	 * @param ba
	 * @param start
	 * @param value
	 */
	public static void setLeUSHORT2(byte[] ba, int start, int value) {
		if (ba.length <= start + 1)
			return;
		ba[start] = (byte) (value & 0xFF);
		ba[start + 1] = (byte) (value >> 8 & 0xFF);
	}
}
