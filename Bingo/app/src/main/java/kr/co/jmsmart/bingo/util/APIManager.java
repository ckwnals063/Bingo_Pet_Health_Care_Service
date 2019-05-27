package kr.co.jmsmart.bingo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.icu.util.TimeZone;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.wapplecloud.libwapple.HttpLoadTask;
import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import kr.co.jmsmart.bingo.data.Sleepdoc_10_min_data_type;
import kr.co.jmsmart.bingo.data.UserDevice;

/**
 * Created by ZZQYU on 2019-01-06.
 */

public class APIManager{
    private static String TAG = "APIManager";
    private Context mContext;

    private boolean flag = false;
    ArrayList<UserGroup> userGroupResult;
    ArrayList<PetType> petTypeResult;
    ArrayList<UserDevice> userDeviceResult;
    HashMap<String, String> userDeviceResultMap;

    private static APIManager instance;

    private static final String URL_BASE =  "http://13.209.110.240:8080";
    private static final String[] URL_LOGIN = new String[]{"/login", MyHttpLoadTask.METHOD_POST};
    private static final String[] URL_LOGOUT = new String[]{"/logout", MyHttpLoadTask.METHOD_GET};

    private static final String[] URL_FINDPASSWORD = new String[]{"/findpassword", MyHttpLoadTask.METHOD_GET};

    private static final String[] URL_GET_USER_GROUP_LIST = new String[]{"/list/group", MyHttpLoadTask.METHOD_GET};
    private static final String[] URL_NEW_USER_SETTING = new String[]{"/user/set", MyHttpLoadTask.METHOD_POST};
    private static final String[] URL_GET_USER_SETTING = new String[]{"/user/set", MyHttpLoadTask.METHOD_GET};

    private static final String[] URL_NEW_ACCOUNT = new String[]{"/user", MyHttpLoadTask.METHOD_POST};
    private static final String[] URL_SELECT_NOW_ACCOUNT = new String[]{"/user", MyHttpLoadTask.METHOD_GET};
    private static final String[] URL_UPDATE_USER = new String[]{"/user", MyHttpLoadTask.METHOD_PUT};
    private static final String[] URL_DELETE_USER = new String[]{"/user", MyHttpLoadTask.METHOD_DELETE};
    private static final String[] URL_EXIST_CHECK = new String[]{"/user/exist", MyHttpLoadTask.METHOD_GET};

    private static final String[] URL_GET_COMPANION_LIST = new String[]{"/list/breeds", MyHttpLoadTask.METHOD_GET};

    private static final String[] URL_NEW_COMPANION_PET = new String[]{"/companion", MyHttpLoadTask.METHOD_POST};
    private static final String[] URL_UPDATE_COMPANION_PET = new String[]{"/companion", MyHttpLoadTask.METHOD_PUT};
    private static final String[] URL_DELETE_COMPANION_PET = new String[]{"/companion", MyHttpLoadTask.METHOD_DELETE};
    private static final String[] URL_GET_MY_PET_LIST = new String[]{"/companion", MyHttpLoadTask.METHOD_GET};

    private static final String[] URL_UPLOAD_PROFILE = new String[]{"/companion/profile", MyHttpLoadTask.METHOD_POST_FILE};
    private static final String[] URL_GET_PROFILE = new String[]{"/companion/profile", MyHttpLoadTask.METHOD_GET};

    private static final String[] URL_INSERT_COMPAIONDATA = new String[]{"/companion/data", MyHttpLoadTask.METHOD_POST};

    private static final String[] URL_GET_MY_MAIN_PROFICIENCY1 = new String[]{"/coach/before", MyHttpLoadTask.METHOD_GET};
    private static final String[] URL_GET_MY_MAIN_PROFICIENCY2 = new String[]{"/coach/after", MyHttpLoadTask.METHOD_GET};
    private static final String[] URL_GET_MY_DETAIL_PROFICIENCY = new String[]{"/coachbydate", MyHttpLoadTask.METHOD_GET};
    private static final String[] URL_GET_DETAIL_DATA = new String[]{"/coachbydate",MyHttpLoadTask.METHOD_GET};
    private static final String[] URL_GET_COACH_GRAPH = new String[]{"/coach/stat/day", MyHttpLoadTask.METHOD_GET};

    private static final String[] URL_GET_DAILY_DATA = new String[]{"/coach/daily", MyHttpLoadTask.METHOD_GET};
    private static final String[] URL_GET_DAILY_GRAPH = new String[]{"/coach/stat/daily", MyHttpLoadTask.METHOD_GET};


    private static final String[] URL_GET_MONTH_GRAPH = new String[]{"/coach/stat/month", MyHttpLoadTask.METHOD_GET};

    private static final String[] URL_NEW_DEVICE = new String[]{"/user/device", MyHttpLoadTask.METHOD_POST};
    private static final String[] URL_USER_DEVICE_LIST = new String[]{"/user/device", MyHttpLoadTask.METHOD_GET};
    private static final String[] URL_UPDATE_DEVICE = new String[]{"/user/device", MyHttpLoadTask.METHOD_PUT};

    private static final String[] URL_CREATE_SHARE_KEY = new String[]{"/user/share", MyHttpLoadTask.METHOD_POST};
    private static final String[] URL_DELETE_SHARE_KEY = new String[]{"/user/share",MyHttpLoadTask.METHOD_DELETE};
    private static final String[] URL_RETRIEVE_SHARE_KEY = new String[]{"/user/share",MyHttpLoadTask.METHOD_GET};


    private static final String[] URL_WEEKLY_ADD_CARD = new String[]{"/coach/weekly",MyHttpLoadTask.METHOD_POST};
    private static final String[] URL_WEEKLY_CARD_LIST = new String[]{"/coach/weekly",MyHttpLoadTask.METHOD_GET};
    private static final String[] URL_WEEKLY_GRAPH_DATA = new String[]{"/coach/stat/weekly", MyHttpLoadTask.METHOD_GET};
    private static final String[] URL_WEEKLY_DELETE = new String[]{"/coach/weekly", MyHttpLoadTask.METHOD_DELETE};


    private static final String[] URL_RANK = new String[]{"/rank", MyHttpLoadTask.METHOD_GET};



    private String localTime;
    private String timeArea;

    private MyHttpLoadTask task;

    private Gson gson;


    private APIManager(Context context){
        mContext = context;
        Calendar calendar = Calendar.getInstance();

        Date currentLocalTime = calendar.getTime();
        SimpleDateFormat date = new SimpleDateFormat("Z");
        localTime = date.format(currentLocalTime);
        timeArea = calendar.getTimeZone().getID();
        gson = new Gson();
    }


    public static APIManager getInstance(Context context) {
        if( instance == null )
            instance = new APIManager(context);

        return instance;
    }

    /*
    1. /com/login.do
    @param    ID   Password
    @return   status   Message
    * */

    public void login(String userId, String password, ResponseCallback callback) {
        task =  new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true; // 다다다다 누르는 경우 쓰레드가 인터럽트 되면서 생기는 에러 무시

        String url = URL_BASE + URL_LOGIN[0];

        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("ID", userId);
        inputMap.put("Password", password);
        // convert map to JSON String
        String jsonString = gson.toJson(inputMap);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url , jsonString, URL_LOGIN[1]);
    }


    /*1
    /com/logout.do
    @param
    @return   status   Message
    */
    public void logout(String userId, ResponseCallback callback) {
        task =  new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true; // 다다다다 누르는 경우 쓰레드가 인터럽트 되면서 생기는 에러 무시

        String url = URL_BASE + URL_LOGOUT[0]+ "?userId="+ userId;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

    }

    public void newCompanionPet(String petNm, String petCd, String petWgtKg, String petWgtLb, char petSex, String petBirth, String userId, ResponseCallback callback) {
        task =  new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true; // 다다다다 누르는 경우 쓰레드가 인터럽트 되면서 생기는 에러 무시

        double kg=0.0 ,lb=0.0, kgToLb = 2.20462;
        if(petWgtKg.equals("")){
            lb = Double.parseDouble(petWgtLb);
            kg = lb / kgToLb;
        }
        else if(petWgtLb.equals("")){
            kg = Double.parseDouble(petWgtKg);
            lb = kg * kgToLb;
        }

        petWgtKg = String.format("%.2f", kg);
        petWgtLb = String.format("%.2f", lb);


        //kg * 2.20462
        String url = URL_BASE + URL_NEW_COMPANION_PET[0];
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("petNm", petNm);
        inputMap.put("petCd", petCd);
        inputMap.put("petWgtKg", petWgtKg);
        inputMap.put("petWgtLb", petWgtLb);
        inputMap.put("petSex", petSex+"");
        inputMap.put("petBirth", petBirth);
        inputMap.put("userId", userId);
        // convert map to JSON String
        String jsonString = gson.toJson(inputMap);
        Log.d("addPet", "newCompanionPet: " + url);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url , jsonString, URL_NEW_COMPANION_PET[1]);
    }
    //
    public void updateCompanionPet(String petSrn, String petNm, String petCd, String petWgtKg, String petWgtLb, char petSex, String petBirth, String userId, ResponseCallback callback) {
        task =  new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true; // 다다다다 누르는 경우 쓰레드가 인터럽트 되면서 생기는 에러 무시

        double kg=0.0 ,lb=0.0, kgToLb = 2.20462;
        if(petWgtKg.equals("")){
            lb = Double.parseDouble(petWgtLb);
            kg = lb / kgToLb;
        }
        else if(petWgtLb.equals("")){
            kg = Double.parseDouble(petWgtKg);
            lb = kg * kgToLb;
        }

        petWgtKg = String.format("%.2f", kg);
        petWgtLb = String.format("%.2f", lb);


        //kg * 2.20462
        String url = URL_BASE + URL_UPDATE_COMPANION_PET[0];

        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("petSrn", petSrn);
        inputMap.put("petNm", petNm);
        inputMap.put("petCd", petCd);
        inputMap.put("petWgtKg", petWgtKg);
        inputMap.put("petWgtLb", petWgtLb);
        inputMap.put("petSex", petSex+"");
        inputMap.put("petBirth", petBirth);
        inputMap.put("userId", userId);
        // convert map to JSON String
        String jsonString = gson.toJson(inputMap);
        Log.i(TAG, "[updateCompanionPet]" + jsonString);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url , jsonString, URL_UPDATE_COMPANION_PET[1]);
    }

    public void findPassword(String userId, ResponseCallback callback){
        MyHttpLoadTask task = new MyHttpLoadTask(mContext, callback, true);

        String url = URL_BASE + URL_FINDPASSWORD[0] + "?userId=" + userId;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    public void uploadProfile(String userId, String petSrn, Bitmap bitmap, ResponseCallback callback) {
        MyHttpLoadTask task = new MyHttpLoadTask(mContext, callback);

        String url = URL_BASE + URL_UPLOAD_PROFILE[0];
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("petSrn", petSrn);
        inputMap.put("userId", userId);

        HashMap<String, Bitmap> filesMap = new HashMap<>();
        filesMap.put("files[0]", bitmap);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url , inputMap, URL_UPLOAD_PROFILE[1], filesMap);
    }
    public static String getProfileUrl(String profileNo) {
        return URL_BASE + URL_GET_PROFILE[0]+"/"+profileNo;
    }
    //
    public void removeCompanionPet(String petSrn, String userId, ResponseCallback callback) {
        task =  new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true; // 다다다다 누르는 경우 쓰레드가 인터럽트 되면서 생기는 에러 무시

        String url = URL_BASE + URL_DELETE_COMPANION_PET[0]
                + "?petSrn="+petSrn
                + "&userId="+userId;

        /*Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("petSrn", petSrn);
        inputMap.put("userId", userId);
        // convert map to JSON String
        String jsonString = gson.toJson(inputMap);*/

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url , "", URL_DELETE_COMPANION_PET[1]);
    }
    //
    public void addNewDevice(String petSrn, String macAddr, String userId, ResponseCallback callback) {
        task =  new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true; // 다다다다 누르는 경우 쓰레드가 인터럽트 되면서 생기는 에러 무시

        String url = URL_BASE + URL_NEW_DEVICE[0];

        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("petSrn", petSrn);
        inputMap.put("macAddr", macAddr);
        inputMap.put("userId", userId);
        // convert map to JSON String
        String jsonString = gson.toJson(inputMap);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, jsonString, URL_NEW_DEVICE[1] );
    }

    //
    private void userDeviceList(String userId, ResponseCallback callback) {
        task =  new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true; // 다다다다 누르는 경우 쓰레드가 인터럽트 되면서 생기는 에러 무시

        String url = URL_BASE + URL_USER_DEVICE_LIST[0]+"?userId="+userId;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url );
    }
    /*
    4. 반려견 견종 리스트 (권한 필요없음)
   /com/getCompanionList.do
   @param
   @return
   pet_code
   pet_nm
   status   Message
    */
    private void getCompanionPetList(ResponseCallback callback) {
        task =  new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true; // 다다다다 누르는 경우 쓰레드가 인터럽트 되면서 생기는 에러 무시

        String url = URL_BASE + URL_GET_COMPANION_LIST[0];

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url );
    }
    /*
    5. 회원가입 (권한 필요없음)
        /com/newAccount.do
        @param
            userId
            groupCd (그룹코드) -> 6번에서 그룹리스트를 들고오세요
            userNm
            userPw
            userPhone(no hipen)
            userSex(F or M)
           userBirth
    */
    public void newAccount(String userId, String groupCd, String userNm, String userPw, String userPhone, char userSex, String userBirth, ResponseCallback callback) {
        task =  new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true; // 다다다다 누르는 경우 쓰레드가 인터럽트 되면서 생기는 에러 무시

        String url = URL_BASE + URL_NEW_ACCOUNT[0];

        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("userId", userId);
        inputMap.put("groupCd", groupCd);
        inputMap.put("userNm", userNm);
        inputMap.put("userPw", userPw);
        inputMap.put("userPhone", userPhone);
        inputMap.put("userSex", userSex+"");
        inputMap.put("userBirth", userBirth);
        // convert map to JSON String
        String jsonString = gson.toJson(inputMap);

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url , jsonString, URL_NEW_ACCOUNT[1]);
    }

    public void userExistCheck(String userId , ResponseCallback callback){
        task = new MyHttpLoadTask(mContext, callback, true, true);


        String url = URL_BASE +
                URL_EXIST_CHECK[0]
                + "?userId=" + userId;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    /*
    6. 그룹리스트 가져오기 (권한 필요없음)
   /com/getUserGroupList.do
   @param
   @return
      groupCd
      groupName
    */
    private void getUserGroupList(ResponseCallback callback) {
        task =  new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true; // 다다다다 누르는 경우 쓰레드가 인터럽트 되면서 생기는 에러 무시

        String url = URL_BASE + URL_GET_USER_GROUP_LIST[0];

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }
    /*
    7. 사용자 정보 가져오기 (권한 필요함)
   /com/selectNowAccount.do
   @param

   @return
   userId
   userNm
   userPhone
   userSex
   userBirth
   userRegisterDttm
    */
    public void selectNowAccount(String userId, ResponseCallback callback) {
        task =  new MyHttpLoadTask(mContext, callback, true, true);

        task.ignoreError = true; // 다다다다 누르는 경우 쓰레드가 인터럽트 되면서 생기는 에러 무시

        String url = URL_BASE + URL_SELECT_NOW_ACCOUNT[0]
                + "?userId="+ userId;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url );
    }

    public void getMypetList(String userId, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext,callback,false,true);

        task.ignoreError = true;

        String url = URL_BASE + URL_GET_MY_PET_LIST[0] + "?userId=" + userId;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    public void getMyMainProficiency(String userId,String petSrn, ResponseCallback callback){

        task = new MyHttpLoadTask(mContext,callback,false,true);

        task.ignoreError = true;
        String url = URL_BASE + (MentUtil.getHour()<10?URL_GET_MY_MAIN_PROFICIENCY1:URL_GET_MY_MAIN_PROFICIENCY2)[0] +"?userId="+ userId+"&petSrn=" + petSrn + "&timearea=" + timeArea;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
    }

    public void getMyMainBefore(String userId,String petSrn, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext,callback,false,true);

        task.ignoreError = true;
        String url = URL_BASE + URL_GET_MY_MAIN_PROFICIENCY1[0] +"?userId="+ userId+"&petSrn=" + petSrn + "&timearea=" + timeArea;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
    }
    public void getMyMainAfter(String userId,String petSrn, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext,callback,false,true);

        task.ignoreError = true;
        String url = URL_BASE + URL_GET_MY_MAIN_PROFICIENCY2[0] +"?userId="+ userId+"&petSrn=" + petSrn + "&timearea=" + timeArea;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
    }

    public void getCoachGraph(String userId,String petSrn, String inpDate ,ResponseCallback callback){
        task = new MyHttpLoadTask(mContext,callback,false,true);

        task.ignoreError = true;
        //http://192.168.0.14:8080/coach/graph?userId=ex1@naver.com&petSrn=4&inpMonth=201901&timezone=%2B0900
        String url = URL_BASE + URL_GET_COACH_GRAPH[0] +"?userId="+ userId+"&petSrn=" + petSrn + "&inpDate=" + inpDate + "&timearea=" + timeArea;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
    }
    public void getDailyData(String userId,String petSrn, String inpDate, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext,callback,false,true);

        task.ignoreError = true;
        String url = URL_BASE + URL_GET_DAILY_DATA[0] +"?userId="+ userId+"&petSrn=" + petSrn + "&timearea=" + timeArea + "&inpDate=" + inpDate;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
    }

    public void getDailyGraph(String userId,String petSrn, String inpDate ,ResponseCallback callback){
        task = new MyHttpLoadTask(mContext,callback,false,true);

        task.ignoreError = true;
        String url = URL_BASE + URL_GET_DAILY_GRAPH[0] +"?userId="+ userId+"&petSrn=" + petSrn + "&inpDate=" + inpDate + "&timearea=" + timeArea;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
    }

    public void getMonthGraph(String userId, String petSrn, String inpDate, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext,callback,false,true);

        task.ignoreError = true;
        String url = URL_BASE + URL_GET_MONTH_GRAPH[0] +"?userId="+ userId+"&petSrn=" + petSrn + "&inpDate=" + inpDate + "&timearea="+timeArea;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
    }


    public void getDetailData(String userId, String petSrn, String inpMonth, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext,callback,true,true);

        task.ignoreError = true;
        String url = URL_BASE + URL_GET_DETAIL_DATA[0] +
                "?userId=" + userId +
                "&petSrn=" + petSrn +
                "&inpMonth=" + inpMonth +
                "&timezone=" + localTime.replace("+","%2B");

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    public Map<String, String> jsonToMap(JSONObject json){
        Map<String, String> result = new HashMap<>();
        Iterator<String> iTag = json.keys();
        while(iTag.hasNext()){
            String tag = iTag.next();
            try {
                result.put(tag, json.getString(tag));
            } catch (Exception e) {
                result.put(tag, "");
            }
        }
        return result;
    }
    public int floatToUFloat(float f){
        int unsignedShort = (int) f;
        unsignedShort = unsignedShort & 0xFFFF;
        return unsignedShort;
    }

    public void insertCompaionData(Sleepdoc_10_min_data_type data, String petSrn, String userId, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext,callback,false,true);

        task.ignoreError = true;

        String url = URL_BASE + URL_INSERT_COMPAIONDATA[0];
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("userId", userId);
        inputMap.put("petSrn", petSrn);
        inputMap.put("barkPoint", floatToUFloat(data.steps)+"");//
        inputMap.put("tLux", data.t_lux+"");
        inputMap.put("avgLux", floatToUFloat(data.avg_lux)+"");//
        inputMap.put("avgK", floatToUFloat(data.avg_k)+"");//
        inputMap.put("sTick", data.s_tick+"");
        inputMap.put("eTick", data.e_tick+"");
        inputMap.put("vectorX", floatToUFloat(data.vector_x)+"");//
        inputMap.put("vectorY", floatToUFloat(data.vector_y)+"");//
        inputMap.put("vectorZ", floatToUFloat(data.vector_z)+"");//
        inputMap.put("timezone", localTime);
        // convert map to JSON String
        String jsonString = gson.toJson(inputMap);

        Log.i("insertCompaionData", url);

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url, jsonString, URL_INSERT_COMPAIONDATA[1]);
    }
    public void updateUser(String userId,String userNm,String userPhone,String userSex,String userBirth, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true;


        String url = URL_BASE + URL_UPDATE_USER[0];
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("userId", userId);
        inputMap.put("userNm", userNm);
        inputMap.put("userPhone", userPhone);
        inputMap.put("userSex", userSex+"");
        inputMap.put("userBirth", userBirth);
        // convert map to JSON String
        String jsonString = gson.toJson(inputMap);

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, jsonString, URL_UPDATE_USER[1]);
    }

    public void deleteUser(String userId, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true;
        String url = URL_BASE + URL_DELETE_USER[0] + "?userId="+userId;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, "", URL_DELETE_USER[1]);
    }

    public void updateDeviceMac(String petSrn, String userId, String mac, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true;

        Log.d("updateDeviceMac", "Update Mac is " + mac);
        if(TextUtils.isEmpty(mac)){
            mac = "";
        }
        else{
            mac.replace(":", "%3a");
        }

        String url = URL_BASE + URL_UPDATE_DEVICE[0];

        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("userId", userId);
        inputMap.put("petSrn", petSrn);
        inputMap.put("macAddr", mac);
        // convert map to JSON String
        String jsonString = gson.toJson(inputMap);

        Log.d("updateDeviceMac", "URL : " + url);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url, jsonString, URL_UPDATE_DEVICE[1]);
    }

    public void createShareKey(String userId, String shareUserId, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext, callback, true, true);

        task.ignoreError = true;

        String url = URL_BASE + URL_CREATE_SHARE_KEY[0];

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("userId",userId);
        inputMap.put("sUserId",shareUserId);
        String jsonString = gson.toJson(inputMap);

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, jsonString, URL_CREATE_SHARE_KEY[1]);
    }

    public void deleteShareKey(String tokenId, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext, callback, true, false);
        task.ignoreError = true;

        String url = URL_BASE + URL_DELETE_SHARE_KEY[0] + "?tokenId=" + tokenId;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url,null,URL_DELETE_SHARE_KEY[1]);
    }

    public void retrieveShareKey(String userId,ResponseCallback callback){
        task = new MyHttpLoadTask(mContext, callback, true, false);

        task.ignoreError = true;

        String url = URL_BASE + URL_RETRIEVE_SHARE_KEY[0] + "?userId=" + userId;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }


    public void addWeeklyCard(String petSrn, String userId, String inpDate, int typeCd, ResponseCallback callback) {
        task =  new MyHttpLoadTask(mContext, callback, false, true);

        task.ignoreError = true; // 다다다다 누르는 경우 쓰레드가 인터럽트 되면서 생기는 에러 무시

        String url = URL_BASE + URL_WEEKLY_ADD_CARD[0];

        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("petSrn", petSrn);
        inputMap.put("userId", userId);
        inputMap.put("inpDate", inpDate);
        inputMap.put("typeCd", "" +typeCd);
        // convert map to JSON String
        String jsonString = gson.toJson(inputMap);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, jsonString, URL_WEEKLY_ADD_CARD[1] );
    }
    public void getWeeklyCardList(String userId, String petSrn, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext, callback, true, false);

        task.ignoreError = true;

        String url = URL_BASE + URL_WEEKLY_CARD_LIST[0] + "?userId=" + userId + "&petSrn="+petSrn;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }


    public void getWeeklyGraphData(String userId, String petSrn,String inpDate, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext, callback, true, false);

        task.ignoreError = true;

        String url = URL_BASE + URL_WEEKLY_GRAPH_DATA[0] + "?userId=" + userId + "&petSrn=" + petSrn + "&inpDate=" + inpDate + "&timearea=" + timeArea;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    public void deleteWeekItem(String userId, String petSrn, String dataSrn, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext, callback, true);

        task.ignoreError = true;

        String url = URL_BASE + URL_WEEKLY_DELETE[0] + "?userId=" + userId + "&petSrn=" + petSrn + "&dataSrn=" + dataSrn;

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, "" ,URL_WEEKLY_DELETE[1]);
    }

    public void getRankData(String userId, String petSrn, ResponseCallback callback){
        task = new MyHttpLoadTask(mContext, callback, true, false);
        task.ignoreError = true;
        String url = URL_BASE + URL_RANK[0] + "?userId=" + userId + "&petSrn=" + petSrn + "&timearea=" + timeArea;
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }


    public ArrayList<UserGroup> getUserGroupList(){
        flag = false;
        userGroupResult = new ArrayList<>();
        getUserGroupList(new ResponseCallback() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                userGroupResult = null;
                flag = true;
            }

            @Override
            public void onDataReceived(JSONObject jsonResponse) {
                try {
                    Log.i("APIManager", "[getUserGroupList] result json : \n"+ jsonResponse.toString());
                    ArrayList<UserGroup> result = new ArrayList<>();
                    JSONArray ja = jsonResponse.getJSONArray("groupList");
                    int i = 0 ;
                    while(!ja.isNull(i)){
                        result.add(new UserGroup(jsonToMap(ja.getJSONObject(i++))));
                    }
                    userGroupResult = result;
                    flag = true;
                }catch (Exception e){

                }
            }

            @Override
            public void onReceiveResponse() {

            }
        });
        Thread th = new Thread(){
            @Override
            public void run() {
                super.run();
                try{
                    while (!flag) {
                        Thread.sleep(500);
                    }
                }catch (Exception e){}
            }
        };
        th.start();
        try {
            th.join();
            return userGroupResult;
        }catch (Exception e){
            if(th.isAlive()) th.interrupt();
            return null;
        }
    }
    //getCompanionPetList
    public ArrayList<PetType> getPetTypeList(){
        flag = false;
        petTypeResult = new ArrayList<>();
        getCompanionPetList(new ResponseCallback() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                petTypeResult = null;
                flag = true;
            }

            @Override
            public void onDataReceived(JSONObject jsonResponse) {
                try {
                    Log.i("APIManager", "[getPetTypeList] result json : \n"+ jsonResponse.toString());
                    ArrayList<PetType> result = new ArrayList<>();
                    JSONArray ja = jsonResponse.getJSONArray("companionList");
                    int i = 0 ;
                    while(!ja.isNull(i)){
                        result.add(new PetType(jsonToMap(ja.getJSONObject(i++))));
                    }
                    petTypeResult = result;
                    flag = true;
                }catch (Exception e){

                }
            }

            @Override
            public void onReceiveResponse() {

            }
        });
        Thread th = new Thread(){
            @Override
            public void run() {
                super.run();
                try{
                    while (!flag) {
                        Thread.sleep(500);
                    }
                }catch (Exception e){}
            }
        };
        th.start();
        try {
            th.join();
            return petTypeResult;
        }catch (Exception e){
            if(th.isAlive()) th.interrupt();
            return null;
        }
    }
    public ArrayList<UserDevice> getUserDeviceList(String userId){
        flag = false;
        userDeviceResult = new ArrayList<>();
        userDeviceList(userId, new ResponseCallback() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                Log.i("APIManager", "[getUserDeviceList] onError : "+errorCode);
                userDeviceResult = null;
                flag = true;
            }

            @Override
            public void onDataReceived(JSONObject jsonResponse) {
                try {
                    Log.i("APIManager", "[getUserDeviceList] result json : \n"+ jsonResponse.toString());
                    ArrayList<UserDevice> result = new ArrayList<>();
                    JSONArray ja = jsonResponse.getJSONArray("userDeviceList");
                    int i = 0 ;
                    while(!ja.isNull(i)){
                        result.add(new UserDevice(jsonToMap(ja.getJSONObject(i++))));
                    }
                    userDeviceResult = result;
                    flag = true;
                }catch (Exception e){
                    Log.i("sadasdasdsa", Log.getStackTraceString(e));
                }
            }

            @Override
            public void onReceiveResponse() {

            }
        });
        Thread th = new Thread(){
            @Override
            public void run() {
                super.run();
                try{
                    while (!flag) {
                        Thread.sleep(500);
                    }
                }catch (Exception e){}
            }
        };
        th.start();
        try {
            th.join();
            return userDeviceResult;
        }catch (Exception e){
            if(th.isAlive()) th.interrupt();
            return null;
        }
    }
    public HashMap<String, String> getUserDeviceList2(String userId){
        flag = false;
        userDeviceResultMap = new HashMap<>();
        userDeviceList(userId, new ResponseCallback() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                userDeviceResultMap = null;
                flag = true;
            }

            @Override
            public void onDataReceived(JSONObject jsonResponse) {
                try {
                    Log.i("APIManager", "[getUserDeviceList] result json : \n"+ jsonResponse.toString());
                    HashMap<String, String> result = new HashMap<>();
                    JSONArray ja = jsonResponse.getJSONArray("userDeviceList");
                    int i = 0 ;
                    while(!ja.isNull(i)){
                        JSONObject row = ja.getJSONObject(i++);
                        result.put(row.getString("petSrn"), row.getString("macAddr"));
                    }
                    userDeviceResultMap = result;
                    flag = true;
                }catch (Exception e){
                    Log.i("sadasdasdsa", Log.getStackTraceString(e));
                }
                // 방법2
                for( Map.Entry<String, String> elem : userDeviceResultMap.entrySet() ){
                    System.out.println( String.format("키 : %s, 값 : %s", elem.getKey(), elem.getValue()) );
                }
            }

            @Override
            public void onReceiveResponse() {

            }
        });
        Thread th = new Thread(){
            @Override
            public void run() {
                super.run();
                try{
                    while (!flag) {
                        Thread.sleep(500);
                    }
                }catch (Exception e){}
            }
        };
        th.start();
        try {
            th.join();
            return userDeviceResultMap;
        }catch (Exception e){
            if(th.isAlive()) th.interrupt();
            return null;
        }
    }
    public class UserGroup{
        private String groupCd;
        private String groupName;
        public UserGroup(Map<String, String> map){
            this.groupCd = map.get("groupCd");
            this.groupName = map.get("groupName");
        }
        public String getGroupCd(){return this.groupCd;}
        public String getGroupName(){return this.groupName;}
    }
    public class PetType{
        private String petCode;
        private String petNm;
        public PetType(Map<String, String> map){
            this.petCode = map.get("petCode");
            this.petNm = map.get("petNm");
        }
        public String getPetCode(){return this.petCode;}
        public String getPetNm(){return this.petNm;}
    }

}