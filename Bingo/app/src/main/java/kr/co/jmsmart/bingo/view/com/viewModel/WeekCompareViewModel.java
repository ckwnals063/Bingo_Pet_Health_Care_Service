package kr.co.jmsmart.bingo.view.com.viewModel;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Struct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.adapter.TimeListAdapter;
import kr.co.jmsmart.bingo.data.CompareItem;
import kr.co.jmsmart.bingo.databinding.ActivityWeekCompareBinding;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.custom.TimeItemAddDialog;

/**
 * Created by Administrator on 2019-01-14.
 */

public class WeekCompareViewModel implements CommonModel {
    private static String TAG = "WeekCompareViewModel";
    private String userId;
    private String petSrn;
    private String petNm;
    private Activity activity;
    private ActivityWeekCompareBinding binding;
    private TimeListAdapter adapter;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat
            (DateFormat.getBestDateTimePattern(
                    Locale.getDefault(), "MMM/dd"));

    private ResponseCallback getWeeklyCardListCallback = new ResponseCallback() {
        @Override
        public void onError(int errorCode, String errorMsg) {

        }

        @Override
        public void onDataReceived(JSONObject jsonResponse) {
            String json = jsonResponse.optJSONArray("weeklyList").toString();
            Log.d(TAG, "onDataReceived Json: " + json);
            final ArrayList<CompareItem> itemList = new Gson().fromJson(json, new TypeToken<List<CompareItem>>() {}.getType());
            adapter = new TimeListAdapter(itemList);
            binding.lvTimeline.setAdapter(adapter);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(itemList.size()>0) {
                        binding.lvTimeline.setItemChecked(0,true);
                        binding.lvTimeline.performItemClick(binding.lvTimeline.getSelectedView(), 0, 0);

                    }
                }
            });

        }

        @Override
        public void onReceiveResponse() {

        }
    };

    public WeekCompareViewModel(Activity activity, ActivityWeekCompareBinding binding) {
        this.activity = activity;
        this.binding = binding;
    }

    @Override
    public void onCreate() {
        userId = activity.getIntent().getStringExtra("userId");
        petSrn = activity.getIntent().getStringExtra("petSrn");
        petNm = activity.getIntent().getStringExtra("petNm");

        activity.setTitle(String.format(activity.getString(R.string.title_week_comp), petNm));

        binding.fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*TimeItemAddDialog dialog = new TimeItemAddDialog(activity); // 왼쪽 버튼 이벤트

                dialog.setCancelable(true);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.show();*/
                TimeItemAddDialog dialog = new TimeItemAddDialog(activity,petSrn,userId);
                dialog.show();
            }
        });

        binding.lvTimeline.setDividerHeight(0);

        getCardList();

        binding.lvTimeline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                binding.infoTv.setVisibility(View.GONE);
                binding.weekBarchart.setVisibility(View.VISIBLE);

                APIManager.getInstance(activity).getWeeklyGraphData(userId, petSrn, adapter.getItem(position).getInpDate(), new ResponseCallback() {
                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        Log.d(TAG, "Graph Data Get Error");
                    }

                    @Override
                    public void onDataReceived(JSONObject jsonResponse) {
                        JSONArray array = jsonResponse.optJSONArray("graphData");

                        final String inpDate = adapter.getItem(position).getInpDate();

                        ArrayList<BarEntry> actList;
                        ArrayList<BarEntry> playList;
                        ArrayList<BarEntry> restList;
                        final ArrayList<String> labelList;

                        try {
                            actList = makeBarEntryList(inpDate);
                            playList = makeBarEntryList(inpDate);
                            restList = makeBarEntryList(inpDate);
                            labelList = makeLabelList(inpDate);

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                            for(int i = array.length()-1; i >= 0 ; i--){
                                JSONObject json = array.optJSONObject(i);

                                int j = array.length()-1-i;
                                Date date = sdf.parse(json.optString("idx"));
                                String idx = dateFormatter.format(date);

                                for(int index = 0 ; index < actList.size() ; index++){
                                    if(TextUtils.equals(idx, labelList.get(index))){
                                        actList.get(index).setY(json.optInt("actVal")/60f);
                                        playList.get(index).setY(json.optInt("playVal")/60f);
                                        restList.get(index).setY(json.optInt("restVal")/60f);
                                    }
                                }
                            }
                            Log.d(TAG, "Json Received : " + jsonResponse.toString());
                            Log.d(TAG, "actList Size is : " + actList.size());
                            for(int i=0; i < actList.size(); i++){
                                Log.d(TAG, "actList[" + i +"] : " + actList.get(i).getX() + "  " + actList.get(i).getY());
                            }

                            if(binding.weekBarchart.getData() != null && binding.weekBarchart.getData().getDataSetCount() > 0){
                                BarDataSet actSet = (BarDataSet) binding.weekBarchart.getData().getDataSetByIndex(0);
                                BarDataSet playSet = (BarDataSet) binding.weekBarchart.getData().getDataSetByIndex(1);
                                BarDataSet restSet = (BarDataSet) binding.weekBarchart.getData().getDataSetByIndex(2);

                                actSet.setValues(actList);
                                playSet.setValues(playList);
                                restSet.setValues(restList);

                                binding.weekBarchart.getData().notifyDataChanged();
                                binding.weekBarchart.notifyDataSetChanged();
                            }
                            else{
                                BarDataSet actSet = new BarDataSet(actList,activity.getString(R.string.act_time));
                                actSet.setColor(Color.rgb(104, 241, 175));
                                BarDataSet playSet = new BarDataSet(playList, activity.getString(R.string.play_time));
                                playSet.setColor(Color.rgb(164, 228, 251));
                                BarDataSet restSet = new BarDataSet(restList, activity.getString(R.string.rest_time));
                                restSet.setColor(Color.rgb(242, 247, 158));

                                BarData barData = new BarData(actSet, playSet, restSet);
                                barData.setValueFormatter(new LargeValueFormatter());
                                barData.setHighlightEnabled(false);
                                barData.setDrawValues(true);

                                binding.weekBarchart.setData(barData);
                            }
                            float groupSpace = 0.1f;
                            float barSpace = 0.05f; // x3 DataSet
                            float barWidth = 0.25f; // x3 DataSet

                            String labelText = activity.getString(adapter.getItem(position).getItemTextStringId());

                            binding.weekBarchart.getAxisLeft().setAxisMinimum(0);
                            binding.weekBarchart.getAxisRight().setEnabled(false);
                            XAxis x = binding.weekBarchart.getXAxis();
                            x.setPosition(XAxis.XAxisPosition.BOTTOM);
                            x.setAxisMinimum(actList.get(0).getX());
                            x.setAxisMaximum(actList.get(actList.size()-1).getX()+ 1f);
                            x.setGranularity(1f);

                            x.setValueFormatter(new IAxisValueFormatter() {
                                @Override
                                public String getFormattedValue(float value, AxisBase axis) {
                                    if(Math.round(value) == value && value < 15f){
                                        return labelList.get((int)value);
                                    }
                                    return "";
                                }
                            });

                            LimitLine todayLine = new LimitLine(7f,labelText);
                            todayLine.setLineWidth(1f);
                            todayLine.setLineColor(Color.BLUE);
                            x.removeAllLimitLines();
                            x.addLimitLine(todayLine);

                            binding.weekBarchart.setBackgroundColor(Color.WHITE);
                            binding.weekBarchart.animateY(750);
                            //binding.weekBarchart.setMaxVisibleValueCount(15);
                            binding.weekBarchart.setPinchZoom(false);
                            binding.weekBarchart.setScaleEnabled(false);
                            binding.weekBarchart.setVisibleXRangeMaximum(7f);
                            binding.weekBarchart.setVisibleXRangeMinimum(7f);
                            binding.weekBarchart.moveViewToX(3.5f);
                            binding.weekBarchart.setDrawValueAboveBar(true);
                            binding.weekBarchart.getDescription().setEnabled(false);
                            binding.weekBarchart.getBarData().setBarWidth(barWidth);
                            binding.weekBarchart.groupBars(actList.get(0).getX(),groupSpace,barSpace);
                            binding.weekBarchart.invalidate();
                        }catch (ParseException e){ e.printStackTrace(); }
                    }

                    @Override
                    public void onReceiveResponse() {

                    }
                });
            }
        });

        binding.lvTimeline.setMenuCreator(new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(activity.getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(activity.getResources().getColor(R.color.colorPrimary)));
                // set item width
                deleteItem.setWidth(((int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        60,
                        activity.getResources().getDisplayMetrics()
                )));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                deleteItem.setTitle(R.string.remove);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        });

        binding.lvTimeline.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                APIManager.getInstance(activity).deleteWeekItem(userId, petSrn, adapter.getItem(position).getDataSrn(), new ResponseCallback() {
                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        Toast.makeText(activity,activity.getString(R.string.server_error),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDataReceived(JSONObject jsonResponse) {
                        getCardList();
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

    public void getCardList(){
        APIManager.getInstance(activity).getWeeklyCardList(userId, petSrn, getWeeklyCardListCallback);
    }

    public ArrayList<BarEntry> makeBarEntryList(String inpData) throws ParseException{
        ArrayList<BarEntry> barList = new ArrayList<>();

        for(int i=0; i<15 ; i++){
            barList.add(new BarEntry(i+0f,0f));
        }

        return barList;
    }

    public ArrayList<String> makeLabelList(String inpData) throws ParseException{
        ArrayList<String> labelList = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        Calendar cal = Calendar.getInstance();
        Date date = sdf.parse(inpData);
        cal.setTime(date);
        cal.add(Calendar.DATE, -7);
        for(int i=0; i<16; i++){
            labelList.add(dateFormatter.format(cal.getTime()));
            cal.add(Calendar.DATE, 1);
        }

        return  labelList;
    }
}