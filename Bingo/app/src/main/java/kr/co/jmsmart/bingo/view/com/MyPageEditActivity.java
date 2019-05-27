package kr.co.jmsmart.bingo.view.com;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.data.User;
import kr.co.jmsmart.bingo.databinding.ActivityMyPageEditBinding;
import kr.co.jmsmart.bingo.view.com.viewModel.MyPageEditViewModel;

/**
 * Created by Administrator on 2019-01-07.
 */

public class MyPageEditActivity extends AppCompatActivity {
    MyPageEditViewModel model;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMyPageEditBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_my_page_edit);
        model = new MyPageEditViewModel(this , binding, (User) getIntent().getSerializableExtra("user"));
        binding.setMyPageEditModel(model);

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        model.onBackPressed();
    }
}
