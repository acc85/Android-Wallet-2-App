package info.blockchain.wallet.ui.Utilities;

import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import piuk.blockchain.android.R;

import android.content.Context;

public class DateUtil {
	
	private static DateUtil instance = null;
	private static Date now = null;
	private static Context context = null;

	private DateUtil() {

	}

	public static DateUtil getInstance(Context ctx) {
		
		now = new Date();
		context = ctx;
		
		if(instance == null) {
			instance = new DateUtil();
		}
		
		return instance;
	}

	public String formatted(long date) {
		String ret = null;
		
		long hours24 = 60L * 60L * 24;
		long now = System.currentTimeMillis() / 1000L;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(now * 1000L));
		int nowYear = cal.get(Calendar.YEAR);
		int nowDay = cal.get(Calendar.DAY_OF_MONTH);

		cal.setTime(new Date(date * 1000L));
		int thenYear = cal.get(Calendar.YEAR);
		int thenDay = cal.get(Calendar.DAY_OF_MONTH);
		
		// within 24h
		if(now - date < hours24) {
			if(thenDay < nowDay) {
				SimpleDateFormat sd = new SimpleDateFormat("HH:mm");
				ret = context.getString(R.string.YESTERDAY) + " @ " + sd.format(date * 1000L);
			}
			else {
				SimpleDateFormat sd = new SimpleDateFormat("HH:mm");
				ret = context.getString(R.string.TODAY) + " @ " + sd.format(date * 1000L);
			}
		}
		// within 48h
		else if(now - date < (hours24 * 2)) {
			SimpleDateFormat sd = new SimpleDateFormat("E dd MMM @ HH:mm");
			ret = sd.format(date * 1000L);
		}
		else {
			if(thenYear < nowYear) {
				SimpleDateFormat sd = new SimpleDateFormat("dd MMM yyyy");
				ret = sd.format(date * 1000L);
			}
			else {
				SimpleDateFormat sd = new SimpleDateFormat("E dd MMM @ HH:mm");
				ret = sd.format(date * 1000L);
			}
		}
		
		return ret;
	}

	public static String dateFormatted(long date) {
		String ret = null;

		long hours24 = 60L * 60L * 24;
		long now = System.currentTimeMillis() / 1000L;

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(now * 1000L));
		int nowYear = cal.get(Calendar.YEAR);
		int nowDay = cal.get(Calendar.DAY_OF_MONTH);

		cal.setTime(new Date(date * 1000L));
		int thenYear = cal.get(Calendar.YEAR);
		int thenDay = cal.get(Calendar.DAY_OF_MONTH);

		// within 24h
		if(now - date < hours24) {
			if(thenDay < nowDay) {
				SimpleDateFormat sd = new SimpleDateFormat("HH:mm");
				ret = context.getString(R.string.YESTERDAY) + " @ " + sd.format(date * 1000L);
			}
			else {
				SimpleDateFormat sd = new SimpleDateFormat("HH:mm");
				ret = context.getString(R.string.TODAY) + " @ " + sd.format(date * 1000L);
			}
		}
		// within 48h
		else if(now - date < (hours24 * 2)) {
			SimpleDateFormat sd = new SimpleDateFormat("E dd MMM @ HH:mm");
			ret = sd.format(date * 1000L);
		}
		else {
			if(thenYear < nowYear) {
				SimpleDateFormat sd = new SimpleDateFormat("dd MMM yyyy");
				ret = sd.format(date * 1000L);
			}
			else {
				SimpleDateFormat sd = new SimpleDateFormat("E dd MMM @ HH:mm");
				ret = sd.format(date * 1000L);
			}
		}

		return ret;
	}

}
