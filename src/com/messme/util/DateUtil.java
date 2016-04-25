package com.messme.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;


public class DateUtil
{
	public static final SimpleDateFormat DATE_TIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	private static final SimpleDateFormat _DATE_TIME = new SimpleDateFormat("dd.MM.yyyy H:mm");
	private static final SimpleDateFormat _DATE = new SimpleDateFormat("dd.MM.yyyy");
	private static final SimpleDateFormat _TIME = new SimpleDateFormat("H:mm", Locale.US);
	private static final SimpleDateFormat _MIN_SEC = new SimpleDateFormat("mm:ss", Locale.US);
	private static Calendar _Calendar = new GregorianCalendar();

	
	public static String GetDate(Date pDate)
	{
		_Calendar.setTime(pDate);
		_Calendar.add(Calendar.HOUR, (TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings()) / 1000 / 60 / 60);
		return _DATE.format(_Calendar.getTime());
	}
	public static String GetTime(Date pDate)
	{
		_Calendar.setTime(pDate);
		_Calendar.add(Calendar.HOUR, (TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings()) / 1000 / 60 / 60);
		return _TIME.format(_Calendar.getTime());
	}
	public static String GetMinSec(Date pDate)
	{
		_Calendar.setTime(pDate);
		_Calendar.add(Calendar.HOUR, (TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings()) / 1000 / 60 / 60);
		return _MIN_SEC.format(_Calendar.getTime());
	}
	public static String GetDateTime(Date pDate)
	{
		_Calendar.setTime(pDate);
		_Calendar.add(Calendar.HOUR, (TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings()) / 1000 / 60 / 60);
		return _DATE_TIME.format(_Calendar.getTime());
	}
	public static String GetDateTime()
	{
		Date now = new Date();
		_Calendar.setTime(now);
		return _DATE_TIME.format(_Calendar.getTime());
	}
}