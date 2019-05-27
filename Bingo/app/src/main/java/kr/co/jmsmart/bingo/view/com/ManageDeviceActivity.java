package kr.co.jmsmart.bingo.view.com;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivityCardListBinding;
import kr.co.jmsmart.bingo.view.com.viewModel.ManageDeviceViewModel;

public class ManageDeviceActivity extends AppCompatActivity {

    private ManageDeviceViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCardListBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_card_list);
        model = new ManageDeviceViewModel(this, binding);
        binding.setManageDeviceModel(model);

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
