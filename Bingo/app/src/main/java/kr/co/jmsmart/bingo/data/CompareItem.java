package kr.co.jmsmart.bingo.data;

import android.content.Context;
import android.util.Log;

import kr.co.jmsmart.bingo.adapter.DataCardListAdapter;
import kr.co.jmsmart.bingo.adapter.TimeListAdapter;
import kr.co.jmsmart.bingo.util.MentUtil;

/**
 * Created by ZZQYU on 2019-02-19.
 */

public class CompareItem {
    String TAG = "CompareItem";
    private String dataSrn="";
    private String inpDate="";
    private String cdVal="";
    private String useYn="N";
    private int typeCd = 0;

    public int getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(int typeCd) {
        this.typeCd = typeCd;
    }

    public String getDataSrn() {
        return dataSrn;
    }
    public int getIntDataSrn() {
        return Integer.parseInt(dataSrn);
    }

    public void setDataSrn(String dataSrn) {
        this.dataSrn = dataSrn;
    }

    public String getInpDate() {
        return inpDate;
    }

    public void setInpDate(String inpDate) {
        this.inpDate = inpDate;
    }

    public String getTypeVal() {
        return cdVal.toLowerCase();
    }

    public void setCdVal(String cdVal) {
        this.cdVal = cdVal;
    }

    public boolean isUse() {
        return useYn.equals("Y");
    }

    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }

    public int getIconResourceId() {
        Log.i(TAG, "[getIconResourceId] getTypeVal: "+getTypeVal());
        return TimeListAdapter.iconIds[TimeListAdapter.types.indexOf(getTypeVal())];
    }

    public int getItemTextStringId() {
        return TimeListAdapter.contentTexts[TimeListAdapter.types.indexOf(getTypeVal())];
    }
}