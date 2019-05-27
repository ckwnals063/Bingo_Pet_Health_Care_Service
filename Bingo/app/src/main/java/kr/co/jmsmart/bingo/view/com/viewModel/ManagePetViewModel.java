package kr.co.jmsmart.bingo.view.com.viewModel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.github.mikephil.charting.charts.CombinedChart;
import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.adapter.CardListAdapter;
import kr.co.jmsmart.bingo.databinding.ActivityCardListBinding;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.data.Pet;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.com.HomeActivity;
import kr.co.jmsmart.bingo.view.com.PetActivity;


public class ManagePetViewModel implements CommonModel {

    //public final ObservableField<String> btnSignInText = new ObservableField<>("");
    private String TAG  = "ManagePetViewModel";
    private Activity activity;
    private ActivityCardListBinding binding;

    private String userId;

    private CardListAdapter clAdapter;

    public ManagePetViewModel(Activity activity, ActivityCardListBinding binding){
        this.activity = activity;
        this.binding = binding;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //수정됐으면
        if(resultCode==1)
        {
            loadPetData();
        }
    }

    public void loadPetData(){
        APIManager.getInstance(activity).getMypetList(userId, new ResponseCallback() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                Log.d(TAG, "펫 코드 받기 에러 발생 " );
            }

            @Override
            public void onDataReceived(JSONObject jsonResponse) {
                try {
                    clAdapter.clear();
                    JSONArray jArray = jsonResponse.optJSONArray("myPetList");
                    for(int i = 0  ; i < jArray.length(); i++){
                        clAdapter.addItem(new CardListAdapter.Item(new Pet(jArray.getJSONObject(i),  activity)));
                    }
                    binding.clListview.post(new Runnable() {
                        @Override
                        public void run() {
                            clAdapter.notifyDataSetChanged();
                            binding.clListview.invalidateViews();
                        }
                    });

                    Log.i(TAG, "[jArray.length] : " + jArray.length());
                }
                catch (Exception e){
                    Log.i(TAG, "[onDataReceived] : " + Log.getStackTraceString(e));
                }
            }

            @Override
            public void onReceiveResponse() {

            }
        });
    }

    @Override
    public void onCreate() {
        userId = activity.getIntent().getStringExtra("userId");


        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(activity.getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(activity.getResources().getColor(R.color.colorPrimary)));
                // set item width
                deleteItem.setWidth(((int)TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        60,
                        activity.getResources().getDisplayMetrics()
                )));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        clAdapter = new CardListAdapter(activity, new ArrayList<CardListAdapter.Item>());
        binding.clListview.setAdapter(clAdapter);
        binding.clListview.setMenuCreator(creator);
        binding.clListview.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);
        binding.clListview.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        binding.clListview.setDividerHeight(0);
        binding.clListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(activity, PetActivity.class);
                intent.putExtra("pet", clAdapter.getItem(position).getPet());
                intent.putExtra("userId", userId);
                intent.putExtra("type", PetViewModel.TYPE_EDIT);

                activity.startActivityForResult(intent, 0);
            }
        });
        binding.clListview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                APIManager.getInstance(activity).removeCompanionPet(clAdapter.getItem(position).getPet().getId()+"", userId, new ResponseCallback() {
                    @Override
                    public void onError(int errorCode, String errorMsg) {

                    }

                    @Override
                    public void onDataReceived(JSONObject jsonResponse) {
                        clAdapter.removeItem(position);
                        binding.clListview.post(new Runnable() {
                            @Override
                            public void run() {
                                clAdapter.notifyDataSetChanged();
                                binding.clListview.invalidateViews();
                            }
                        });

                    }

                    @Override
                    public void onReceiveResponse() {

                    }
                });
                return true;
            }
        });

        loadPetData();

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {

    }

    public void clickAdd(){
        Intent intent = new Intent(activity, PetActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("type", PetViewModel.TYPE_ADD);
        activity.startActivity(intent);
    }

    public void onBackPressed(){
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.putExtra("userId",userId);
        activity.startActivity(intent);
        activity.finish();
    }
}
