package kr.co.jmsmart.bingo.view.com;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivitySignUpBinding;
import kr.co.jmsmart.bingo.view.com.viewModel.SignUpViewModel;


/**
 * Created by Administrator on 2019-01-04.
 */

public class SignUpActivity extends AppCompatActivity {

    private SignUpViewModel model = new SignUpViewModel(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySignUpBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        binding.setSignUpModel(model);

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
