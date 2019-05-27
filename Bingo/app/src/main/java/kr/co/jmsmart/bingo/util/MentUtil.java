package kr.co.jmsmart.bingo.util;

import android.content.Context;
import android.content.Intent;

import com.wapplecloud.libwapple.Log;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import kr.co.jmsmart.bingo.R;

/**
 * Created by ZZQYU on 2019-02-17.
 */

public class MentUtil {
    private Context c ;
    private static MentUtil instance = null;

    private int loveTime1 = 10, loveTime2 = 21;
    private int sunTime = 17;
    private int barkTime1 = 19, barkTime2 = 22;



    private MentUtil(Context context){
        c= context;
    }
    public static MentUtil getInstance(Context context){
        if(instance == null)
            instance = new MentUtil(context);
        return instance;
    }
    public static int getHour(){
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }


    public String getMainLoveCard(String petNm, String grade){
        String ans = "";
        int hour = getHour();
        if(hour < loveTime1) ans = c.getString(R.string.ment_pink_box_1);
        else if(hour >= loveTime2) ans = c.getString(R.string.ment_pink_box_3);
        else ans = c.getString(R.string.ment_pink_box_2);
        return String.format(ans, petNm, grade);
    }

    public String getMainMent(String cdCl, String petNm, int type){
        switch (cdCl){
            case "love":
                return getMainLoveMent(petNm, type);
            case "dep":
                return getMainDepressionMent(petNm, type);
            case "sun":
                return getMainSunMent(petNm, type);
            case "bark":
                return getMainBarkMent(petNm, type);
            case "rest":
                return getMainRestMent(petNm, type);
        }
        return "-";
    }

    public String getMainLoveMent(String petNm, int type){
        int[][] ments = new int[][]{
                {R.string.ment_love_1_f, R.string.ment_love_1_d, R.string.ment_love_1_c, R.string.ment_love_1_b, R.string.ment_love_1_a},
                {R.string.ment_love_2_f, R.string.ment_love_2_d, R.string.ment_love_2_c, R.string.ment_love_2_b, R.string.ment_love_2_a},
                {R.string.ment_love_3_f, R.string.ment_love_3_d, R.string.ment_love_3_c, R.string.ment_love_3_b, R.string.ment_love_3_a}
        };
        int hour = getHour();
        int index = hour<loveTime1?0:(hour>=loveTime2?2:1);
        return String.format(c.getString(ments[index][type]), petNm);
    }
    public String getMainDepressionMent(String petNm, int type){
        int[][] ments = new int[][]{
                {R.string.ment_dep_1_1, R.string.ment_dep_1_2, R.string.ment_dep_1_3,R.string.ment_dep_1_4},
                {R.string.ment_dep_2_1, R.string.ment_dep_2_2, R.string.ment_dep_2_3,R.string.ment_dep_2_4}
        };
        int index = getHour()<sunTime?0:1;
        return String.format(c.getString(ments[index][type]), petNm);
    }

    public String getMainSunMent(String petNm, int type){
        int[][] ments = new int[][]{
                {R.string.ment_sun_1_1, R.string.ment_sun_1_2, R.string.ment_sun_1_3,R.string.ment_sun_1_4},
                {R.string.ment_sun_2_1, R.string.ment_sun_2_2, R.string.ment_sun_2_3,R.string.ment_sun_2_4}
        };
        int index = getHour()<sunTime?0:1;
        return String.format(c.getString(ments[index][type]), petNm);
    }
    public String getMainBarkMent(String petNm, int type){
        int[][] ments = new int[][]{
                {R.string.ment_bark_1_1, R.string.ment_bark_1_2, R.string.ment_bark_1_3,R.string.ment_bark_1_4},
                {R.string.ment_bark_2_1, R.string.ment_bark_2_2, R.string.ment_bark_2_3,R.string.ment_bark_2_4},
                {R.string.ment_bark_3_1, R.string.ment_bark_3_2, R.string.ment_bark_3_3,R.string.ment_bark_3_4}
        };
        int hour = getHour();
        int index = hour<barkTime1?0:(hour>=barkTime2?2:1);
        return String.format(c.getString(ments[index][type]), petNm);
    }
    public String getMainRestMent(String petNm, int type){
        int[] ments = {R.string.ment_rest_1_1, R.string.ment_rest_1_2, R.string.ment_rest_1_3,R.string.ment_rest_1_4};
        return String.format(c.getString(ments[type]), petNm);
    }

    public String getSubMent(String cdCl, String petNm, int type){
        switch (cdCl){
            case "sun":
                return getSubSunMent(petNm, type);
            case "uv":
                return getSubUvMent(petNm, type);
            case "vit":
                return getSubVitaMent(petNm, type);
            case "act":
                return getSubActMent(petNm, type);
            case "play":
                return getSubPlayMent(petNm, type);
            case "rest":
                return getSubRestMent(petNm, type);
            case "love":
                return getMainLoveMent(petNm, type);
            case "dep":
                return getMainDepressionMent(petNm, type);
            case "bark":
                return getMainBarkMent(petNm, type);
        }
        return "-";
    }


    public String getSubSunMent(String petNm, int type){
        int[][] ments = new int[][]{
                {R.string.sub_sun_1_1, R.string.sub_sun_1_2, R.string.sub_sun_1_3,R.string.sub_sun_1_4},
                {R.string.sub_sun_2_1, R.string.sub_sun_2_2, R.string.sub_sun_2_3,R.string.sub_sun_2_4}
        };
        int index = getHour()<sunTime?0:1;
        return String.format(c.getString(ments[index][type]), petNm);
    }
    public String getSubUvMent(String petNm, int type){
        int[][] ments = new int[][]{
                {R.string.sub_uv_1_1, R.string.sub_uv_1_2, R.string.sub_uv_1_3,R.string.sub_uv_1_4,R.string.sub_uv_1_5},
                {R.string.sub_uv_2_1, R.string.sub_uv_2_2, R.string.sub_uv_2_3,R.string.sub_uv_2_4,R.string.sub_uv_2_5}
        };
        int index = getHour()<sunTime?0:1;
        return String.format(c.getString(ments[index][type]), petNm);
    }
    public String getSubVitaMent(String petNm, int type){
        int[][] ments = new int[][]{
                {R.string.sub_vita_1_1, R.string.sub_vita_1_2, R.string.sub_vita_1_3,R.string.sub_vita_1_4},
                {R.string.sub_vita_2_1, R.string.sub_vita_2_2, R.string.sub_vita_2_3,R.string.sub_vita_2_4}
        };
        int index = getHour()<sunTime?0:1;
        return String.format(c.getString(ments[index][type]), petNm);
    }
    public String getSubActMent(String petNm, int type){
        int[][] ments = new int[][]{
                {R.string.sub_act_1_1, R.string.sub_act_1_2, R.string.sub_act_1_3,R.string.sub_act_1_4},
                {R.string.sub_act_2_1, R.string.sub_act_2_2, R.string.sub_act_2_3,R.string.sub_act_2_4},
                {R.string.sub_act_3_1, R.string.sub_act_3_2, R.string.sub_act_3_3,R.string.sub_act_3_4}
        };
        int hour = getHour();
        int index = hour<barkTime1?0:(hour>=barkTime2?2:1);
        return String.format(c.getString(ments[index][type]), petNm);
    }
    public String getSubPlayMent(String petNm, int type){
        int[][] ments = new int[][]{
                {R.string.sub_play_1_1, R.string.sub_play_1_2, R.string.sub_play_1_3,R.string.sub_play_1_4},
                {R.string.sub_play_2_1, R.string.sub_play_2_2, R.string.sub_play_2_3,R.string.sub_play_2_4},
                {R.string.sub_play_3_1, R.string.sub_play_3_2, R.string.sub_play_3_3,R.string.sub_play_3_4}
        };
        int hour = getHour();
        int index = hour<barkTime1?0:(hour>=barkTime2?2:1);
        return String.format(c.getString(ments[index][type]), petNm);
    }
    public String getSubRestMent(String petNm, int type){
        int[] ments = {R.string.sub_rest_1_1, R.string.sub_rest_1_2, R.string.sub_rest_1_3,R.string.sub_rest_1_4};
        return String.format(c.getString(ments[type]), petNm);
    }



    public String getRateString(String rate){
        HashMap<String, Integer> map = new HashMap<>();
        map.put("WARNING", R.string.rate_1);
        map.put("LOW", R.string.rate_2);
        map.put("NORMAL", R.string.rate_3);
        map.put("ENOUGH", R.string.rate_4);
        map.put("OVER", R.string.rate_5);
        map.put("BAD", R.string.rate_6);
        map.put("GOOD", R.string.rate_7);
        return c.getString(map.get(rate));
    }

    public String valAddUnit(String cdCl, int value){
        DecimalFormat formatter = new DecimalFormat("###,###");
        String ans = "";
        switch(cdCl) {
            case "uv":case "love": case "dep": {
                ans = "%";
                break;
            }
            case "sun":{
                ans = "lux";
                break;
            }
            case "luxpol": {
                ans = "%";
                break;
            }
            case "vit": {
                ans = "iu";
                break;
            }
            case "bark": {
                ans = "bark";
                break;
            }
            case "cal": {
                ans = "kcal";
                break;
            }
            case "act": case "play":case "rest":{
                return minToTime(value);
            }
        }
        if(value==-1)return ans;
        return formatter.format(value)+ans;
    }
    public String valAddUnit(String cdCl, int value, boolean isAddUnit){
        DecimalFormat formatter = new DecimalFormat("###,###");
        String ans = "";
        switch(cdCl) {
            case "uv":case "love": case "dep": {
                ans = "%";
                break;
            }
            case "sun":{
                ans = "lux";
                break;
            }
            case "luxpol": {
                ans = "%";
                break;
            }
            case "vit": {
                ans = "iu";
                break;
            }
            case "bark": {
                ans = "bark";
                break;
            }
            case "cal": {
                ans = "kcal";
                if(isAddUnit)return ans;
                break;
            }
            case "act": case "play":case "rest":{
                return minToTime(value);
            }
        }
        return formatter.format(value)+(isAddUnit?ans:"");
    }

    public String minToTime(int min){
        int h = min/60;
        return min==0?"0:00"+"":(h + ":" + String.format("%02d", min%60));
    }


}
