package kr.co.jmsmart.bingo.view.com.viewModel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableField;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.wapplecloud.libwapple.APIWrapperBase;
import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivityLoginBinding;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.util.SharedPreferencesUtil;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.com.FindPasswordActivity;
import kr.co.jmsmart.bingo.view.com.HomeActivity;
import kr.co.jmsmart.bingo.view.com.SignUpActivity;


/**
* ================================================
* MainViewModel.java
* @작성자       : kyw
* @작성일       : 2018-12-13
* @클래스 설명  : 메인 뷰 모델
* ================================================
*/

public class LoginViewModel implements CommonModel {

    private Context context = null;
    private ActivityLoginBinding binding;

    private String TAG = "LoginViewModel";


    private ProgressDialog progressDialog;

    //텍스트
    public final ObservableField<String> inputEmailHint = new ObservableField<>("");
    public final ObservableField<String> inputPasswordHint = new ObservableField<>("");
    public final ObservableField<String> btnLoginText = new ObservableField<>("");

    public final ObservableField<String> inputEmail = new ObservableField<>("");
    public final ObservableField<String> inputPassword = new ObservableField<>("");

    public final ObservableField<String> btnSignInText = new ObservableField<>("");

    private boolean isAutoOn = false;

    public LoginViewModel(Context context, ActivityLoginBinding binding){
        this.context = context;
        this.binding = binding;
    }

    @Override
    public void onCreate() {


        inputEmailHint.set(context.getResources().getString(R.string.prompt_email));
        inputPasswordHint.set(context.getResources().getString(R.string.password));
        btnLoginText.set(context.getResources().getString(R.string.sign_in));
        btnSignInText.set(context.getResources().getString(R.string.sign_up));

        binding.edPw.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                click();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        autoLogin();
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {

    }
    public void click(){
        inputEmail.set(inputEmail.get().trim());
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.edPw.getWindowToken(), 0);

        ((Activity)context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if(inputEmail.get().isEmpty() ||  inputPassword.get().isEmpty()) showSnack(R.string.empty_mail_pw);
        else if(!checkEmail(inputEmail.get())) showSnack(R.string.check_mail);
        else {
            progressDialog = new ProgressDialog(context);
            progressDialog.show();
            APIManager.getInstance(context).login(inputEmail.get(), inputPassword.get(), new ResponseCallback() {
                @Override
                public void onError(int errorCode, String errorMsg) {
                    Log.d(TAG, "onErrorCode: " + errorCode);
                    progressDialog.cancel();
                    if(errorCode == APIWrapperBase.ERROR_NETWORK)
                        showSnack(R.string.not_internet_connect);
                    else showSnack(R.string.fail_login);
                }

                @Override
                public void onDataReceived(JSONObject jsonResponse) {
                    if(binding.cbAuto.isChecked())
                        SharedPreferencesUtil.setLoginInfo(context, inputEmail.get(), inputPassword.get());

                    Log.i(TAG, jsonResponse.toString());
                    progressDialog.cancel();
                    Intent intent = new Intent(context, HomeActivity.class);
                    intent.putExtra("userId", inputEmail.get());
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }

                @Override
                public void onReceiveResponse() {
                    Log.d(TAG, "onReceiveResponse: ");
                }
            });
        }
    }
    public void onSignInClick(){
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }
    public void onFindPwClick(){
        Intent intent = new Intent(context, FindPasswordActivity.class);
        context.startActivity(intent);
    }
    public static boolean checkEmail(String email){
        String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        boolean isNormal = m.matches();
        return isNormal;
    }
    public void showSnack(int stringId){
        Snackbar.make(((Activity)context).getWindow().getDecorView().getRootView(), stringId, Snackbar.LENGTH_SHORT).show();
    }

    public void autoLogin(){
        String[] info = SharedPreferencesUtil.getLoginInfo(context);
        if(info!=null){
            inputEmail.set(info[0]);
            inputPassword.set(info[1]);
            click();
        }
    }







}
