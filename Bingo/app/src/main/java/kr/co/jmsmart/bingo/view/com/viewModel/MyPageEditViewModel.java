package kr.co.jmsmart.bingo.view.com.viewModel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableField;
import android.widget.Toast;

import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.data.User;
import kr.co.jmsmart.bingo.databinding.ActivityMyPageEditBinding;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.com.MyPageActivity;

/**
 * Created by Administrator on 2019-01-07.
 */

public class MyPageEditViewModel implements CommonModel {
    private Context context;
    public User user;
    private ActivityMyPageEditBinding binding;
    private String TAG = "mypageViewModel";

    public ObservableField<Boolean> isModify = new ObservableField<>(true);
    public ObservableField<String> applyOrModify = new ObservableField<>("");

    public MyPageEditViewModel(Context context , ActivityMyPageEditBinding binding, User user){
        this.context=context;
        this.user = user;
        this.binding = binding;
        applyOrModify.set(context.getResources().getString(R.string.mypage_apply));
    }
    @Override
    public void onCreate() {
        if(user.sex.equals("M")){
            binding.mypageRadioMale.setChecked(true);
            binding.mypageRadioFemale.setChecked(false);
        }else{
            binding.mypageRadioMale.setChecked(false);
            binding.mypageRadioFemale.setChecked(true);
        }
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

    public void onClick(){
        //API나오면 구현들어가야해
        if(!isValidDateStr(user.birth)) {
            Toast.makeText(context,context.getString(R.string.info_input_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if(binding.mypageRadioMale.isChecked())user.sex = "M";
        else user.sex = "F";
        APIManager.getInstance(context).updateUser(user.userId, user.name, user.phone, user.sex, user.birth, new ResponseCallback() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,context.getString(R.string.profile_change_error),Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onDataReceived(JSONObject jsonResponse) {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,context.getString(R.string.profile_change_text),Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                });

                //applyOrModify.set(context.getResources().getString(R.string.myPage_Modify));
                //isModify.set(false);
            }

            @Override
            public void onReceiveResponse() {

            }
        });
    }

    public void onBackPressed(){
        Intent intent = new Intent(context, MyPageActivity.class);
        intent.putExtra("userId",user.userId);
        context.startActivity(intent);
        ((Activity)context).finish();
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
