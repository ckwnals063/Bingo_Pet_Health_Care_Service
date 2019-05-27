package kr.co.jmsmart.bingo.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.data.Friend;
import kr.co.jmsmart.bingo.databinding.AdapterFriendItemBinding;

/**
 * Created by Administrator on 2019-01-14.
 */

public class FriendListAdapter extends BaseAdapter {
    Context mContext = null;
    ArrayList<Friend> mData = null;

    public FriendListAdapter(Context context){
        mContext = context;
        mData = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getType();
    }

    @Override
    public Friend getItem(int position) {
        return mData.get(position);
    }
    public void addItem(Friend item) {
        mData.add(item);
    }

    public void removeItem(Friend item) {
        mData.remove(item);
    }
    public void removeItem(int itemPosition) {
        mData.remove(itemPosition);
    }

    public void clear(){
        mData.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AdapterFriendItemBinding binding;
        if (convertView != null) {
            binding = (AdapterFriendItemBinding) convertView.getTag();
        } else {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            binding = DataBindingUtil.inflate(inflater, R.layout.adapter_friend_item, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        }
        Friend item = mData.get(position);
        if(item.getType() == Friend.TYPE_HEADER){
            binding.header.setVisibility(View.VISIBLE);
            binding.body.setVisibility(View.GONE);
            binding.tvHeader.setText(item.getName());
            binding.leftArrowFriend.setVisibility(View.GONE);
        }
        else{
            binding.header.setVisibility(View.GONE);
            binding.body.setVisibility(View.VISIBLE);
            binding.tvFName.setText(item.getName());
        }
        return convertView;
    }
}