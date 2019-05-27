package kr.co.jmsmart.bingo.view.com.viewModel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.ObservableField;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.adapter.FriendListAdapter;
import kr.co.jmsmart.bingo.data.Friend;
import kr.co.jmsmart.bingo.databinding.ActivityFriendListBinding;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.com.DailyReportActivity;

/**
 * Created by Administrator on 2019-01-13.
 */

public class FriendListViewModel implements CommonModel {
    private Activity activity;
    private ActivityFriendListBinding binding;
    private String userId;
    private FriendListAdapter mAdapter = null;
    private SearchView mSearchView;

    public ObservableField<String> text_friendlist = new ObservableField<>("");
    public ObservableField<String> btn_addFriend = new ObservableField<>("");

    public ResponseCallback retrieveShareKeyCallBack = new ResponseCallback() {
        @Override
        public void onError(int errorCode, String errorMsg) {

        }

        @Override
        public void onDataReceived(JSONObject jsonResponse) {

            mAdapter.clear();
            ArrayList<Friend> sharing = new ArrayList<>();
            sharing.add(new Friend(Friend.TYPE_HEADER, activity.getString(R.string.care_me),null,null,0));
            ArrayList<Friend> shared = new ArrayList<>();
            shared.add(new Friend(Friend.TYPE_HEADER,activity.getString(R.string.care_who),null,null,0));

            JSONArray sharingUserList = jsonResponse.optJSONArray("sharingUserList");
            JSONArray sharedUserList = jsonResponse.optJSONArray("sharedUserList");
            try {
                for(int i=0; i<sharingUserList.length() ; i++){
                    JSONObject object = sharingUserList.optJSONObject(i);
                    sharing.add(new Friend(Friend.TYPE_BODY,object.getString("sUserNm"),object.getString("sUserId"),object.getString("tokenId"),Friend.TYPE_UPSIDE_FRIEND));
                }
                for(int i=0; i<sharedUserList.length() ; i++){
                    JSONObject object = sharedUserList.optJSONObject(i);
                    shared.add(new Friend(Friend.TYPE_BODY,object.getString("sUserNm"),object.getString("sUserId"),object.getString("tokenId"), Friend.TYPE_DOWNSIZE_FRIEND));
                }
            }
            catch (JSONException e) { e.printStackTrace(); }


            if(sharing.size() > 0 || shared.size() > 0) binding.tvEmpty.setVisibility(View.GONE);

            for(int i = 0 ; i < sharing.size() ; i++)
                mAdapter.addItem(sharing.get(i));
            for(int i = 0 ; i < shared.size() ; i++)
                mAdapter.addItem(shared.get(i));

            mAdapter.notifyDataSetChanged();

        }

        @Override
        public void onReceiveResponse() {

        }
    };

    public FriendListViewModel(Activity activity, ActivityFriendListBinding binding ) {
        this.activity = activity;
        this.binding = binding;
    }

    @Override
    public void onCreate() {
        userId = activity.getIntent().getStringExtra("userId");

        mAdapter = new FriendListAdapter(activity);

        APIManager.getInstance(activity).retrieveShareKey(userId, retrieveShareKeyCallBack);

        binding.lvFriend.setAdapter(mAdapter);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                if(menu.getViewType() == Friend.TYPE_BODY) {
                    // create "delete" item
                    SwipeMenuItem deleteItem = new SwipeMenuItem(activity.getApplicationContext());
                    // set item background
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(0xfd, 0x71, 0x5e)));
                    // set item width
                    deleteItem.setWidth(((int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            60,
                            activity.getResources().getDisplayMetrics()
                    )));
                    // set a icon
                    deleteItem.setIcon(R.drawable.ic_delete);
                    // add to menu
                    menu.addMenuItem(deleteItem);
                }
            }
        };
        binding.lvFriend.setMenuCreator(creator);
        binding.lvFriend.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);
        binding.lvFriend.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        binding.lvFriend.setDividerHeight(0);
        binding.lvFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Friend item = (Friend)binding.lvFriend.getAdapter().getItem(position);
                if(item.getType() == Friend.TYPE_BODY && item.getFriendType() == Friend.TYPE_DOWNSIZE_FRIEND){
                    //데일리 리포트
                    String friendUserId = item.getId();
                    Intent intent = new Intent(activity,DailyReportActivity.class);
                    intent.putExtra("userId",item.getId());
                    intent.putExtra("petSrn","FRIEND");
                    intent.putExtra("petNm",item.getName());
                    activity.startActivity(intent);
                }
            }
        });
        binding.lvFriend.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                if (mAdapter.getItem(position).getFriendType() == Friend.TYPE_UPSIDE_FRIEND || mAdapter.getItem(position).getFriendType() == Friend.TYPE_DOWNSIZE_FRIEND)
                    APIManager.getInstance(activity).deleteShareKey(mAdapter.getItem(position).getTokenId(), new ResponseCallback() {
                        @Override
                        public void onError(int errorCode, String errorMsg) {
                            Toast.makeText(activity, activity.getString(R.string.fail_remove_friend), Toast.LENGTH_SHORT).show();
                            binding.lvFriend.smoothCloseMenu();
                        }

                        @Override
                        public void onDataReceived(JSONObject jsonResponse) {
                            APIManager.getInstance(activity).retrieveShareKey(userId, retrieveShareKeyCallBack);
                        }

                        @Override
                        public void onReceiveResponse() {

                        }
                    });
                return true;
            }
        });
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

    public void onAddBtnClick(){
        /*Intent intent = new Intent(activity, AddFriendActivity.class);
        intent.putExtra("userId",userId);
        activity.startActivity(intent);*/
        mSearchView.setIconified(false);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        activity.getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) item.getActionView();
        mSearchView.setQueryHint("e-mail");
        //item.expandActionView();

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                final String query = s.trim();
                mSearchView.setQuery(query, false);
                if(TextUtils.equals(userId, query)){
                    Toast.makeText(activity,activity.getString(R.string.login_id_input_error),Toast.LENGTH_SHORT).show();
                }
                else{
                    boolean flag = true;
                    for(int i=0; i<mAdapter.getCount(); i++){
                        if(TextUtils.equals(query,mAdapter.getItem(i).getId())){
                            flag = false;
                        }
                    }
                    if(flag){
                        APIManager.getInstance(activity).selectNowAccount(query, new ResponseCallback() {
                            @Override
                            public void onError(int errorCode, String errorMsg) {
                                if(errorCode == 103){
                                    Toast.makeText(activity,activity.getString(R.string.user_no_exist_error),Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onDataReceived(JSONObject jsonResponse) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                String name = jsonResponse.optJSONObject("userInfo").optString("userNm");
                                builder.setMessage(String.format(activity.getString(R.string.friend_add_message), name));

                                builder.setPositiveButton(activity.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        APIManager.getInstance(activity).createShareKey(userId, query, new ResponseCallback() {
                                            @Override
                                            public void onError(int errorCode, String errorMsg) {

                                            }

                                            @Override
                                            public void onDataReceived(JSONObject jsonResponse) {
                                                APIManager.getInstance(activity).retrieveShareKey(userId,retrieveShareKeyCallBack);
                                                mSearchView.setQuery("",false);
                                                mSearchView.clearFocus();
                                            }

                                            @Override
                                            public void onReceiveResponse() {

                                            }
                                        });
                                    }
                                });
                                builder.setNegativeButton(activity.getString(R.string.no), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                                builder.show();
                            }

                            @Override
                            public void onReceiveResponse() {

                            }
                        });
                    }
                    else{
                        Toast.makeText(activity,activity.getString(R.string.registerd_user_error),Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) { return false; }
        });
        return true;
    }
}
