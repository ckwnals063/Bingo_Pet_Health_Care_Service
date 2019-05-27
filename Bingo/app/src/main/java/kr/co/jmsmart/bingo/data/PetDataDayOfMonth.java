package kr.co.jmsmart.bingo.data;

import android.content.Context;
import android.os.Parcelable;

import com.wapplecloud.libwapple.Log;

import java.io.Serializable;
import java.lang.reflect.Field;

import kr.co.jmsmart.bingo.adapter.DataCardListAdapter;
import kr.co.jmsmart.bingo.util.MentUtil;

/**
 * Created by ZZQYU on 2019-02-19.
 */

public class PetDataDayOfMonth implements Serializable {
    public String idx;
    public String sunVal, sunGrade, sunGradeText;
    public String uvVal, uvGrade, uvGradeText;
    public String vitVal, vitGrade, vitGradeText;
    public String actVal, actGrade, actGradeText;
    public String playVal, playGrade, playGradeText;
    public String restVal, restGrade, restGradeText;
    public String calVal, calGrade, calGradeText;
    public String barkVal, barkGrade, barkGradeText;
    public String depVal, depGrade, depGradeText;
    public String luxpolVal, luxpolGrade, luxpolGradeText;
    public String loveVal, loveGrade, loveGradeText;
    public int getGraphVal(String tag){
        int ans = 0;
        try {
            Field f = this.getClass().getDeclaredField(tag + "Val");
            ans = Integer.parseInt((String)f.get(this));
        }catch (Exception e){

        }
        return ans;
    }
    public PetData getPetData(String tag, int goal){
        PetData  p = null;
        Class c = this.getClass();
        try {
            Field[] fs = {c.getDeclaredField(tag + "Val"), c.getDeclaredField(tag + "Grade"), c.getDeclaredField(tag + "GradeText")};
            for (Field f : fs)
                f.setAccessible(true);//String idx = ;
            p = new PetData(tag, (String)fs[1].get(this), (String)fs[2].get(this), Integer.parseInt((String)fs[0].get(this)), goal);
        }
        catch (Exception e){

        }
        return p;
    }

}