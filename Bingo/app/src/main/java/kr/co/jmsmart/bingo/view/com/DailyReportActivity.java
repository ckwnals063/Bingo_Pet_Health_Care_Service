package kr.co.jmsmart.bingo.view.com;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivityDailyReportBinding;
import kr.co.jmsmart.bingo.databinding.ActivityLoginBinding;
import kr.co.jmsmart.bingo.view.com.viewModel.DailyReportViewModel;
import kr.co.jmsmart.bingo.view.com.viewModel.LoginViewModel;

public class DailyReportActivity extends AppCompatActivity {

    private DailyReportViewModel model;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDailyReportBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_daily_report);
        model = new DailyReportViewModel(this, binding);
        binding.setDailyReportModel(model);


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
    public boolean onCreateOptionsMenu(Menu menu) {
        return model.onCreateOptionsMenu(menu);
    }
}
