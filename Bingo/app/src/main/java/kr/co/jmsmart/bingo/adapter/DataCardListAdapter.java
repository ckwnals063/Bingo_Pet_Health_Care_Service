package kr.co.jmsmart.bingo.adapter;


import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.data.PetData;
import kr.co.jmsmart.bingo.databinding.AdapterDataCardItemBinding;
import kr.co.jmsmart.bingo.databinding.AdapterRankCardItemBinding;

public class DataCardListAdapter extends BaseAdapter  {
    String TAG = "DataCardListAdapter";
    private String rankingMent;
    private ArrayList<PetData> itemList;
    private boolean isMentVisible;
    private boolean isMain;
    private String petNm;
    public static int[] iconIds = new int[]{R.drawable.ic_depression, R.drawable.ic_sun_exposure, R.drawable.ic_luxpol,R.drawable.ic_uv, R.drawable.ic_vitamind,
            R.drawable.ic_step, R.drawable.ic_cal, R.drawable.ic_activity, R.drawable.ic_play, R.drawable.ic_rest};

    public static String[] titles = new String[]{"Depression", "Sun Exposure", "Light Pollution", "UV Exposure", "Vitamin D",
            "Bark Point", "Calorie","Activity", "Play", "Rest"};
    public static List<String> tags = Arrays.asList(new String[]{"dep", "sun", "luxpol", "uv", "vit", "bark", "cal", "act", "play", "rest"});
    public DataCardListAdapter(boolean isMain, boolean isMentVisible, String petNm, JSONObject json) {
        this.itemList = jsonToPetData(json);
        this.isMentVisible = isMentVisible;
        this.isMain = isMain;
        this.petNm = petNm;
    }
    public DataCardListAdapter(boolean isMain, boolean isMentVisible, String petNm, ArrayList<PetData> itemList) {
        this.itemList = itemList;
        this.isMentVisible = isMentVisible;
        this.isMain = isMain;
        this.petNm = petNm;
    }
    public DataCardListAdapter(boolean isMain, boolean isMentVisible, String petNm) {
        this(isMain, isMentVisible, petNm, new ArrayList<PetData>());
    }
    public void setRankingMent(String rankingMent){
        this.rankingMent = rankingMent;
        addItem(new PetData(rankingMent, null, null, -1, -1));
    }

    private ArrayList<PetData> jsonToPetData(JSONObject json){
        if(json!=null) {
            JSONObject cData = json.optJSONObject("cData");
            JSONObject gData = json.optJSONObject("gData");

            Log.i(TAG,"json data : " + json.toString());
            Log.i(TAG, "cData data : " + cData.toString());
            Log.i(TAG, "gData data : " + gData.toString());

            Log.d(TAG, "idx data : " + cData.optInt("idx"));

            if(cData.optInt("idx") == -1){
                ArrayList<PetData> ans = new ArrayList<>();
                for (int i = 0; i < tags.size(); i++) {
                    try {
                        String target = tags.get(i);
                        PetData pet = new PetData(target,
                                "0",
                                "WARNING",
                                0,
                                target != "cal" ? gData.getInt(target + "Goal") : 0);
                        ans.add(pet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return ans;
            }
            else {
                ArrayList<PetData> ans = new ArrayList<>();
                for (int i = 0; i < tags.size(); i++) {
                    try {
                        String target = tags.get(i);
                        PetData pet = new PetData(target,
                                cData.getString(target + "Grade"),
                                cData.getString(target + "GradeText"),
                                cData.getInt(target + "Val"),
                                target != "cal" ? gData.getInt(target + "Goal") : 0);
                        ans.add(pet);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return ans;
            }
        }
        else return new ArrayList<>();
    }

    public void setDisplayItem(final List<String> displayTags){
        Collections.sort(itemList, new Comparator<PetData>() {
            @Override
            public int compare(PetData a, PetData b) {
                return new Integer(displayTags.indexOf(a.getCdCl())).compareTo(displayTags.indexOf(b.getCdCl()));
            }
        });
        int size = itemList.size()-displayTags.size();
        for(int i = 0 ; i<size; i++)itemList.remove(0);
    }


    public void addItem(PetData item) {
        itemList.add(item);
    }

    public void removeItem(PetData item) {
        itemList.remove(item);
    }
    public void removeItem(int itemPosition) {
        itemList.remove(itemPosition);
    }

    public void clear(){
        itemList.clear();
        rankingMent = null;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public PetData getItem(int position) {
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
        if(position == getCount()-1 && rankingMent!=null){
            AdapterRankCardItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.adapter_rank_card_item, parent, false);
            binding.tvMent.setText(rankingMent);
            convertView = binding.getRoot();
        }
        else {
            AdapterDataCardItemBinding binding;
            if (convertView != null) {
                binding = (AdapterDataCardItemBinding) convertView.getTag();
            } else {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                binding = DataBindingUtil.inflate(inflater, R.layout.adapter_data_card_item, parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            }
            if (!isMentVisible) binding.cardMent.setVisibility(View.GONE);
            if (itemList != null) {
                PetData item = itemList.get(position);

                binding.cardIcon.setImageDrawable(parent.getContext().getDrawable(item.getIconResourceId()));
                binding.cardTitle.setText(item.getTitle());
                if (item.getDataCd().equals("-1")) {
                    binding.cardMent.setText("-");
                    binding.cardRate.setText("No Data");
                    binding.cardValue.setText("-");
                } else {
                    binding.cardMent.setText(item.getMent(parent.getContext(), petNm, isMain));
                    binding.cardRate.setText(item.getRate(parent.getContext()));
                    binding.cardValue.setText(item.getValue(parent.getContext()));
                }
                binding.cardBaseValue.setText(item.getBaseValue(parent.getContext()));

            }
        }


        return convertView;
    }




}