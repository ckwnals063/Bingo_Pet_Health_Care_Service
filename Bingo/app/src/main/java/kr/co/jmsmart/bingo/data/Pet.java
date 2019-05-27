package kr.co.jmsmart.bingo.data;

import android.content.Context;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.util.SharedPreferencesUtil;

/**
 * Created by ZZQYU on 2019-01-16.
 */

public class Pet implements Serializable {
    private int id;
    private String petNm;
    private String breedCd;
    private String breedNm;
    private float kg;
    private float lb;
    private char sex;
    private String birth;
    private String months;
    private String sexStr;
    private String unit;
    private String weight;
    private String fileCode;


    public Pet(JSONObject jObject, Context c){
        try {
            this.id = jObject.getInt("petSrn");
            this.petNm = jObject.getString("petNm");
            this.breedCd = jObject.getString("petCd");
            this.breedNm = jObject.getString("petCdNm");
            this.kg = (float)jObject.getDouble("petWgtKg");
            this.lb = (float)jObject.getDouble("petWgtLb");
            this.sex = jObject.getString("petSex").charAt(0);
            this.birth = jObject.getString("petBirth");
            this.sexStr = sex=='M'?c.getString(R.string.pet_male):c.getString(R.string.pet_female);
            this.unit = SharedPreferencesUtil.getDefaultUnit(c);
            this.weight = (unit.equals("Kg")?kg:lb)+"";
            String fc = jObject.getString("fileCode");
            this.fileCode = fc.equals("null")?null:fc;
        }catch (Exception e){

        }
        this.months = c.getString(R.string.months);
    }
    public String getTitle(){
        return petNm;
    }
    public String getSubText(){
        int month = 0;
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd");
            Date pet = sdf.parse(birth);
            month = getMonthsDifference(pet, new Date());
        }
        catch (Exception e){}


        return breedNm + " | " + sexStr + " | " + month + months + " | " +  weight + unit;
    }



    private int getMonthsDifference(Date date1, Date date2){

        /* 해당년도에 12를 곱해서 총 개월수를 구하고 해당 월을 더 한다. */
        int month1 = date1.getYear() * 12 + date1.getMonth();
        int month2 = date2.getYear() * 12 + date2.getMonth();

        return month2 - month1;
    }

    public int getId() {
        return id;
    }

    public String getPetNm() {
        return petNm;
    }

    public String getBreedCd() {
        return breedCd;
    }

    public String getBreedNm() {
        return breedNm;
    }

    public float getKg() {
        return kg;
    }

    public float getLb() {
        return lb;
    }

    public char getSex() {
        return sex;
    }

    public String getBirth() {
        return birth;
    }

    public String getFileCode() {
        return fileCode;
    }
}
