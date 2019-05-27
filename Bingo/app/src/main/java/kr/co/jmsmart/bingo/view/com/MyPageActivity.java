package kr.co.jmsmart.bingo.view.com;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONObject;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.data.User;
import kr.co.jmsmart.bingo.util.APIManager;

public class MyPageActivity extends PreferenceActivity {
    private String userId;
    private String flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyPreferenceFragment f = new MyPreferenceFragment();
        Bundle b = new Bundle();
        userId = getIntent().getStringExtra("userId");
        flag = getIntent().getStringExtra("flag");
        b.putString("userId", userId);
        f.setArguments(b);
        getFragmentManager().beginTransaction().replace(android.R.id.content,f).commit();
    }
    @Override
    public void onBackPressed(){
        if(flag != null){
            finish();
        }
        else {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        private static String TAG = "MyPreferenceFragment";
        private static String userId;
        private static User user;
        private static String male, female;
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_my_page);

            userId = getArguments().getString("userId");
            male = getResources().getString(R.string.male);
            female = getResources().getString(R.string.female);

            final PreferenceScreen prefNameMail = (PreferenceScreen)findPreference("pref_name_mail");
            final PreferenceScreen prefPhone = (PreferenceScreen)findPreference("pref_phone");
            final PreferenceScreen prefBirth = (PreferenceScreen)findPreference("pref_birth");
            final PreferenceScreen prefSex = (PreferenceScreen)findPreference("pref_sex");
            final PreferenceScreen prefEdit = (PreferenceScreen)findPreference("pref_edit");
            final Preference.OnPreferenceClickListener listener = new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean ans = true;
                    switch (preference.getKey()){
                        case "pref_edit":{
                            Intent intent = new Intent(MyPreferenceFragment.this.getContext(), MyPageEditActivity.class);
                            intent.putExtra("user", user);
                            startActivity(intent);
                            getActivity().finish();
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
                    Toast.makeText(MyPreferenceFragment.this.getContext(), R.string.fail,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDataReceived(JSONObject jsonResponse) {
                    Log.d(TAG, "onDataReceived: " + jsonResponse.toString());
                    user = new User(jsonResponse.optJSONObject("userInfo"), userId);
                    MyPreferenceFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            prefNameMail.setTitle(user.name);
                            prefNameMail.setSummary(user.userId);
                            prefPhone.setSummary(user.phone);
                            prefBirth.setSummary(user.birth);
                            prefSex.setSummary(user.sex.equals("M")?male:female);
                            prefEdit.setOnPreferenceClickListener(listener);
                        }
                    });
                }

                @Override
                public void onReceiveResponse() {

                }
            });
        }
    }



}
