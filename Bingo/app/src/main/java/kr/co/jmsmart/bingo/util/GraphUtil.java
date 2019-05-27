package kr.co.jmsmart.bingo.util;

import android.graphics.Color;

import com.wapplecloud.libwapple.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ZZQYU on 2019-02-15.
 */

public class GraphUtil {
    public static int getTemperatureColor(int temperature){
        int[][] colors =
                {{0x00, 0x00, 0x00},
                        {0xff, 0x00, 0x00},
                        {0xff, 0x80, 0x00},
                        {0xff, 0xff, 0x00},
                        {0xee, 0xee, 0xee},
                        {0x64, 0xee, 0xee},
                        {0x64, 0xff, 0xff}};

        if (temperature<=0) return Color.rgb(colors[0][0], colors[0][1], colors[0][2]);
        else if(temperature>0&&temperature<3000){
            return Color.rgb((colors[0][0]*(3000-temperature)+colors[1][0]*temperature)/3000, (colors[0][1]*(3000-temperature)+colors[1][1]*temperature)/3000, (colors[0][2]*(3000-temperature)+colors[1][2]*temperature)/3000);
        }
        else if(temperature>=8000){
            return Color.rgb(colors[6][0], colors[6][1], colors[6][2]);
        }
        else {
            int index = temperature/1000-2;
            return Color.rgb((colors[index][0]*(1000-temperature%1000)+colors[index+1][0]*(temperature%1000))/1000, (colors[index][1]*(1000-temperature%1000)+colors[index+1][1]*(temperature%1000))/1000, (colors[index][2]*(1000-temperature%1000)+colors[index+1][2]*(temperature%1000))/1000);
        }
    }
    public static class GraphItem{
        //{"dTime":"201902131730","tLux":0.0,"barkPoint":0,"avgK":10000.0}
        private String dTime, avgLux, barkPoint, avgK, uv="0";
        private String now = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());

        public String getdTime() {
            return dTime;
        }
        public String getTime() {
            return dTime.substring(8);
        }
        public float getFloatTime() {
            float ans = Integer.parseInt(dTime.substring(8, 10));
            ans+= dTime.substring(10).equals("30")?0.5f:0f;
            if(getdTime().contains(now))
                ans+=24;
            return ans;
        }
        public String getDate() {
            return dTime.substring(0,8);
        }

        public void setdTime(String dTime) {
            this.dTime = dTime;
        }

        public String gettLux() {
            return avgLux;
        }

        public void setAvgLux(String avgLux) {
            this.avgLux = avgLux;
        }

        public String getBarkPoint() {
            return barkPoint;
        }

        public void setBarkPoint(String barkPoint) {
            this.barkPoint = barkPoint;
        }

        public String getAvgK() {
            return avgK;
        }

        public void setAvgK(String avgK) {
            this.avgK = avgK;
        }

        public String getUv() {
            return uv;
        }

        public void setUv(String uv) {
            this.uv = uv;
        }
    }
}
