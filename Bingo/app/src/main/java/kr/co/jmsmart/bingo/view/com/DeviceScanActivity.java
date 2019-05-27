package kr.co.jmsmart.bingo.view.com;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivityDeviceScanBinding;
import kr.co.jmsmart.bingo.view.com.viewModel.DeviceScanViewModel;

public class DeviceScanActivity extends AppCompatActivity{

    private DeviceScanViewModel model;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDeviceScanBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_device_scan);
        model = new DeviceScanViewModel(this, binding);
        binding.setDeviceScanModel(model);



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
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        model.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        model.onBackPressed();
    }
}

