package kr.co.jmsmart.bingo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.data.Ranking;

/**
 * Created by Administrator on 2019-01-14.
 */

public class RankingAdapter extends BaseAdapter{
    Context context;
    ArrayList<Ranking> mData;
    LayoutInflater mInflater;

    public RankingAdapter(Context context, ArrayList<Ranking> mData) {
        this.context = context;
        this.mData = mData;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Ranking getItem(int position) {
        return mData.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemLayout = mInflater.inflate(R.layout.custom_ranking_view,null);

        TextView txtRanking = itemLayout.findViewById(R.id.txt_ranking);
        ImageView imgPetPicture = itemLayout.findViewById(R.id.img_mypet_picture);
        TextView txtName = itemLayout.findViewById(R.id.txt_petName);
        TextView txtGrade = itemLayout.findViewById(R.id.txt_petGrade);
        TextView txtOwner = itemLayout.findViewById(R.id.txt_petOwner);

        txtRanking.setText(mData.get(position).ranking);
        imgPetPicture.setImageResource(R.drawable.dog);
        txtName.setText(mData.get(position).name);
        txtGrade.setText(mData.get(position).grade);
        txtOwner.setText(mData.get(position).owner);

        return itemLayout;
    }
}
