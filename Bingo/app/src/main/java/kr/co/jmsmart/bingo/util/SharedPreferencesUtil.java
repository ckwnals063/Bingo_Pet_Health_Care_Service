package kr.co.jmsmart.bingo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by ZZQYU on 2019-01-30.
 */

public class SharedPreferencesUtil {
    private static String TAG = "SharedPreferencesUtil";
    private static String LOGIN_SETTING_NAME = "LOGIN_INFO";
    private static String UNIT_SETTING_NAME = "DEFAULT_UNIT";
    public static void setLoginInfo(Context context, String id, String pw){
        SharedPreferences loginInformation = context.getSharedPreferences(LOGIN_SETTING_NAME,0);
        SharedPreferences.Editor editor= loginInformation.edit();
        editor.putString("id", id);
        editor.putString("password" , pw);
        editor.commit();
    }
    public static String[] getLoginInfo(Context context){
        String[] ans = null;
        if(isExistLoginInfo(context)){
            SharedPreferences info = context.getSharedPreferences(LOGIN_SETTING_NAME,0);
            Log.i(TAG, "getLoginInfo " + info.getString("id","") + " " + info.getString("password",""));
            ans = new String[]{info.getString("id",""), info.getString("password","")};
        }
        return ans;
    }
    public static boolean removeLoginInfo(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(LOGIN_SETTING_NAME,0).edit();
        editor.clear();
        return editor.commit();
    }
    public static boolean isExistLoginInfo(Context context){
        SharedPreferences info = context.getSharedPreferences(LOGIN_SETTING_NAME,0);
        Log.i(TAG, "isExistLoginInfo " + info.getString("id","") + " " + info.getString("password",""));
        Log.i(TAG, "isExistLoginInfo " + !info.getString("id", "").isEmpty());
        return !info.getString("id", "").isEmpty();
    }

    public static void setDefaultUnit(Context context, String unit){
        SharedPreferences prefUnit = context.getSharedPreferences(UNIT_SETTING_NAME,0);
        SharedPreferences.Editor editor= prefUnit.edit();
        editor.putString("unit", unit);
        editor.commit();
    }
    public static String getDefaultUnit(Context context){
        String ans = null;
        if(isExistDefaultUnit(context)){
            SharedPreferences prefUnit = context.getSharedPreferences(UNIT_SETTING_NAME,0);
            Log.i(TAG, "getDefaultUnit " + prefUnit.getString("unit","") );
            ans = prefUnit.getString("unit","");
        }
        return ans;
    }
    public static boolean isExistDefaultUnit(Context context){
        SharedPreferences prefUnit = context.getSharedPreferences(UNIT_SETTING_NAME,0);
        Log.i(TAG, "isExistDefaultUnit " + !prefUnit.getString("unit", "").isEmpty());
        return !prefUnit.getString("unit","").isEmpty();
    }
}
