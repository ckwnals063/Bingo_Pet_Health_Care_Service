package kr.co.jmsmart.bingo.view.com;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivityPetBinding;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.com.viewModel.PetViewModel;

public class PetActivity extends AppCompatActivity{

    private PetViewModel model;

    private int currType;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPetBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_pet);
        currType = getIntent().getIntExtra("type", 0);
        userId = getIntent().getStringExtra("userId");
        model = new PetViewModel(this, binding, userId, currType);
        binding.setPetModel(model);

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
    protected Dialog onCreateDialog(int id) {
        return model.onCreateDialog(id);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        model.onActivityResult(requestCode,resultCode,data);
    }

}

