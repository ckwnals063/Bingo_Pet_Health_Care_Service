package kr.co.jmsmart.bingo.adapter;


import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.data.CompareItem;
import kr.co.jmsmart.bingo.databinding.AdapterTimeCardItemBinding;

public class TimeListAdapter extends BaseAdapter  {
    private ArrayList<CompareItem> itemList;

    public static int[] iconIds = new int[]{R.drawable.ic_liquid, R.drawable.ic_toy, R.drawable.ic_change_home,R.drawable.ic_leash,
            R.drawable.ic_syringe, R.drawable.ic_drug, R.drawable.ic_feed, R.drawable.ic_cushion};

    public static int[] contentTexts = new int[]{R.string.comp_item_1, R.string.comp_item_2, R.string.comp_item_3, R.string.comp_item_4,
            R.string.comp_item_5, R.string.comp_item_6, R.string.comp_item_7, R.string.comp_item_8};
    public static List<String> types = Arrays.asList(new String[]{"drink", "toy", "house", "leash", "injection", "medicine", "feed", "cushion"});




    public TimeListAdapter() {
        this.itemList = new ArrayList<>();
    }
    public TimeListAdapter(ArrayList<CompareItem> itemList) {
        this.itemList = itemList;
    }

    public void addItem(CompareItem item) {
        itemList.add(item);
    }

    public void removeItem(CompareItem item) {
        itemList.remove(item);
    }
    public void removeItem(int itemPosition) {
        itemList.remove(itemPosition);
    }

    public void clear(){
        itemList.clear();
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public CompareItem getItem(int position) {
        if (position > itemList.size())
            return null;
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AdapterTimeCardItemBinding binding;
        if (convertView != null) {
            binding = (AdapterTimeCardItemBinding) convertView.getTag();
        } else {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            binding = DataBindingUtil.inflate(inflater, R.layout.adapter_time_card_item, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        }
        CompareItem item = itemList.get(position);
        binding.timeline.initLine(0);
        binding.cardDate.setText(item.getInpDate());
        binding.cardIcon.setImageResource(item.getIconResourceId());
        binding.cardValue.setText(item.getItemTextStringId());

        return convertView;
    }


}
