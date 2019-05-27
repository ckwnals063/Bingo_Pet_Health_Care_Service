package kr.co.jmsmart.bingo.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.wapplecloud.libwapple.APIWrapperBase;
import com.wapplecloud.libwapple.BuildConfig;
import com.wapplecloud.libwapple.HttpLoadTask;
import com.wapplecloud.libwapple.Log;
import com.wapplecloud.libwapple.Preference;
import com.wapplecloud.libwapple.ResponseCallback;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * Created by ZZQYU on 2019-01-06.
 */

public class MyHttpLoadTask extends HttpLoadTask {
    private static Stack<HttpClient> clientStack = new Stack<HttpClient>(); // 현재 요청중인 request들을 한꺼번에 cancel할 수 있게
    private static String userAgent;
    private static HashMap<String, String> cookies = new HashMap<String, String>();

    private Context mContext;
    private ResponseCallback mCallback;
    private boolean runOnUiThread = false; // 받은 응답 처리를 어느 쓰레드에서 처리할건지

    private boolean saveSession = false; // 평소에는 받는 세션 저장안하고 이 값이 true일 때에만(로그인/로그아웃) 저장


    public MyHttpLoadTask(Context context) {
        super(context, null);
        this.mContext = context;
    }
    public MyHttpLoadTask(Context context, ResponseCallback callback) {
        super(context, callback);
        this.mContext = context;
        this.mCallback = callback;
    }

    public MyHttpLoadTask(Context context, ResponseCallback callback, boolean runOnUiThread, boolean saveSession) {
        super(context, callback, runOnUiThread, saveSession);
        this.mContext = context;
        this.mCallback = callback;
        this.runOnUiThread = runOnUiThread;
    }

    public MyHttpLoadTask(Context context, ResponseCallback callback, boolean runOnUiThread) {
        super(context, callback, runOnUiThread);
        this.mContext = context;
        this.mCallback = callback;
        this.runOnUiThread = runOnUiThread;
    }



    @Override
    protected Void doInBackground(Object... params) {
        String url = (String)params[0];
        url.replaceAll("(\r\n|\r|\n|\n\r)", " ");
        url.replaceAll(" ","%20");

        String jsonString;
        String method;

        if( params.length >= 2 )
            method = (String)params[2];
        else
            method = METHOD_GET;

        Log.d("URL", url);
        Log.d("method", method);

        // set timeout options
        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        // The default value is zero, that means the timeout is not used.
        int timeoutConnection = 15000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT)
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = (BuildConfig.DEBUG ? 15000 : 30000);
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        HttpClient client = new DefaultHttpClient(httpParameters);
        clientStack.add(client);

        HttpRequestBase req = null;

        try {
            if( method.equals(METHOD_POST) ) {
                req = new HttpPost(url);
                req.addHeader("Content-Type", "application/json");

                HttpPost post = (HttpPost)req;

                jsonString = (String)params[1];
                StringEntity entity = new StringEntity(jsonString, HTTP.UTF_8);
                post.setEntity(entity);

            } else if( method.equals(METHOD_GET)) {
                if( params.length > 1 )
                    req = new HttpGet(url + "?" + params[1]);
                else
                    req = new HttpGet(url);

            } else if( method.equals(METHOD_DELETE)) {
                req = new HttpDelete(url);
            } else if( method.equals(METHOD_PUT)) {
                req = new HttpPut(url);
                req.addHeader("Content-Type", "application/json");
                jsonString = (String)params[1];
                StringEntity entity = new StringEntity(jsonString, HTTP.UTF_8);
                HttpPut put = (HttpPut)req;
                put.setEntity(entity);
            }
            else if( method.equals(METHOD_POST_FILE) || method.equals(METHOD_PUT_FILE) ) {
                /*
                File f = SaveBitmapToFileCache(bitmap, "test.jpg");

                try {
                    FileBody bin = new FileBody(f);
                    StringBody id = new StringBody("3");
                    MultipartEntity reqEntity = new MultipartEntity();
                    reqEntity.addPart("files[0]", bin);
                    reqEntity.addPart("userId", new StringBody("ex1@naver.com"));
                    //reqEntity.addPart("image_title", new StringBody("CoolPic"));

                    httppost.setEntity(reqEntity);
                    System.out.println("Requesting : " + httppost.getRequestLine());
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    String responseBody = httpclient.execute(httppost, responseHandler);
                    System.out.println("responseBody : " + responseBody);

                } catch (Exception e) {
                    Log.i(TAG, "[upload2] " + Log.getStackTraceString(e));
                } finally {
                    httpclient.getConnectionManager().shutdown();
                }
                */
                HttpRequestBase post;
                if( method.equals(METHOD_PUT_FILE) ) {
                    req = new HttpPut(url);
                    post = (HttpPut)req;
                } else {
                    req = new HttpPost(url);
                    post = (HttpPost)req;
                }
                @SuppressWarnings("unchecked")
                HashMap<String, String> map = (HashMap<String, String>)params[1];
                MultipartEntity multipartEntity = new MultipartEntity();
                //req.addHeader("Content-Type", "multipart/form-data");

                for( String key : map.keySet() ) {
                    String value = map.get(key);
                    //multipartEntity.addPart(key, new StringBody(value));
                    multipartEntity.addPart(key, new StringBody(value, "text/json", Charset.forName("UTF-8")));
                }

                // file이 있으면
                if( params.length > 3 ) {
                    @SuppressWarnings("unchecked")
                    HashMap<String, Bitmap> mapFile = (HashMap<String, Bitmap>)params[3];
                    // imgs0, imgs1, ... 이 순서대로 처리
                    Set<String> keySet = mapFile.keySet();
                    SortedSet<String> sortedSet = new TreeSet<String>();
                    sortedSet.addAll(keySet);
                    for( String key : sortedSet ) {
                        Bitmap value = mapFile.get(key);
                        if( value != null ) {

                            File file = SaveBitmapToFileCache(value, "cache.jpg");

                            Log.d("FILE="+key+":"+file);
                            // 이미지를 여러개 보내는 경우 map에 같은 "imgs" 키가 있을 수 없어서 imgs0, imgs1, ... 이렇게 오면 imgs로 아니면 key값으로 하자
                            if( key.startsWith("imgs"))
                                multipartEntity.addPart("imgs", new FileBody(file));
                            else
                                multipartEntity.addPart(key, new FileBody(file));
                        }
                    }
                }

                if( method.equals(METHOD_PUT_FILE) ) {
                    ((HttpPut) post).setEntity((HttpEntity) multipartEntity);
                } else {
                    ((HttpPost) post).setEntity((HttpEntity) multipartEntity);
                }
//

            }

            if( req != null ) {
                if( userAgent != null )
                    req.addHeader("User-Agent", userAgent);

                // time zone 추가
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                        Locale.getDefault());
                Date currentLocalTime = calendar.getTime();
                SimpleDateFormat date = new SimpleDateFormat("Z");
                String localTime = date.format(currentLocalTime);
                req.addHeader("x-wapple-timezone", localTime);

                Log.d("timezone:"+localTime);

                // locale
                if( locale != null ) {
                    req.addHeader("x-wapple-locale", locale);
                }

                // add cookie
                if( cookies != null && cookies.size() > 0) {
                    String cookie_string = "";
                    for( String key : cookies.keySet() ) {
                        cookie_string += key + "=" + cookies.get(key) + ";";
                    }
                    req.addHeader("Cookie", cookie_string);
                    req.setHeader("Session", cookie_string);
                    Log.d("Set cookie:"+cookie_string);
                }

                Log.i("---------------- execute request");
                // 언어별로 다른 응답 받기 위함. ko-KR이 표준 포맷임 http://en.wikipedia.org/wiki/IETF_language_tag
                String locale = Locale.getDefault().toString().replace("_", "-");
                req.addHeader("Accept-Language", locale);


                if(req.getMethod()==METHOD_PUT) {
                    InputStream in = ((HttpPut) req).getEntity().getContent();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String read;

                    while ((read = br.readLine()) != null) {
                        //System.out.println(read);
                        sb.append(read);
                    }

                    br.close();
                    Log.i("!!!!!!!!", sb.toString());
                }


                HttpResponse response = client.execute(req);
                HttpEntity responseEntity = response.getEntity();

                Header[] headers = response.getAllHeaders();
                for( Header header : headers ) {
                    Log.d("Header", header.toString());
                }

                Header[] header_cookies = response.getHeaders("set-cookie");

                if( header_cookies.length > 0 /*&& cookies.size() == 0*/
                        && saveSession ) {
                    for( Header header_cookie : header_cookies) {
                        String cookie = header_cookie.getValue();
                        setCookie(cookie);
                    }
                }

                // preference에 저장
                Preference.createInstance(mContext);
                Preference.saveSessionId();


                // common response handler
                if( mCallback != null && mContext != null ) {
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onReceiveResponse();
                        }});
                }

                Header headerContentType = responseEntity.getContentType();
                if( headerContentType != null && headerContentType.getValue().contains("image")) {
                    // binary image
                    if( mCallback != null && mContext != null ) {
                        try {
                            final File tempFile = File.createTempFile("mmimg", null);
                            InputStream is = responseEntity.getContent();
                            OutputStream os = new FileOutputStream(tempFile);

                            final byte[] buffer = new byte[1024];
                            int read;
                            while( (read=is.read(buffer)) != -1 ) {
                                os.write(buffer, 0, read);
                            }

                            os.flush();
                            os.close();
                            is.close();
                            ((Activity)mContext).runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    mCallback.onBinaryDataReceived(tempFile.getAbsolutePath());
                                }});
                        } catch(Exception e) {
                            e.printStackTrace();
                            final String errorMsg = e.getLocalizedMessage();
                            ((Activity)mContext).runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    mCallback.onError(APIWrapperBase.ERROR_IO_ERROR, errorMsg);
                                }});
                        }

                    }
//					clientStack.remove(client);
                }
                else {
                    final String responseBody = EntityUtils.toString(responseEntity, "UTF-8");
                    Log.d("Response", responseBody);

                    if( mCallback != null ) {
                        try {
                            final JSONObject jsonResponse = new JSONObject(responseBody);
                            final JSONObject result = jsonResponse;

                            if( runOnUiThread ) {
                                ((Activity)mContext).runOnUiThread( new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if( result != null && result.has("errorCode") && result.getInt("errorCode") != APIWrapperBase.STATUS_CODE_OK ) {
                                                mCallback.onError(result.getInt("errorCode"), result.getString("status"));
                                            } else {
                                                mCallback.onDataReceived(jsonResponse);
                                            }
                                        } catch (JSONException e) {
                                            mCallback.onError(APIWrapperBase.ERROR_INVALID_JSON, APIWrapperBase.ERROR_MSG_INVALID_JSON);
                                        }
                                    }});
                            } else {
                                try {
                                    if( result != null && result.has("errorCode") && result.getInt("errorCode") != APIWrapperBase.STATUS_CODE_OK ) {
                                        try {
                                            mCallback.onError(result.getInt("errorCode"), result.getString("status"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        mCallback.onDataReceived(jsonResponse);
                                    }
                                } catch (JSONException e) {
                                    mCallback.onError(APIWrapperBase.ERROR_INVALID_JSON, APIWrapperBase.ERROR_MSG_INVALID_JSON);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Activity)mContext).runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    mCallback.onError(APIWrapperBase.ERROR_GENERAL_SERVER_ERROR, responseBody);
                                }
                            });
                        }

//						clientStack.remove(client);
                    }
                }

            }


        }
        catch (SocketException e) {
            // 요청을 여러개 동시에 보내서 이전 요청을 취소하면 이리로 옴
        }
        catch( IOException e ) {
            e.printStackTrace();
            if( ignoreError ) {
                return null;
            }
            ((Activity)mContext).runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    if( mCallback != null)
                        mCallback.onReceiveResponse();
                    mCallback.onError(APIWrapperBase.ERROR_NETWORK, "Network error");
                }});
        } catch (final Exception e) {
            e.printStackTrace();
            ((Activity)mContext).runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    if( mCallback != null)
                        mCallback.onReceiveResponse();
                    mCallback.onError(APIWrapperBase.ERROR_UNKNOWN, e.getLocalizedMessage());
                }});
        }


        return null;
    }
    public File SaveBitmapToFileCache(Bitmap bitmap, String filename) {
        File file = mContext.getCacheDir();

        // If no folders
        if (!file.exists()) {
            file.mkdirs();
            // Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }

        File fileCacheItem = new File(file.getAbsolutePath() +"/"+ filename);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileCacheItem;
    }
}