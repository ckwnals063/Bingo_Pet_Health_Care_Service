package kr.co.jmsmart.bingo.view.com;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivityDailyReportBinding;
import kr.co.jmsmart.bingo.databinding.ActivityWeekCompareBinding;
import kr.co.jmsmart.bingo.view.com.viewModel.DailyReportViewModel;
import kr.co.jmsmart.bingo.view.com.viewModel.WeekCompareViewModel;

public class WeekCompareActivity extends AppCompatActivity {

    private WeekCompareViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityWeekCompareBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_week_compare);
        model = new WeekCompareViewModel(this, binding);
        binding.setWeekCompareModel(model);

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

    public void getCardList(){
        model.getCardList();
    }
}