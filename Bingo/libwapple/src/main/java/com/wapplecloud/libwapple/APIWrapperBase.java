package com.wapplecloud.libwapple;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class APIWrapperBase {

	protected String VERSION = "1.0.0.150805";
	protected String LOGIN = "login";
	protected String LOGOUT = "logout";
	protected String CREATE_DEVICE_TOKEN = "pushdevice";
	protected String DELETE_DEVICE_TOKEN = "pushdevice";
	String ACCOUNT_ME = "account/me";
	protected String CREATE_USER = "user";
	protected String RETRIEVE_USER = "user";
	protected String UPDATE_USER = "user";
	protected String DELETE_USER = "user";
	protected String CREATE_PHONE_CONFIRM = "pnconfirm";
	protected String UPDATE_PHONE_CONFIRM = "pnconfirm";
	protected String FIND_PASSWORD = "findpassword";

//	public static final String base_url = "http://apitest.mypassbook.co.kr/";
	public static String base_url;// = Constant.SERVER_API + "/";
	
	public static final String PARENT_TYPE_USER = "user";
	public static final String PARENT_TYPE_CONTENT = "content";
	public static final String PARENT_TYPE_PET = "pet";
	public static final String PARENT_TYPE_VENUE = "venue";

	public static final int MSG_LOADING = 100;
	public static final int MSG_LOADING_START = 1;
	public static final int MSG_LOADING_DONE = 2;
	
	public static final int STATUS_CODE_OK = 200;
	public static final int ERROR_EXISTING_EMAIL = 103;
	public static final int ERROR_NETWORK = 800;
	public static final int ERROR_UNKNOWN = 900;
	public static final int ERROR_INVALID_JSON = 901;
	public static final int ERROR_IO_ERROR = 902;
	public static final int ERROR_GENERAL_SERVER_ERROR = 500;

	public static final String COMMENT_TYPE_WEIGHT = "wtraining";
	public static final String COMMENT_TYPE_AEROBICS = "aerobic";
	public static final String COMMENT_TYPE_DIETARY = "dietary";
	public static final String COMMENT_TYPE_CONTENT = "content";

	public static final String LIKE_TYPE_DIETARY = "dietary";
	public static final String LIKE_TYPE_WEIGHT = "wtraining";
	public static final String LIKE_TYPE_AEROBIC = "aerobic";
	public static final String LIKE_TYPE_CONTENT = "content";
	public static final String LIKE_TYPE_USER = "user";

	public static final String ERROR_MSG_INVALID_JSON = "invalid json";
	
	//public static ArrayList<String> cookies = new ArrayList<String>();
	
	protected Context mContext;
	
	private static APIWrapperBase mInstance;
	
	/**
	 * 현재 인스턴스를 가져오기 
	 * @param context
	 * @return 현재 API wrapper instance
	 */
	public static APIWrapperBase getInstance(Context context) {
		if( mInstance == null )
			mInstance = new APIWrapperBase(context);
		
		if( base_url == null )
			throw new AssertionError("base_url must not be null.");


		return mInstance;
	}

	/**
	 * 새로운 인스턴스를 생성 
	 * @param context
	 * @param url 서버 URL (http로 시작하는 서버 주소. 끝에 "/"는 있어도 되고 없어도 무관함)
	 * @return API wrapper instance
	 */
	public static APIWrapperBase createInstance(Context context, String url) {
		if( mInstance != null )
			mInstance = null;
		
		mInstance = new APIWrapperBase(context);
		base_url = url;

		return mInstance;
	}

	/**
	 * 서버의 기본 주소를 지정. 
	 * @param url http로 시작하는 서버 주소. '/'로 끝나도 되고 안끝나도 무관.
	 */
	public static void setServerUrl(String url) {
		base_url = url;
		if( !base_url.endsWith("/") ) {
			base_url += "/";
		}
	}
	
	/**
	 * x-wapple-locale 헤더 지정 
	 * @param locale locale 문자열. 
	 */
	public void setLocale(String locale) {
		HttpLoadTask.locale = locale;
        Log.d("Locale="+locale);
	}
	
	public String version() {
		return VERSION;
	}
	
	public String getISODateString(Date date) {
		String strDate = null;
		
		// 서버에는 GMT 시간으로 던진다 
		TimeZone tz = TimeZone.getDefault();
		Date now = new Date();
		int offsetFromUtc = tz.getOffset(now.getTime()) / 1000;

		// offsetFromUtc만큼 빼서 GMT 생성 
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.SECOND, -offsetFromUtc);
		Date gmtDate = c.getTime();
		
		SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
		strDate = ISO8601DATEFORMAT.format(gmtDate);
		return strDate;
	}

	public APIWrapperBase(Context context) {
		mContext = context;

		Preference.createInstance(context);
		// session id가 있으면 ---------
		String sessionId = Preference.getSessionId();

		android.util.Log.d("TEST", "sessionId="+sessionId);

		if( sessionId != null ) {
			HttpLoadTask.setCookie(sessionId);
		}
		// ---------------------------


	}
	
	protected JSONObject getJSON(Map<?,?> map) {
		Iterator<?> iter = map.entrySet().iterator();
		JSONObject holder = new JSONObject();

		while (iter.hasNext()) {
			Map.Entry<?,?> pairs = (Map.Entry<?,?>) iter.next();
			String key = (String) pairs.getKey();
			
			Object obj = pairs.getValue();
			if( obj instanceof String || obj instanceof Boolean || obj instanceof Double || obj instanceof JSONObject) {
				try {
					holder.put(key, obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Map<?,?> m = (Map<?,?>) pairs.getValue();
				JSONObject data = new JSONObject();
				
				try {
					Iterator<?> iter2 = m.entrySet().iterator();
					while (iter2.hasNext()) {
						Map.Entry<?,?> pairs2 = (Map.Entry<?,?>) iter2.next();
						data.put((String) pairs2.getKey(), pairs2.getValue());
					}
					holder.put(key, data);
				} catch (JSONException e) {
					Log.e("Transforming", "There was an error packaging JSON:"+e);
				}
			}
		}
		
		return holder;
	}
	// see http://androidsnippets.com/transform-a-map-to-a-json-object
	
	/**
	 * 로그인
	 * @param email 사용자의 email
	 * @param password 비밀번호 
	 * @param remember_me 로그인 세션을 계속 기억할지 여부. true 권장. 
	 * @param callback ResponseCallback 객체
	 */
	public void login(String email, String password, boolean remember_me, ResponseCallback callback) {

		HttpLoadTask.clearCookie();

		HttpLoadTask task = new HttpLoadTask(mContext, callback, false, true);

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("strategy", "local");
		map.put("user", email);
		map.put("pw", password);
		map.put("rememberme", remember_me );
		
		
		JSONObject json = new JSONObject(map);
		String jsonString = json.toString();
		
		Log.d("login", jsonString);
		
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + LOGIN, jsonString, HttpLoadTask.METHOD_POST);
		
	}

	/**
	 * facebook 로그인
	 * @param fbToken facebook SDK에서 넘겨받은 토큰값 
	 * @param callback ResponseCallback 객체
	 */
	public void login(String fbToken, ResponseCallback callback) {

		HttpLoadTask.clearCookie();

		HttpLoadTask task = new HttpLoadTask(mContext, callback, false, true);

		HashMap<String, Object> map = new HashMap<String, Object>();
		
		map.put("strategy", "facebook");
		map.put("access_token", fbToken);
		map.put("rememberme", true );
		
		
		JSONObject json = new JSONObject(map);
		String jsonString = json.toString();
		
		Log.d("login", jsonString);
		
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + LOGIN, jsonString, HttpLoadTask.METHOD_POST);
		
	}
	

	/**
	 * 카카오 로그인
	 * @param kakaoToken KAKAO SDK에서 넘겨받은 토큰값 
	 * @param callback ResponseCallback 객체
	 */
	public void loginWithKakao(String kakaoToken, ResponseCallback callback) {

		HttpLoadTask task = new HttpLoadTask(mContext, callback, false, true);

		HashMap<String, Object> map = new HashMap<String, Object>();
		
		map.put("strategy", "kakao");
		map.put("access_token", kakaoToken);
		map.put("rememberme", true );
		
		
		JSONObject json = new JSONObject(map);
		String jsonString = json.toString();
		
		Log.d("login", jsonString);
		
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + LOGIN, jsonString, HttpLoadTask.METHOD_POST);
		
	}

	/**
	 * 로그아웃
	 * @param callback ResponseCallback 객체
	 */
	public void logout(ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback, false, true);
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + LOGOUT);
		
	}

	/**
	 * 페이스북으로 신규가입 
	 * @param fbToken facebook SDK에서 넘겨받은 토큰값 
	 * @param tel 전화번호. 국가코드(예: +82)를 포함한 번호  
	 * @param callback ResponseCallback 객체
	 */
	public void createFacebookUser(/*String email,*/ String fbToken, String tel, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);

		HashMap<String, String> mapAuth = new HashMap<String, String>();
		
//		mapAuth.put("email", email);
		mapAuth.put("fbat", fbToken);
		mapAuth.put("tel", tel);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		map.put("auth", new JSONObject(mapAuth));

		// 전화번호  
//		HashMap<String, String> mapUser = new HashMap<String, String>();
//		mapUser.put("tel", tel);
//		map.put("user", new JSONObject(mapUser));

		
		JSONObject json = new JSONObject(map);
		String jsonString = json.toString();
		
		Log.d("createUser", jsonString);
		
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + CREATE_USER, jsonString, HttpLoadTask.METHOD_POST);
	}
	
	/**
	 * KAKAO ID로 신규가입 
	 * @param kakaoToken KAKAO SDK에서 넘겨받은 토큰값 
	 * @param callback ResponseCallback 객체
	 */
	public void createKakaoUser(String kakaoToken, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);

		HashMap<String, String> mapAuth = new HashMap<String, String>();
		
		mapAuth.put("kkat", kakaoToken);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		map.put("auth", new JSONObject(mapAuth));

		JSONObject json = new JSONObject(map);
		String jsonString = json.toString();
		
		Log.d("createUser", jsonString);
		
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + CREATE_USER, jsonString, HttpLoadTask.METHOD_POST);
	}
	
	/**
	 * 신규가입 
	 * @param email 이메일주소 (아이디로 사용함)
	 * @param password 비밀번호 
	 * @param phoneNumber 전화번호 
	 * @param callback ResponseCallback 객체
	 */
	public void createUser(String email, String password, /*String name,*/ String phoneNumber, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		// auth
		HashMap<String, String> mapAuth = new HashMap<String, String>();
		
		mapAuth.put("email", email);
		mapAuth.put("pw", password);
		mapAuth.put("tel", phoneNumber);
		
		// user
//		HashMap<String, String> mapUser = new HashMap<String, String>();
//		
//		mapUser.put("nick", name);
//		mapUser.put("tel", phoneNumber);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		map.put("auth", new JSONObject(mapAuth));
//		map.put("user", new JSONObject(mapUser));

		JSONObject json = new JSONObject(map);
		String jsonString = json.toString();
		
		Log.d("createUser", jsonString);

		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + CREATE_USER, jsonString, HttpLoadTask.METHOD_POST);			
	}
	
	/**
	 * 내 정보 가져오기  
	 * @param callback ResponseCallback 객체
	 */
	public void retrieveUserMe(ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + RETRIEVE_USER + "/me" );		
	}

	/**
	 * email로 사용자 정보 가져오기  
	 * @param email 정보를 가져오려는 사용자의 이메일주소
	 * @param callback ResponseCallback 객체
	 */
	public void retrieveUserWithEmail(String email, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + RETRIEVE_USER + "/?email=" + email );		
	}

	/**
	 * id로 사용자 정보 가져오기  
	 * @param id MongoDB ID값. 사용자 로그인 아이디가 아님.
	 * @param callback ResponseCallback 객체
	 */
	public void retrieveUserWithId(String id, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + RETRIEVE_USER + "/" + id );		
	}

	/**
	 * facebook auth token으로 사용자 정보 가져오기  
	 * @param fbat facebook authentication token
	 * @param callback ResponseCallback 객체
	 */
	public void retrieveUserWithFacebook(String fbat, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + RETRIEVE_USER + "/?fbat=" + fbat );		
	}

	/**
	 * 전화번호로 사용자 정보 가져오기  
	 * @param phone 전화번호. 국가코드(예: +82)가 포함된 번호로 전송할 것.
	 * @param callback ResponseCallback 객체
	 */
	public void retrieveUserWithPhone(String phone, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + RETRIEVE_USER + "/?tel=" + phone );		
	}
	
	/**
	 * GCM Registration ID를 서버로 전송  
	 * @param token GCM Registration ID (device id)
	 * @param callback ResponseCallback 객체
	 */
	public void createDevice( String token, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
		
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + CREATE_DEVICE_TOKEN + "/android/" + token, "", HttpLoadTask.METHOD_POST);
	}
	
	/**
	 * GCM Registration ID를 서버에서 삭제  
	 * @param token GCM Registration ID (device id)
	 * @param callback ResponseCallback 객체
	 */
	public void deleteDevice( String token, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
		
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + CREATE_DEVICE_TOKEN + "/android/" + token, "", HttpLoadTask.METHOD_DELETE);
	}
	
	/**
	 * 문자로 인증번호 받기 
	 * @param phoneNumber 전화번호. 국가코드(예: +82)가 포함된 번호로 전송할 것.
	 * @param callback ResponseCallback 객체
	 */
	public void createPhoneConfirm(String phoneNumber, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + CREATE_PHONE_CONFIRM + "/subs?tel=" + phoneNumber, "", HttpLoadTask.METHOD_POST);			
	}
	
	/**
	 * 문자로 받은 인증번호로 인증하기 
	 * @param phoneNumber 전화번호. 국가코드(예: +82)가 포함된 번호로 전송할 것.
	 * @param confirmNumber 문자로 받은 인증번호
	 * @param callback ResponseCallback 객체
	 */
	public void updatePhoneConfirm(String phoneNumber, String confirmNumber, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("cnf", confirmNumber);
		
		JSONObject json = new JSONObject(map);
		String jsonString = json.toString();
		
		Log.d("updatePhoneConfirm", jsonString);

		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + UPDATE_PHONE_CONFIRM + "/subs?tel=" + phoneNumber, jsonString, HttpLoadTask.METHOD_PUT);		
	}
	
	// 
	// Update user
	// 
	
	/**
	 * 사용자 프로필 이미지 및 이름,생일 변경 
	 * @param profileImage 프로필 이미지 경로 
	 * @param nick 별명
	 * @param dob Date 형식의 생일 
	 * @param gender 남자라면 "MALE", 여자라면 "FEMALE"
	 * @param skinType A:잘탐 B:보통 C:안탐
	 * @param callback ResponseCallback 객체
	 */
	public void updateUser(String profileImage, String nick, Date dob, String gender, String skinType, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		HashMap<String, String> mapFile = new HashMap<String, String>();
		if( profileImage != null ) {
			mapFile.put("profile", profileImage);
		} 

		HashMap<String, Object> mapD = new HashMap<String, Object>();
        if( nick != null )
    		mapD.put("nick", nick);
		
        if( dob != null ) {
            Calendar c = Calendar.getInstance();
            c.setTime(dob);
            HashMap<String, Object> mapDob = new HashMap<String, Object>();

            mapDob.put("full", getISODateString(dob));
            mapDob.put("year", c.get(Calendar.YEAR));
            mapDob.put("month", c.get(Calendar.MONTH)+1);
            mapDob.put("day", c.get(Calendar.DAY_OF_MONTH));

            mapD.put("dob", new JSONObject(mapDob));
        }

        if( gender != null )
    		mapD.put("gender", gender);

		if( skinType != null ) {
			mapD.put("skin_type", skinType);
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("d", new JSONObject(mapD).toString());
		
		Log.d("updateUser file:" + profileImage);

		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + UPDATE_USER, map, HttpLoadTask.METHOD_PUT_FILE, mapFile);			
	}

	/**
	 * 비밀번호 변경  
	 * @param oldPassword 이전 비밀번호
	 * @param newPassword 새 비밀번호
	 * @param callback ResponseCallback 객체
	 */
	public void updateUserPassword(String oldPassword, String newPassword, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("old_password", oldPassword);
		map.put("password", newPassword);
		JSONObject json = new JSONObject(map);
		String jsonString = json.toString();
		
		Log.d("updateUserPassword:" + json);

		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + UPDATE_USER + "/password", jsonString, HttpLoadTask.METHOD_PUT);			
	}

	/**
	 * 이메일 주소 변경  
	 * @param email 새 이메일 주소 
	 * @param callback ResponseCallback 객체
	 */	
	public void updateUserEmail(String email, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("email", email);

		JSONObject json = new JSONObject(map);
		String jsonString = json.toString();
		
		Log.d("updateUserEmail:" + json);

		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + UPDATE_USER + "/email", jsonString, HttpLoadTask.METHOD_PUT);			
	}

	/**
	 * 전화번호 변경  
	 * @param tel 전화번호. 국가코드(예: +82)가 포함된 번호로 전송할 것.
	 * @param callback ResponseCallback 객체
	 */	
	public void updateUserPhone(String tel, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("tel", tel);

		JSONObject json = new JSONObject(map);
		String jsonString = json.toString();
		
		Log.d("updateUserPhone:" + json);

		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + UPDATE_USER + "/tel", jsonString, HttpLoadTask.METHOD_PUT);			
	}
	
	/**
	 * 프로필 이미지를 삭제
	 * @param callback ResponseCallback 객체
	 */	
	public void deleteUserProfileImage(ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		Log.d("deleteUserProfileImage");

		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + UPDATE_USER + "/delprofile", "", HttpLoadTask.METHOD_PUT);			
	}
	
	/**
	 * 유저를 삭제 (회원탈퇴)
	 * @param callback ResponseCallback 객체
	 */	
	public void deleteUser(ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		Log.d("deleteUser");

		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + DELETE_USER, "", HttpLoadTask.METHOD_DELETE);			
	}
	
	/**
	 * 비밀번호 찾기를 실행
	 * @param email 회원가입시 사용한 email
	 * @param callback ResponseCallback 객체
	 */	
	public void findPassword(String email, ResponseCallback callback) {
		HttpLoadTask task = new HttpLoadTask(mContext, callback);
				
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, base_url + FIND_PASSWORD + "?email="+email );
	}


}

