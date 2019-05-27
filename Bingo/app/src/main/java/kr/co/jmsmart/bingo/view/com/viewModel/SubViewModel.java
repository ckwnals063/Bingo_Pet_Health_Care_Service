package kr.co.jmsmart.bingo.view.com.viewModel;

import android.app.Activity;
import android.content.Intent;
import android.databinding.ObservableField;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.model.GradientColor;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.adapter.DataCardListAdapter;
import kr.co.jmsmart.bingo.data.PetDataDayOfMonth;
import kr.co.jmsmart.bingo.databinding.ActivitySubBinding;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.util.GraphUtil;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.com.DetailChartActivity;

/**
 * Created by Administrator on 2019-01-14.
 */

public class SubViewModel implements CommonModel {
    private static String TAG = "SubViewModel";
    private Activity activity;
    private ActivitySubBinding binding;
    private String userId, petSrn, petNm;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
    private Calendar cal;

    //어제 아침 7시 ~ 현시간
    private float start = 7f;
    private float end =0f;



    private ArrayList<GraphUtil.GraphItem> dataList;
    private ArrayList<PetDataDayOfMonth> detailData;
    private JSONObject goalMap ;


    public ObservableField<String> viewDate = new ObservableField<>("");

    private ResponseCallback getDetailDataCallback = new ResponseCallback() {
        @Override
        public void onError(int errorCode, String errorMsg) {
            Log.i(TAG, "errorCode: "+errorCode+" errorMsg: " + errorMsg);
            detailData = null;
            goalMap = null;

            binding.lvCard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    DataCardListAdapter a = ((DataCardListAdapter)adapterView.getAdapter());
                    try {
                        String goal="0";

                        Intent intent = new Intent(activity, DetailChartActivity.class);
                        intent.putExtra("goal", goal);
                        intent.putExtra("target", a.getItem(i).getCdCl());
                        intent.putExtra("userId", userId);
                        intent.putExtra("petSrn", petSrn);
                        intent.putExtra("petNm", petNm);
                        activity.startActivity(intent);
                    }catch (Exception e){
                        Log.i(TAG, Log.getStackTraceString(e));
                    }
                }
            });
        }

        @Override
        public void onDataReceived(JSONObject jsonResponse) {
            try {
                detailData = new Gson().fromJson(jsonResponse.getJSONArray("monthList").toString(), new TypeToken<List<PetDataDayOfMonth>>() {
                }.getType());
                goalMap = jsonResponse.getJSONObject("gData");
                binding.lvCard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        DataCardListAdapter a = ((DataCardListAdapter)adapterView.getAdapter());
                        try {
                            String goal="0";
                            if(!a.getItem(i).getCdCl().equals("cal"))
                                goal= goalMap.getString(a.getItem(i).getCdCl() + "Goal");

                            Intent intent = new Intent(activity, DetailChartActivity.class);
                            intent.putExtra("goal", goal);
                            intent.putExtra("target", a.getItem(i).getCdCl());
                            intent.putExtra("userId", userId);
                            intent.putExtra("petSrn", petSrn);
                            intent.putExtra("petNm", petNm);
                            intent.putExtra("detailData", detailData);
                            intent.putExtra("fstDay", goalMap.getString("fstDay"));
                            activity.startActivity(intent);
                        }catch (Exception e){
                            Log.i(TAG, Log.getStackTraceString(e));
                        }
                    }
                });
            }catch (Exception e){
                Log.i(TAG, Log.getStackTraceString(e));
            }
        }

        @Override
        public void onReceiveResponse() {

        }
    };

    public SubViewModel(Activity activity, ActivitySubBinding binding ) {
        this.activity = activity;
        this.binding = binding;
    }

    public void setListItem(){
        binding.lvCard.setDividerHeight(0);
        String[] targets =  activity.getIntent().getStringArrayExtra("targets");
        JSONObject json = null;
        try {
            json = new JSONObject(activity.getIntent().getStringExtra("json"));

            binding.lvCard.setAdapter(new DataCardListAdapter(false, true, petNm, json));
            ((DataCardListAdapter)binding.lvCard.getAdapter()).setDisplayItem(Arrays.asList(targets));
            ((DataCardListAdapter)binding.lvCard.getAdapter()).notifyDataSetChanged();
            binding.lvCard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });

        }catch (Exception e) {
        }

    }
    public void initGraph(){
        binding.graph.getDescription().setEnabled(false);
        binding.graph.setBackgroundColor(Color.WHITE);
        binding.graph.setDrawGridBackground(false);
        binding.graph.setDrawBarShadow(false);
        binding.graph.setHighlightFullBarEnabled(false);

        // draw bars behind lines
        binding.graph.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER
        });


        binding.graph.getDescription().setEnabled(false);
        binding.graph.getLegend().setEnabled(false);             //색상 설명 지우기
        binding.graph.setPinchZoom(false);
        binding.graph.setDoubleTapToZoomEnabled(false);

        YAxis rightAxis = binding.graph.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(-10f); // this replaces setStartAtZero(true)


        YAxis leftAxis = binding.graph.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(-10f); // this replaces setStartAtZero(true)

        XAxis xAxis = binding.graph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(start-0.5f);
        xAxis.setAxisMaximum(end+0.5f);
        xAxis.setGranularity(1f);
        //xAxis.setAxisMaximum(end);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return (int)(value%24) + "";
            }
        });

    }


    @Override
    public void onCreate() {
        ((AnimationDrawable) binding.ivAni1.getDrawable()).start();
        ((AnimationDrawable) binding.ivAni2.getDrawable()).start();

        Calendar today = Calendar.getInstance();
        String yyyymm = String.format("%04d%02d", today.get(Calendar.YEAR), today.get(Calendar.MONTH)+1);

        end = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+24;
        userId = activity.getIntent().getStringExtra("userId");
        petSrn = activity.getIntent().getStringExtra("petSrn");
        petNm = activity.getIntent().getStringExtra("petNm");

        activity.setTitle(String.format(activity.getString(R.string.title_sub), petNm));

        cal = new GregorianCalendar(Locale.KOREA);
        viewDate.set(getViewDate(sdf.format(cal.getTime())));


        setListItem();

        initGraph();
        APIManager.getInstance(activity).getMonthGraph(userId, petSrn, yyyymm, getDetailDataCallback);


        APIManager.getInstance(activity).getCoachGraph(userId, petSrn, yyyymm ,new ResponseCallback() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.graph.invalidate();
                    }
                });
            }

            @Override
            public void onDataReceived(JSONObject jsonResponse) {
                try {
                    String json = jsonResponse.getString("graphData").toString();
                    Log.i(TAG, json);

                    Gson gson = new Gson();
                    dataList = gson.fromJson(json, new TypeToken<List<GraphUtil.GraphItem>>(){}.getType());
                    for(int i = 0 ; i < dataList.size(); i++){
                        GraphUtil.GraphItem it = dataList.get(i);
                        Log.i(TAG, String.format("{\"dTime\":\"%s\",\"avgLux\":%s,\"uv\":%s,\"barkPoint\":%s,\"avgK\":%s  fTime: %s}", it.getdTime(), it.getAvgK(), it.getUv(), it.getBarkPoint(), it.getAvgK(), it.getFloatTime()));
                    }

                    Log.i(TAG, String.format("{\"start\":\"%s\",\"end\":%s}", start, end));

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.graph.setData(generateGraphData());
                            if(binding.graph.getAxisLeft().getAxisMaximum()<200f){
                                binding.graph.getAxisLeft().setAxisMaximum(200f);
                                binding.graph.getAxisRight().setAxisMaximum(200f);
                            }
                            binding.graph.invalidate();
                            binding.graph.setVisibleXRangeMaximum(13f);               // 최대 그래프에 7개
                            binding.graph.setVisibleXRangeMinimum(13f);               // 최소 그래프에 1개
                            binding.graph.setMaxVisibleValueCount(13);
                            binding.graph.moveViewToX(binding.graph.getData().getEntryCount() - 1);       // 처음 화면의 위치를 마지막 막대로 옮기기
                            Legend legend = binding.graph.getLegend();
                            ArrayList<LegendEntry> legendEntryList = new ArrayList<>();
                            for(LegendEntry e : legend.getEntries()){
                                String label =( e.label==null?"null":e.label.toString());
                                Log.i(TAG, "LegendEntry :"  + label);
                                if(!label.equals("null")){
                                    legendEntryList.add(e);
                                }
                            }
                            legend.setCustom(legendEntryList);
                            legend.setEnabled(true);
                        }
                    });
                }
                catch (Exception e){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.graph.invalidate();
                        }
                    });
                    Log.i(TAG, "[Error] :" + Log.getStackTraceString(e));
                }
            }

            @Override
            public void onReceiveResponse() {

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

    private CombinedData generateGraphData(){
        ArrayList<Entry> lineEntries1 = new ArrayList<>();
        ArrayList<Entry> lineEntries2 = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<Entry> scatterEntries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        int i = 0;
        for (float index = start; index <= end; index+=0.5) {
            float y1 = 0, y2 = 0, y3 = 0, y4 = 0;
            if(i==dataList.size()) break;
            if(index==dataList.get(i).getFloatTime()){
                y1 = Float.parseFloat(dataList.get(i).gettLux());
                y2 = Float.parseFloat(dataList.get(i).getUv());
                y3 = Float.parseFloat(dataList.get(i).getBarkPoint());
                y4 = Float.parseFloat(dataList.get(i++).getAvgK());

            }
            colors.add(GraphUtil.getTemperatureColor((int)y4));
            lineEntries1.add(new Entry(index, y1));
            lineEntries2.add(new Entry(index, y2));
            barEntries.add(new BarEntry(index, y3));
            scatterEntries.add(new Entry(index, -6));
            scatterEntries.add(new Entry(index+0.25f, -6));

        }
        LineDataSet lSet1 = new LineDataSet(lineEntries1, activity.getString(R.string.sun_exposure));
        lSet1.setColor(activity.getColor(R.color.colorPrimary));
        lSet1.setLineWidth(2.0f);
        lSet1.setDrawCircles(false);
        lSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lSet1.setDrawValues(false);
        //lSet1.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineDataSet lSet2 = new LineDataSet(lineEntries2, activity.getString(R.string.ultraviolet));
        lSet2.setColor(activity.getColor(R.color.colorSecond));
        lSet2.setLineWidth(2.0f);
        lSet2.setDrawCircles(false);
        lSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lSet2.setDrawValues(false);
        //lSet2.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineData ld = new LineData(lSet1, lSet2);

        BarDataSet bSet = new BarDataSet(barEntries, activity.getString(R.string.steps));
        bSet.setColor(activity.getColor(R.color.colorAccent));
        bSet.setDrawValues(false);

        float barWidth = 0.25f; // x2 dataset

        BarData bd = new BarData(bSet);
        bd.setBarWidth(barWidth);

        ScatterDataSet sSet = new ScatterDataSet(scatterEntries, null);
        sSet.setColors(colors/*ColorTemplate.MATERIAL_COLORS*/);
        sSet.setScatterShape(ScatterChart.ScatterShape.SQUARE);
        sSet.setScatterShapeSize(TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 8f, activity.getResources().getDisplayMetrics() ));
        sSet.setDrawValues(false);
        ScatterData sd = new ScatterData(sSet);

        CombinedData data = new CombinedData();
        data.setData(ld);
        data.setData(bd);
        data.setData(sd);
        return data;
    }


    protected float getRandom(float range, float start) {
        return (float) (Math.random() * range) + start;
    }


    //    private BarData generateBarData() {
//
//        ArrayList<BarEntry> entries1 = new ArrayList<>();
//        ArrayList<BarEntry> entries2 = new ArrayList<>();
//
//        for (int index = start; index <= end; index++) {
//            entries1.add(new BarEntry(0, getRandom(25, 25)));
//
//            // stacked
//            entries2.add(new BarEntry(0, new float[]{getRandom(13, 12), getRandom(13, 12)}));
//        }
//
//        BarDataSet set1 = new BarDataSet(entries1, "Bar 1");
//        set1.setColor(Color.rgb(60, 220, 78));
//        set1.setValueTextColor(Color.rgb(60, 220, 78));
//        set1.setValueTextSize(10f);
//        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
//
//        BarDataSet set2 = new BarDataSet(entries2, "");
//        set2.setStackLabels(new String[]{"Stack 1", "Stack 2"});
//        set2.setColors(Color.rgb(61, 165, 255), Color.rgb(23, 197, 255));
//        set2.setValueTextColor(Color.rgb(61, 165, 255));
//        set2.setValueTextSize(10f);
//        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
//
//        float groupSpace = 0.06f;
//        float barSpace = 0.02f; // x2 dataset
//        float barWidth = 0.45f; // x2 dataset
//        // (0.45 + 0.02) * 2 + 0.06 = 1.00 -> interval per "group"
//
//        BarData d = new BarData(set1, set2);
//        d.setBarWidth(barWidth);
//
//        // make this BarData object grouped
//        d.groupBars(0, groupSpace, barSpace); // start at x = 0
//
//        return d;
//    }


}