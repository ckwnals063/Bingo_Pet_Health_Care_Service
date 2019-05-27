package com.wapplecloud.libwapple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.wapplecloud.libwapple.BuildConfig;

public class Log {
    static final boolean LOG = true;//BuildConfig.DEBUG;
    static final String TAG = "wapple";
    
    public static void i(String tag, String string) {
        if (LOG) printLog("i", tag, string);
    }
    public static void e(String tag, String string) {
        if (LOG) printLog("e", tag, string);
    }
    public static void d(String tag, String string) {
        if (LOG) printLog("d", tag, string);
    }
    public static void v(String tag, String string) {
        if (LOG) printLog("v", tag, string);
    }
    public static void w(String tag, String string) {
        if (LOG) printLog("w", tag, string);
    }
    
    public static void i(String string) {
        if (LOG) printLog("i", TAG, string);
    }
    public static void e(String string) {
        if (LOG) printLog("e", TAG, string);
    }
    public static void d(String string) {
        if (LOG) printLog("d", TAG, string);
    }
    public static void v(String string) {
        if (LOG) printLog("v", TAG, string);
    }
    public static void w(String string) {
        if (LOG) printLog("w", TAG, string);
    }
    
    public static void printLog(String level, String tag, String string) {
    	Class<?> Log;
    	if( string == null )
    		string = "null";
    	
		try {
			Log = Class.forName("android.util.Log");
			Class<?>[] paramString = new Class[2];	
			paramString[0] = String.class;
			paramString[1] = String.class;
			
	    	Method m = Log.getDeclaredMethod(level, paramString);
	    	
	    	int maxLogSize = 3000;
	    	for(int i = 0; i <= string.length() / maxLogSize; i++) {
	    	    int start = i * maxLogSize;
	    	    int end = (i+1) * maxLogSize;
	    	    end = end > string.length() ? string.length() : end;
	    	    m.invoke(null, tag, string.substring(start, end));
	    	}
	    	
	    	
	    	
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

    }    
}