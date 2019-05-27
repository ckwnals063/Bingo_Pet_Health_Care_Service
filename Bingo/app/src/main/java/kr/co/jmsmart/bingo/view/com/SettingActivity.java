package kr.co.jmsmart.bingo.view.com;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONObject;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.data.User;
import kr.co.jmsmart.bingo.data.UserDevice;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.util.SharedPreferencesUtil;

/**
 * Created by Administrator on 2019-01-13.
 */

public class SettingActivity extends PreferenceActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingActivity.MyPreferenceSettingFragment f = new SettingActivity.MyPreferenceSettingFragment();
        Bundle b = new Bundle();
        String userId = getIntent().getStringExtra("userId");
        b.putString("userId", userId);
        f.setArguments(b);
        getFragmentManager().beginTransaction().replace(android.R.id.content,f).commit();
    }

    public static class MyPreferenceSettingFragment extends PreferenceFragment
    {
        private static String TAG = "MyPreferenceFragment";
        private static String userId;
        private static User user;
        private String pw;
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_setting);

            userId = getArguments().getString("userId");


            final SwitchPreference prefAutoLogin= (SwitchPreference)findPreference("pref_s_auto_login");
            if(SharedPreferencesUtil.isExistLoginInfo(this.getContext())){
                prefAutoLogin.setDefaultValue(true);
                pw = SharedPreferencesUtil.getLoginInfo(this.getContext())[1];
            }
            else prefAutoLogin.setEnabled(false);

            final ListPreference prefUnit = (ListPreference)findPreference("pref_s_unit");
            prefUnit.setDefaultValue(SharedPreferencesUtil.getDefaultUnit(getContext()));
            prefUnit.setSummary(SharedPreferencesUtil.getDefaultUnit(getContext()));
            prefUnit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    ((ListPreference)preference).setSummary((String)o);
                    SharedPreferencesUtil.setDefaultUnit(MyPreferenceSettingFragment.this.getContext(), (String)o);
                    return true;
                }
            });

            prefAutoLogin.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean isOn = (Boolean) o;
                    Context context = MyPreferenceSettingFragment.this.getContext();
                    if (!isOn) {
                        SharedPreferencesUtil.removeLoginInfo(context);
                    }
                    else {
                        if(pw!=null){
                            SharedPreferencesUtil.setLoginInfo(context, userId, pw);
                        }
                        else {
                            Toast.makeText(context, "Please set auto login when you login this app.", Toast.LENGTH_LONG).show();
                        }
                    }
                    return true;
                }
            });


            final PreferenceScreen prefNameMail = (PreferenceScreen)findPreference("pref_s_name_mail");
            final PreferenceScreen prefChangePw = (PreferenceScreen)findPreference("pref_s_change_pw");
            final PreferenceScreen prefRemove = (PreferenceScreen)findPreference("pref_s_remove");
            final PreferenceScreen prefPolicy = (PreferenceScreen)findPreference("pref_s_policy");
            final PreferenceScreen prefNotice = (PreferenceScreen)findPreference("pref_s_notice");
            final PreferenceScreen prefTerms = (PreferenceScreen)findPreference("pref_s_terms");
            final PreferenceScreen prefInquiry = (PreferenceScreen)findPreference("pref_s_inquiry");
            final Preference.OnPreferenceClickListener listener = new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean ans = true;
                    switch (preference.getKey()){
                        case "pref_s_name_mail":{
                            Intent intent = new Intent(MyPreferenceSettingFragment.this.getContext(), MyPageActivity.class);
                            intent.putExtra("userId", userId);
                            intent.putExtra("flag", "setting");
                            startActivity(intent);
                            break;
                        }case "pref_s_change_pw":{
                            final ProgressDialog dialog = new ProgressDialog(getContext());
                            dialog.show();
                            APIManager.getInstance(getActivity()).findPassword(userId, new ResponseCallback() {
                                @Override
                                public void onError(int errorCode, String errorMsg) {
                                    dialog.cancel();
                                    Toast.makeText(getContext(),getString(R.string.find_password_error),Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onDataReceived(JSONObject jsonResponse) {
                                    dialog.cancel();
                                    Toast.makeText(getContext(),getString(R.string.find_password_ok),Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onReceiveResponse() {

                                }
                            });
                            break;
                        }
                        case "pref_s_remove":{
                            removeAccount();
                            break;
                        }
                        case "pref_s_notice":{
                            break;
                        }
                        case "pref_s_policy":{
                            Intent intent = new Intent(MyPreferenceSettingFragment.this.getContext(), TextActivity.class);
                            intent.putExtra("title", getString(R.string.privacy_policy));
                            startActivity(intent);
                            break;
                        }
                        case "pref_s_terms":{
                            Intent intent = new Intent(MyPreferenceSettingFragment.this.getContext(), TextActivity.class);
                            intent.putExtra("title", getString(R.string.terms_conditions));
                            startActivity(intent);
                            break;
                        }
                        case "pref_s_inquiry":{
                            break;
                        }
                        default: ans = false;
                    }
                    return ans;
                }
            };

            APIManager.getInstance(this.getContext()).selectNowAccount(userId, new ResponseCallback() {
                @Override
                public void onError(int errorCode, String errorMsg) {
                    Toast.makeText(MyPreferenceSettingFragment.this.getContext(),R.string.fail,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDataReceived(JSONObject jsonResponse) {
                    Log.d(TAG, "onDataReceived: " + jsonResponse.toString());
                    user = new User(jsonResponse.optJSONObject("userInfo"), userId);
                    MyPreferenceSettingFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            prefNameMail.setTitle(user.name);
                            prefNameMail.setSummary(user.userId);
                            prefNameMail.setOnPreferenceClickListener(listener);
                            prefChangePw.setOnPreferenceClickListener(listener);
                            prefRemove.setOnPreferenceClickListener(listener);
                        }
                    });
                }

                @Override
                public void onReceiveResponse() {

                }
            });

            prefPolicy.setOnPreferenceClickListener(listener);
            prefNotice.setOnPreferenceClickListener(listener);
            prefTerms.setOnPreferenceClickListener(listener);
            prefInquiry.setOnPreferenceClickListener(listener);

            PreferenceManager.setDefaultValues(getContext(), R.xml.pref_setting, false);
        }
        public void removeAccount(){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.remove_account);
            builder.setMessage(R.string.want_leave_account);
            builder.setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            final Activity activity = MyPreferenceSettingFragment.this.getActivity();
                            APIManager.getInstance(MyPreferenceSettingFragment.this.getContext()).deleteUser(userId, new ResponseCallback() {
                                @Override
                                public void onError(int errorCode, String errorMsg) {
                                    Snackbar.make(activity.getWindow().getDecorView().getRootView(), activity.getString(R.string.fail), Snackbar.LENGTH_LONG).show();
                                }

                                @Override
                                public void onDataReceived(JSONObject jsonResponse) {
                                    SharedPreferencesUtil.removeLoginInfo(activity);
                                    Intent intent = new Intent(activity, LoginActivity.class);
                                    activity.finishAffinity();
                                }

                                @Override
                                public void onReceiveResponse() {

                                }
                            });
                        }
                    });
            builder.setNegativeButton(android.R.string.no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            builder.show();
        }
    }

}
