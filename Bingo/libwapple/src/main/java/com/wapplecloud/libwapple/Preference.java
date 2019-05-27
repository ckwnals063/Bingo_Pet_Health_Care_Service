package com.wapplecloud.libwapple;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Preference {
	public static final String PREF_NAME = "my_pref";
	
	// 세션 아이디
	public static final String KEY_SESSION_ID = "connect.sid";

	private static SharedPreferences pref;

	public static void createInstance(Context context) {
		if( pref == null )
			pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
	}
	
	public static boolean contains(String key) {
		return pref.contains(key);
	}
	
	public static void setPreference(String key, String value) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static void setPreference(String key, boolean b) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(key, b);
		editor.commit();
	}

	public static String getPreference(String key) {
		return pref.getString(key, null);
	}

	public static boolean getPreferenceBoolean(String key) {
		return pref.getBoolean(key, false);
	}

	public static boolean getPreferenceBoolean(String key, boolean _default) {
		return pref.getBoolean(key, _default);
	}

	public static void setPreference(String key, int value) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public static int getPreferenceInt(String key) {
		return pref.getInt(key, 0);
	}

	public static void setPreference(String key, double value) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putFloat(key, (float)value);
		editor.commit();
	}
	
	public static double getPreferenceDouble(String key) {
		return (double)pref.getFloat(key, 0.0f);
	}

	public static String getSessionId() {
		String connectSid = getPreference(KEY_SESSION_ID);
		return connectSid;
	}
	
	public static void saveSessionId() {
		String cookie = HttpLoadTask.getCookieString();
		setPreference(KEY_SESSION_ID, cookie);
	}
	
	public static void clearSessionId() {
		setPreference(KEY_SESSION_ID, null);
	}
}
