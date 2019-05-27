package kr.co.jmsmart.bingo.view.com.viewModel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.ObservableField;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.wapplecloud.libwapple.ResponseCallback;

import org.apache.commons.codec.binary.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.adapter.DataCardListAdapter;
import kr.co.jmsmart.bingo.data.PetData;
import kr.co.jmsmart.bingo.data.PetDataDayOfMonth;
import kr.co.jmsmart.bingo.databinding.ActivityDetailChartBinding;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.util.GraphUtil;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.com.DetailChartActivity;


public class DetailChartViewModel implements CommonModel {

    private Activity activity = null;
    private String TAG = "DetailChartViewModel";
    private ActivityDetailChartBinding binding;

    private int[] imgArray;
    private SimpleDateFormat sdf;
    private Date today;
    private Calendar cal;

    private String userId;
    private String petSrn;
    private String petNm;
    private String target;
    private String fstDay=null;
    private int goal;
    private ArrayList<PetDataDayOfMonth> detailData;

    private int start=1, end=0;

    private int selectIndex = 0;

    private String defaultBarColor = "#808080";

    private ProgressDialog progressDialog;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat
            (DateFormat.getBestDateTimePattern(
                    Locale.getDefault(), "MMMMyyyy"));


    private boolean isTime = false;





    public ObservableField<String> viewDate = new ObservableField<>("");
    public ObservableField<Integer> nextBtnImg = new ObservableField<>(R.drawable.chevron_right_copy);
    public ObservableField<Integer> preBtnImg = new ObservableField<>(R.drawable.chevron_left);

    public DetailChartViewModel(Activity activity, ActivityDetailChartBinding binding ){
        this.activity = activity;
        this.binding = binding;
    }

    @Override
    public void onCreate() {

        ((AnimationDrawable) binding.ivAni1.getDrawable()).start();
        ((AnimationDrawable) binding.ivAni2.getDrawable()).start();


        progressDialog = new ProgressDialog(activity);

        cal = Calendar.getInstance();
        today = new Date(cal.getTimeInMillis());
        end = cal.get(Calendar.DATE);

        sdf = new SimpleDateFormat("yyyyMM");
        imgArray = new int[]{R.drawable.chevron_left, R.drawable.chevron_left_copy, R.drawable.chevron_right, R.drawable.chevron_right_copy};

        viewDate.set(getViewDate(sdf.format(cal.getTime())));

        Intent intent = activity.getIntent();
        userId = intent.getStringExtra("userId");
        petSrn = intent.getStringExtra("petSrn");
        petNm = intent.getStringExtra("petNm");
        target = intent.getStringExtra("target");
        if(intent.getStringExtra("fstDay")!=null)
            fstDay = intent.getStringExtra("fstDay").substring(0,6);
        else fstDay = null;
        goal = (int)Double.parseDouble(intent.getStringExtra("goal"));
        activity.setTitle(String.format(activity.getString(R.string.title_detail_item), petNm, DataCardListAdapter.titles[DataCardListAdapter.tags.indexOf(target)]));

        String dateString = dateFormatter.format(cal.getTimeInMillis());
        binding.detailTimetxt.setText(dateString);
        if (target.equals("act") || target.equals("play") || target.equals("rest"))
            isTime = true;

        if(fstDay!=null) {
            detailData = (ArrayList<PetDataDayOfMonth>) intent.getSerializableExtra("detailData");

            if (sdf.format(cal.getTime()).equals(fstDay)) {
                preBtnImg.set(imgArray[1]);
                binding.detailPreBtn.setImageResource(imgArray[1]);
            }

            for (int i = 0; i < detailData.size(); i++) {
                Log.d(TAG, "detailData : " + i + "  " + detailData.get(i).barkVal);
            }

            initDataGraphList();
        }
        else{
            preBtnImg.set(imgArray[1]);
            binding.detailPreBtn.setImageResource(imgArray[1]);
        }

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
    private BarData generateGraphData(){
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        int i = 0;
        for (int index = start; index <= end; index++) {
            float y=0;
            if(i<detailData.size()) {
                if (index == Integer.parseInt(detailData.get(i).idx)) {
                    y = detailData.get(i++).getGraphVal(target);
                }
            }
            colors.add(Color.parseColor(defaultBarColor));
            barEntries.add(new BarEntry(index, y));
        }
        if(colors.size()>0) {
            colors.set(colors.size() - 1, activity.getColor(R.color.colorAccent));
            selectIndex = colors.size() - 1;
        }

        BarDataSet bSet = new BarDataSet(barEntries, activity.getString(R.string.steps));
        bSet.setColors(colors);
        bSet.setDrawValues(false);
        float barWidth = 0.65f; // x2 dataset

        BarData bd = new BarData(bSet);
        bd.setBarWidth(barWidth);

        return bd;
    }

    //// src는 yyyyMM 형태
    public String getViewDate(String src){
        try {
            String year = src.substring(2, 4);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat month_date = new SimpleDateFormat("MMMM", new Locale("en", "US"));
            String month_name = month_date.format(sdf.parse(src));

            return month_name + " " + year;
        }
        catch (ParseException e){
            e.printStackTrace();
            return "error";
        }
    }

    public void initDataGraphList(){
        final DataCardListAdapter adapter = new DataCardListAdapter(false, true, petNm);
        adapter.addItem(detailData.get(detailData.size()-1).getPetData(target, goal));
        binding.detailListView.setAdapter(adapter);
        binding.detailListView.setDividerHeight(0);
        //binding.graph.invalidate();
        binding.graph.clear();
        binding.graph.setData(generateGraphData());
        binding.graph.notifyDataSetChanged();

        prepareChart();
    }



    public void prepareChart(){

        XAxis xAxis = binding.graph.getXAxis();
        //xAxis.setLabelCount(entries.size()+1, true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        //////////////////////////////////////////////////y축 조작
        YAxis yAxis = binding.graph.getAxisRight();
        yAxis.setEnabled(false);                         // y축 오른쪽 없애기

        yAxis = binding.graph.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        LimitLine li = new LimitLine(goal,"");
        li.setLineWidth(2f);
        li.setLineColor(Color.parseColor("#b4b6b8"));
        yAxis.addLimitLine(li);

        //////////////////////////////////////////////////

        binding.graph.getDescription().setEnabled(false);        //Description Label 지우기
        binding.graph.getLegend().setEnabled(false);             //색상 설명 지우기
        binding.graph.moveViewToX(binding.graph.getData().getEntryCount() - 1);       // 처음 화면의 위치를 마지막 막대로 옮기기
        binding.graph.setDrawBorders(false);                      // 경계선 안그리기
        binding.graph.setDrawBarShadow(false);                   // 안해주면 막대가 화면 꽉채움
        binding.graph.setVisibleXRangeMaximum(7f);               // 최대 그래프에 7개
        binding.graph.setVisibleXRangeMinimum(7f);               // 최소 그래프에 1개
        //binding.graph.setVisibleYRangeMinimum(0, YAxis.AxisDependency.LEFT);           //y축 값 0부터 시작
        binding.graph.setFitBars(true);                         // 의미없는거같음 1
        binding.graph.setMaxVisibleValueCount(7);                // 의미없는거같음 2
        binding.graph.setBackgroundColor(Color.WHITE);           // 배경 색상
        binding.graph.setDrawGridBackground(false);              // 몰라 해줘야한다고했음
        binding.graph.setAutoScaleMinMaxEnabled(false);
        binding.graph.setDrawValueAboveBar(true);                 // 막대 위의 값

        binding.graph.setTouchEnabled(true);
        binding.graph.setPinchZoom(false);
        binding.graph.setScaleEnabled(false);                    // 확대 막기
        binding.graph.setHighlightPerTapEnabled(false); //이걸해야 클릭이 먹는다

        binding.graph.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                //Toast.makeText(activity,e.getX()+"",Toast.LENGTH_LONG).show();
                int[] colors = binding.graph.getBarData().getColors();
                int date = (int)e.getX();
                colors[date-1] = activity.getColor(R.color.colorAccent);
                colors[selectIndex] = Color.parseColor(defaultBarColor);
                selectIndex = date-1;
                PetDataDayOfMonth sData = null;
                for(PetDataDayOfMonth d:detailData){
                    if(Integer.parseInt(d.idx) == date) {
                        sData = d;
                        break;
                    }
                }
                DataCardListAdapter a = (DataCardListAdapter)binding.detailListView.getAdapter();
                a.clear();
                if(sData != null)
                    a.addItem(sData.getPetData(target, goal));
                else a.addItem(new PetData(target, "-1", "-1", -1, goal));
            }

            @Override
            public void onNothingSelected() {

            }
        });

        binding.graph.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.format("%.1f",value/60f);
            }
        });

        binding.graph.animateY(750);
        binding.graph.invalidate();
    }

    public void initOtherMonthData(Calendar cal){
        String yyyymm = String.format("%04d%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1);


        String dateString = dateFormatter.format(cal.getTimeInMillis());
        binding.detailTimetxt.setText(dateString);

        progressDialog.show();

        APIManager.getInstance(activity).getMonthGraph(userId, petSrn, yyyymm, new ResponseCallback() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                Log.i(TAG, "errorCode: "+errorCode+" errorMsg: " + errorMsg);
                detailData = null;
                progressDialog.dismiss();

            }

            @Override
            public void onDataReceived(JSONObject jsonResponse) {
                Log.d(TAG, "onDataReceived: ");
                try {
                    detailData = new Gson().fromJson(jsonResponse.getJSONArray("monthList").toString(), new TypeToken<List<PetDataDayOfMonth>>() {
                    }.getType());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initDataGraphList();
                        }
                    });
                    progressDialog.dismiss();

                }catch (Exception e){
                    Log.i(TAG, Log.getStackTraceString(e));
                    progressDialog.dismiss();
                }
            }
            @Override
            public void onReceiveResponse() {

            }
        });
    }



    public void onNextBtnClick(){
        if(!nextBtnImg.get().equals(imgArray[3])) {
            cal.add(Calendar.MONTH, 1);
            viewDate.set(getViewDate(sdf.format(cal.getTime())));
            end = cal.getActualMaximum(Calendar.DATE);

            initOtherMonthData(cal);

            if(preBtnImg.get().equals(imgArray[1])){
                preBtnImg.set(imgArray[0]);
                binding.detailPreBtn.setImageResource(imgArray[0]);
            }
            if(sdf.format(today).equals(sdf.format(cal.getTime()))){
                nextBtnImg.set(imgArray[3]);
                binding.detailNextBtn.setImageResource(imgArray[3]);
                end = cal.get(Calendar.DATE);
            }
        }
    }

    public void onPreBtnClick(){
        if(!preBtnImg.get().equals(imgArray[1])) {
            cal.add(Calendar.MONTH, -1);
            end = cal.getActualMaximum(Calendar.DATE);

            viewDate.set(getViewDate(sdf.format(cal.getTime())));

            initOtherMonthData(cal);

            if (nextBtnImg.get().equals(imgArray[3])) {
                //비활성화 되어있다면 활성화하라
                nextBtnImg.set(imgArray[2]);
                binding.detailNextBtn.setImageResource(imgArray[2]);
            }
            //이전 한계
            if(sdf.format(cal.getTime()).equals(fstDay)){
                preBtnImg.set(imgArray[1]);
                binding.detailPreBtn.setImageResource(imgArray[1]);
            }
        }
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}