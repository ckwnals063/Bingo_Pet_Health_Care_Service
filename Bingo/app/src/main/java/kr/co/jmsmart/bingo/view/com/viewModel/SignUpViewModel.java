package kr.co.jmsmart.bingo.view.com.viewModel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.ObservableField;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.com.PetActivity;
import kr.co.jmsmart.bingo.view.com.SettingActivity;
import kr.co.jmsmart.bingo.view.com.TextActivity;

/**
 * Created by Administrator on 2019-01-04.
 */

public class SignUpViewModel implements CommonModel {

    private Context context = null;

    private int nowCur = 0;

    private String TAG = "SignUpViewModel";

    public final ObservableField<Integer> idVisible = new ObservableField<>(View.VISIBLE);
    public final ObservableField<Integer> detailVisible = new ObservableField<>(View.GONE);
    public final ObservableField<Integer> puppyVisible = new ObservableField<>(View.GONE);

    public final ObservableField<Boolean> nextBtnEnabled = new ObservableField<>(true);
    public final ObservableField<Boolean> beforeBtnEnabled = new ObservableField<>(false);

    public final ObservableField<String> errorTxt = new ObservableField<>("");
    public final ObservableField<String> userIdTxt = new ObservableField<>("");
    public final ObservableField<String> userPassTxt = new ObservableField<>("");
    public final ObservableField<String> userPassCheckTxt = new ObservableField<>("");
    public final ObservableField<String> userPhoneTxt = new ObservableField<>("");
    public final ObservableField<String> userNameTxt = new ObservableField<>("");
    public final ObservableField<String> userBirthdayTxt = new ObservableField<>("");

    private String userType = "";
    ArrayList<APIManager.UserGroup> userGroups;

    RadioGroup userSex;

    private ProgressDialog progressDialog;

    public SignUpViewModel(Context context){
        this.context = context;
    }

    @Override
    public void onCreate() {
        Spinner userTypeSpinner = ((Activity)context).findViewById(R.id.spinner_member_type);

        userSex = ((Activity) context).findViewById(R.id.radio_user);

        userTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userType = userGroups.get(position).getGroupCd();
                Log.d(TAG, "userType selected : " + userType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        userGroups = APIManager.getInstance(context).getUserGroupList();
        userTypeSpinner.setAdapter(new ArrayAdapter<String>(context,R.layout.support_simple_spinner_dropdown_item, getUserGroupList(userGroups)));

        Intent intent = new Intent(context, TextActivity.class);
        intent.putExtra("title", context.getString(R.string.privacy_policy));
        context.startActivity(intent);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }
    public void onBeforeClick(){
        switch (nowCur){
            case 1 : {
                detailVisible.set(View.GONE);
                idVisible.set(View.VISIBLE);
                beforeBtnEnabled.set(false);



                nowCur = 0;
                break;
            }
        }
    }
    public void onNextClick(){
        userIdTxt.set(userIdTxt.get().trim());
        switch (nowCur){
            case 0 : {
                if(userIdTxt.get().length() < 1 || !checkEmail(userIdTxt.get())){
                    Toast.makeText(context,context.getString(R.string.id_input_error),Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(userPassTxt.get().length() < 1 || !userPassTxt.get().equals(userPassCheckTxt.get())){
                    Toast.makeText(context,context.getString(R.string.pw_input_error),Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "UserPass Length : " + userPassTxt.get().length());
                    Log.d(TAG, "UserPass TextActivity : " + userPassTxt.get());
                    Log.d(TAG, "UserPassCheck : " + userPassCheckTxt.get());
                    break;
                }

                progressDialog = new ProgressDialog(context);
                progressDialog.onStart();
                APIManager.getInstance(context).userExistCheck(userIdTxt.get(), new ResponseCallback() {
                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        progressDialog.cancel();
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,context.getString(R.string.registerd_user_error),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onDataReceived(JSONObject jsonResponse) {

                        progressDialog.cancel();

                        idVisible.set(View.GONE);
                        detailVisible.set(View.VISIBLE);
                        beforeBtnEnabled.set(true);

                        nowCur = 1;
                    }

                    @Override
                    public void onReceiveResponse() {

                    }
                });

                break;
            }
            case 1 :{
                if(userNameTxt.get().length() < 1 || userBirthdayTxt.get().length() < 8 || userPhoneTxt.get().length() < 10 || !isValidDateStr(userBirthdayTxt.get())){
                    Toast.makeText(context,context.getString(R.string.info_input_error),Toast.LENGTH_SHORT).show();
                    break;
                }

                progressDialog = new ProgressDialog(context);
                progressDialog.onStart();
                APIManager.getInstance(context).newAccount(userIdTxt.get()
                        , userType
                        , userNameTxt.get()
                        , userPassTxt.get()
                        , userPhoneTxt.get()
                        , userSex.getCheckedRadioButtonId() == R.id.radio_male ? 'M' : 'F'
                        , userBirthdayTxt.get(), new ResponseCallback() {
                            @Override
                            public void onError(int errorCode, String errorMsg) {
                                progressDialog.cancel();
                                Toast.makeText(context, R.string.fail_signup,Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onDataReceived(JSONObject jsonResponse) {
                                progressDialog.cancel();
                                ((Activity)context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, context.getString(R.string.sign_in_success), Toast.LENGTH_LONG).show();
                                    }
                                });

                                ((Activity)context).finish();
                            }

                            @Override
                            public void onReceiveResponse() {

                            }
                        });
                break;


            }
        }
    }

    public ArrayList<String> getUserGroupList(ArrayList<APIManager.UserGroup> list){
        //U001 U002 U003
        int[] transrateIds = {R.string.user_type1, R.string.user_type2, R.string.user_type3};
        ArrayList<String> result = new ArrayList<>();

        for(APIManager.UserGroup user : list){
            int index = Integer.parseInt(user.getGroupCd().replace("U", ""))-1;
            result.add(context.getString(transrateIds[index]));
        }
        return result;
    }

    public static boolean checkEmail(String email){

        String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        boolean isNormal = m.matches();
        return isNormal;

    }

    public static boolean isValidDateStr(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setLenient(false);
            sdf.parse(date);
        }
        catch (ParseException e) {
            return false;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
