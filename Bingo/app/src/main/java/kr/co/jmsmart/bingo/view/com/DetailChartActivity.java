package kr.co.jmsmart.bingo.view.com;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivityDetailChartBinding;
import kr.co.jmsmart.bingo.view.com.viewModel.DetailChartViewModel;

public class DetailChartActivity extends AppCompatActivity {
    private DetailChartViewModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDetailChartBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_chart);
        model = new DetailChartViewModel(this, binding);
        binding.setDetailChartModel(model);

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