package kr.co.jmsmart.bingo.view.com;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivityFriendListBinding;
import kr.co.jmsmart.bingo.view.com.viewModel.FriendListViewModel;

/**
 * Created by Administrator on 2019-01-13.
 */

public class FriendListActivity extends AppCompatActivity {
    private FriendListViewModel model;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFriendListBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_friend_list);
        model = new FriendListViewModel(this, binding);
        binding.setFriendListModel(model);
        model.onCreate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        model.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.onResume();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return model.onCreateOptionsMenu(menu);
    }
}
