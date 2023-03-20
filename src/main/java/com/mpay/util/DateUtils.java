package com.mpay.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

	private static final DateFormat longDbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final DateFormat shortDbFormat = new SimpleDateFormat("yyyy-MM-dd");

	private static Timestamp todayTimestamp = new Timestamp(System.currentTimeMillis());

	public static String todayShortDbFormat() {
		Calendar cal = Calendar.getInstance();
		return shortDbFormat.format(cal.getTime());
	}

	public static Timestamp todayTimestamp() {
		return todayTimestamp;
	}

	public static String addRemoveDaysFromToday(int days) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		return shortDbFormat.format(cal.getTime());
	}
}
