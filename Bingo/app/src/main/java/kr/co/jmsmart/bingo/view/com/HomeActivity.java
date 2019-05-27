package kr.co.jmsmart.bingo.view.com;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivityHomeBinding;
import kr.co.jmsmart.bingo.view.com.viewModel.HomeViewModel;

public class HomeActivity extends AppCompatActivity {
    private HomeViewModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHomeBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        binding.setHomeModel(model);
        model = new HomeViewModel(this, binding);
        model.onCreate();

    }

    @Override
    protected void onResume() {
        super.onResume();
        model.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        model.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        model.onDestroy();
    }

    @Override
    public void onBackPressed() {
        model.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return model.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return model.onOptionsItemSelected(item);
    }
}
