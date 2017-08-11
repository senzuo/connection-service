package com.chh.ap.cs.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    
    public static final String yyyy_MM_dd = "yyyy-MM-dd";

    public static Date parse(String dateStr, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
	 * 转换时间为字符串(格式为年月日十分秒)  yyyy-MM-dd HH:mm:ss
	 * @param Date
	 * @return
	 */
	public static String toDateTimeString(Date date) {
		if (date == null) {
			return null;
		}
		return sdf.format(date);
	}

}
