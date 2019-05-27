package kr.co.jmsmart.bingo.data;

import android.content.Context;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.adapter.DataCardListAdapter;
import kr.co.jmsmart.bingo.util.MentUtil;

/**
 * Created by ZZQYU on 2019-02-19.
 */

public class PetData {
    private String cdCl;
    private String dataCd;
    private String cdVal;
    private int dataVal;
    private int dataGoal;

    public PetData(String cdCl, String dataCd, String cdVal, int dataVal, int dataGoal) {
        this.cdCl = cdCl;           // target
        this.dataCd = dataCd;       // grade
        this.cdVal = cdVal;         // gradeText
        this.dataVal = dataVal;     // val
        this.dataGoal = dataGoal;   // goal
    }

    public String getCdCl() {
        return cdCl.replace("_cl", "");
    }

    public void setCdCl(String cdCl) {
        this.cdCl = cdCl;
    }

    public String getCdVal() {
        return cdVal;
    }

    public void setCdVal(String cdVal) {
        this.cdVal = cdVal;
    }

    public int getDataVal() {
        return dataVal;
    }

    public void setDataVal(int dataVal) {
        this.dataVal = dataVal;
    }

    public int getDataGoal() {
        return dataGoal;
    }

    public void setDataGoal(int dataGoal) {
        this.dataGoal = dataGoal;
    }

    public String getDataCd() {
        return dataCd;
    }
    public int getIntDataCd(){
        return Integer.parseInt(dataCd);
    }

    public void setDataCd(String dataCd) {
        this.dataCd = dataCd;
    }


    public String getTitle() {
        return DataCardListAdapter.titles[DataCardListAdapter.tags.indexOf(getCdCl())];
    }
    public int getIconResourceId() {
        return DataCardListAdapter.iconIds[DataCardListAdapter.tags.indexOf(getCdCl())];
    }
    public String getRate(Context c) {
        return MentUtil.getInstance(c).getRateString(getCdVal());
    }

    public String getMent(Context c, String petNm, boolean isMain) {
        if(isMain)
            return MentUtil.getInstance(c).getMainMent(getCdCl(), petNm, getIntDataCd());
        else
            return MentUtil.getInstance(c).getSubMent(getCdCl(), petNm, getIntDataCd());
    }

    public String getValue(Context c) {
        return MentUtil.getInstance(c).valAddUnit(getCdCl(), getDataVal(), false);
    }

    public String getBaseValue(Context c) {
        String ans = MentUtil.getInstance(c).valAddUnit(getCdCl(), getDataGoal(), true);
        if(!ans.contains("kcal")) ans="/"+ans;
        return ans;
    }

}