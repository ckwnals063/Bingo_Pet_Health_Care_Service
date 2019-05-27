package kr.co.jmsmart.bingo.view.com;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivityCardListBinding;
import kr.co.jmsmart.bingo.view.com.viewModel.ManagePetViewModel;

public class ManagePetActivity extends AppCompatActivity {

    private ManagePetViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCardListBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_card_list);
        model = new ManagePetViewModel(this, binding);
        binding.setManagePetModel(model);

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

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        model.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        model.onBackPressed();
    }
}
