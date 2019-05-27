package kr.co.jmsmart.bingo.view.com;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivityLoginBinding;
import kr.co.jmsmart.bingo.view.com.viewModel.LoginViewModel;
public class LoginActivity extends AppCompatActivity{

    /** LoginViewModel */
    private LoginViewModel model;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        model = new LoginViewModel(this, binding);
        binding.setLoginModel(model);

        model.onCreate();
    }
    @Override
    protected void onResume() {
        super.onResume();
        model.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        model.onDestroy();
    }

}

