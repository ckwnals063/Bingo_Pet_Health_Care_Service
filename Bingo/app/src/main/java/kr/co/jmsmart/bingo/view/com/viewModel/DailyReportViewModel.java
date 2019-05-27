package kr.co.jmsmart.bingo.view.com.viewModel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BaseDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.adapter.DataCardListAdapter;
import kr.co.jmsmart.bingo.data.PetData;
import kr.co.jmsmart.bingo.data.PetDataDayOfMonth;
import kr.co.jmsmart.bingo.data.UserDevice;
import kr.co.jmsmart.bingo.databinding.ActivityDailyReportBinding;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.util.GraphUtil;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.com.DetailChartActivity;

/**
 * Created by Administrator on 2019-01-14.
 */

public class DailyReportViewModel implements CommonModel {
    private static String TAG = "DailyReportViewModel";
    private String userId;
    private String petSrn;
    private String petNm;
    private Activity activity;
    private ActivityDailyReportBinding binding;

    private float start1=7f, end1 = 19f;
    private float start2=19f, end2 = 31f;


    private ArrayList<GraphUtil.GraphItem> dataList;
    private ArrayList<UserDevice> spinerList;
    private ArrayAdapter<String> arrayAdapter;

    private ArrayList<PetDataDayOfMonth> detailData;
    private JSONObject goalMap ;

    private SimpleDateFormat timeTxtFormat = new SimpleDateFormat("yyyy. MM. dd");
    private SimpleDateFormat inpDateFormat = new SimpleDateFormat("yyyyMMdd");
    private Calendar searchCal;
    private int[] imgArray;
    private int preBtnRes;
    private int nextBtnRes;

    private String fstDay = null;

    private ProgressDialog progressDialog;

    public DailyReportViewModel(Activity activity, ActivityDailyReportBinding binding) {
        this.activity = activity;
        this.binding = binding;
    }

    public void setListItem(final JSONObject json){

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.lvReport.setAdapter(new DataCardListAdapter(false, false, petNm, json));
                ((DataCardListAdapter)binding.lvReport.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,100f, activity.getResources().getDisplayMetrics());
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
    public void initGraph(CombinedChart graph, float start, float end){
        graph.getDescription().setEnabled(false);
        graph.setBackgroundColor(Color.WHITE);
        graph.setDrawGridBackground(false);
        graph.setDrawBarShadow(false);
        graph.setHighlightFullBarEnabled(false);

        // draw bars behind lines
        graph.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER
        });


        graph.getDescription().setEnabled(false);
        graph.getLegend().setEnabled(false);             //색상 설명 지우기
        graph.setPinchZoom(false);
        graph.setDoubleTapToZoomEnabled(false);

        YAxis rightAxis = graph.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(-10f); // this replaces setStartAtZero(true)

        YAxis leftAxis = graph.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(-10f); // this replaces setStartAtZero(true)


        XAxis xAxis = graph.getXAxis();
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
    ResponseCallback graphCallback = new ResponseCallback() {
        @Override
        public void onError(int errorCode, String errorMsg) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.graph1.invalidate();
                    binding.graph2.invalidate();
                }
            });

        }

        @Override
        public void onDataReceived(JSONObject jsonResponse) {
            try {
                String json = jsonResponse.getString("graphData").toString();
                Log.i(TAG, json);
                Gson gson = new Gson();
                dataList = gson.fromJson(json, new TypeToken<List<GraphUtil.GraphItem>>() {
                }.getType());
                for (int i = 0; i < dataList.size(); i++) {
                    GraphUtil.GraphItem it = dataList.get(i);
                    Log.i(TAG, String.format("{\"dTime\":\"%s\",\"tLux\":%s,\"barkPoint\":%s,\"avgK\":%s  fTime: %s}", it.getdTime(), it.gettLux(), it.getBarkPoint(), it.getAvgK(), it.getFloatTime()));
                }



                final CombinedChart[] graph = {binding.graph1, binding.graph2};
                final float[] start = {start1, start2};
                final float[] end = {end1, end2};
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float[] max = {0, 0};
                        for (int i = 0; i < 2; i++) {
                            graph[i].setData(generateGraphData(start[i], end[i]));
                            graph[i].notifyDataSetChanged();
                            graph[i].invalidate();
                            Log.i(TAG, "MAX1: " + graph[i].getAxisLeft().getAxisMaximum());
                            max[i] = graph[i].getData().getYMax();
                        }
                        Legend legend = graph[1].getLegend();
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


                        Log.i(TAG, "MAX0: " + max[0] + "MAX1: " + max[1]);
                        int index = max[0] > max[1] ? 0 : 1;
                        Log.i(TAG, "MAX: " + max[index]);
                        if (max[index] < 200) max[index] = 200;

                        Log.i(TAG, "MAX: " + max[index]);

                        for (int i = 0; i < 2; i++) {
                            graph[i].getAxisRight().setAxisMaximum(max[index]+50f);
                            graph[i].getAxisLeft().setAxisMaximum(max[index]+50f);
                            graph[i].setVisibleXRangeMaximum(13f);               // 최대 그래프에 7개
                            graph[i].setVisibleXRangeMinimum(13f);               // 최소 그래프에 1개
                            graph[i].setMaxVisibleValueCount(13);
                            graph[i].notifyDataSetChanged();
                            graph[i].invalidate();
                        }
                    }
                });


            }
            catch (Exception e){
                Log.i(TAG, Log.getStackTraceString(e));
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.graph1.notifyDataSetChanged();
                        binding.graph2.notifyDataSetChanged();
                        binding.graph1.invalidate();
                        binding.graph2.invalidate();
                    }
                });
            }
        }

        @Override
        public void onReceiveResponse() {

        }
    };

    ResponseCallback getDetailDataCallback = new ResponseCallback() {
        @Override
        public void onError(int errorCode, String errorMsg) {
            Log.i(TAG, "errorCode: "+errorCode+" errorMsg: " + errorMsg);
            detailData = null;
            goalMap = null;
            binding.lvReport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                fstDay = goalMap.optString("fstDay");
                binding.lvReport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
    ResponseCallback getDailyDataCallback = new ResponseCallback() {
        @Override
        public void onError(int errorCode, String errorMsg) {
            setListItem(null);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setListViewHeightBasedOnChildren(binding.lvReport);
                }
            });
            progressDialog.dismiss();
        }

        @Override
        public void onDataReceived(JSONObject jsonResponse) {
            APIManager.getInstance(activity).getDailyGraph(userId, petSrn, inpDateFormat.format(searchCal.getTime()) ,graphCallback);
            try {
                setListItem(jsonResponse);
            }catch (Exception e){
                Log.i(TAG, Log.getStackTraceString(e));
                //setListItem(null);
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setListViewHeightBasedOnChildren(binding.lvReport);
                }
            });
            progressDialog.dismiss();

            SimpleDateFormat yyyymm = new SimpleDateFormat("yyyyMM");
            if(detailData==null)
                APIManager.getInstance(activity).getMonthGraph(userId, petSrn, yyyymm.format(searchCal.getTime()), getDetailDataCallback);

        }

        @Override
        public void onReceiveResponse() {

        }
    };

    @Override
    public void onCreate() {
        progressDialog = new ProgressDialog(activity);

        imgArray = new int[]{R.drawable.chevron_left, R.drawable.chevron_left_copy, R.drawable.chevron_right, R.drawable.chevron_right_copy};
        try {
            userId = activity.getIntent().getStringExtra("userId");
            petSrn = activity.getIntent().getStringExtra("petSrn");
            petNm = activity.getIntent().getStringExtra("petNm");

            ((AppCompatActivity) activity).setSupportActionBar(binding.toolbar);

            searchCal = Calendar.getInstance();
            searchCal.setTime(new Date());

            binding.dailyNextBtn.setImageResource(imgArray[3]);
            binding.dailyTimetxt.setText(timeTxtFormat.format(searchCal.getTime()));
            nextBtnRes = imgArray[3];
            preBtnRes = imgArray[0];

            initGraph(binding.graph1, start1, end1);
            initGraph(binding.graph2, start2, end2);

            binding.lvReport.setDividerHeight(0);

            if (TextUtils.equals(petSrn, "FRIEND")) {
                spinerList = APIManager.getInstance(activity).getUserDeviceList(userId);
                String[] petNames = new String[spinerList.size()];
                for (int i = 0; i < spinerList.size(); i++) {
                    petNames[i] = spinerList.get(i).getPetNm();
                }
                arrayAdapter = new ArrayAdapter<String>(activity, R.layout.support_simple_spinner_dropdown_item, petNames);
            } else {
                activity.setTitle(String.format(activity.getString(R.string.title_daily), petNm));
                progressDialog.show();
                APIManager.getInstance(activity).getDailyData(userId, petSrn, inpDateFormat.format(searchCal.getTime()) ,getDailyDataCallback);
            }
        }catch (Exception e){
            Log.i(TAG, Log.getStackTraceString(e));
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

    private CombinedData generateGraphData(float start, float end){
        ArrayList<Entry> lineEntries1 = new ArrayList<>();
        ArrayList<Entry> lineEntries2 = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<Entry> scatterEntries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        int i = 0;
        for(i=0; i<dataList.size(); i++){
            if(start==dataList.get(i).getFloatTime())break;
        }
        for (float index = start; index <= end; index+=0.5) {
            float y1 = 0, y2 = 0, y3 = 0, y4 = 0;
            //if(i==dataList.size()) break;
            if(i!=dataList.size()) {
                if (index == dataList.get(i).getFloatTime()) {
                    y1 = Float.parseFloat(dataList.get(i).gettLux());
                    y2 = Float.parseFloat(dataList.get(i).getUv());
                    y3 = Float.parseFloat(dataList.get(i).getBarkPoint());
                    y4 = Float.parseFloat(dataList.get(i++).getAvgK());
                }
            }
            colors.add(GraphUtil.getTemperatureColor((int) y4));
            lineEntries1.add(new Entry(index, y1));
            lineEntries2.add(new Entry(index, y2));
            barEntries.add(new BarEntry(index, y3));
            scatterEntries.add(new Entry(index, -6));
            scatterEntries.add(new Entry(index + 0.25f, -6));

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
        float barWidth = 0.40f; // x2 dataset

        BarData bd = new BarData(bSet);
        bd.setBarWidth(barWidth);

        ScatterDataSet sSet = new ScatterDataSet(scatterEntries, null);
        Log.i(TAG, "dataList : " + dataList.size()+"");
        Log.i(TAG, "colors : " + colors.size()+"");
        if(dataList.size()>0)
            sSet.setColors(colors);
        else sSet.setColors(new int[]{Color.parseColor("#000000")});
        /*ColorTemplate.MATERIAL_COLORS*/
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

    public boolean onCreateOptionsMenu(Menu menu){
        if(TextUtils.equals(petSrn,"FRIEND")){
            activity.getMenuInflater().inflate(R.menu.menu_spinner,menu);
            MenuItem item = menu.findItem(R.id.menu_spinner);
            Spinner spinner = (Spinner) item.getActionView();
            spinner.setAdapter(arrayAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    activity.setTitle(String.format(activity.getString(R.string.title_daily), spinerList.get(i).getPetNm()));
                    petSrn = spinerList.get(i).getPetSrn();
                    progressDialog.show();
                    APIManager.getInstance(activity).getDailyData(userId, petSrn, inpDateFormat.format(searchCal.getTime()) ,getDailyDataCallback);
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            spinner.setSelected(true);
            spinner.setSelection(0);
        }
        return true;
    }

    public void onPreBtnClick(){
        if(preBtnRes != imgArray[1] && fstDay != null){
            searchCal.add(Calendar.DATE, -1);
            binding.dailyTimetxt.setText(timeTxtFormat.format(searchCal.getTime()));
            progressDialog.show();
            APIManager.getInstance(activity).getDailyData(userId, petSrn, inpDateFormat.format(searchCal.getTime()), getDailyDataCallback);
            binding.dailyNextBtn.setImageResource(imgArray[2]);
            nextBtnRes = imgArray[2];
            if(inpDateFormat.format(searchCal.getTime()).equals(fstDay)){
                binding.dailyPreBtn.setImageResource(imgArray[1]);
                preBtnRes = imgArray[1];
            }
        }
    }

    public void onNextBtnClick(){
        if(nextBtnRes != imgArray[3] && fstDay != null){
            searchCal.add(Calendar.DATE, 1);
            binding.dailyTimetxt.setText(timeTxtFormat.format(searchCal.getTime()));
            progressDialog.show();
            APIManager.getInstance(activity).getDailyData(userId, petSrn, inpDateFormat.format(searchCal.getTime()), getDailyDataCallback);
            binding.dailyPreBtn.setImageResource(imgArray[0]);
            preBtnRes = imgArray[0];
            if(inpDateFormat.format(searchCal.getTime()).equals(inpDateFormat.format(new Date()))){
                binding.dailyNextBtn.setImageResource(imgArray[3]);
                nextBtnRes = imgArray[3];
            }
        }
    }
}