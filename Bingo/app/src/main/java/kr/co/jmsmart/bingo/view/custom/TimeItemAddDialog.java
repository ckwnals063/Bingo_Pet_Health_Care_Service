package kr.co.jmsmart.bingo.view.custom;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.sql.Struct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.adapter.TimeListAdapter;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.view.com.WeekCompareActivity;
import kr.co.jmsmart.bingo.view.com.viewModel.WeekCompareViewModel;

/**
 * Created by ZZQYU on 2019-02-12.
 */

public class TimeItemAddDialog extends Dialog{
    private String TAG = "TimeItemAddDialog";
    private Activity activity;
    private int selItemCode = 99;
    private String petSrn;
    private String userId;

    private GridView gridView;
    private AppCompatEditText editText, editYear, editMonth, editDay;
    private AppCompatButton btCancel, btOk;

    private LinearLayoutCompat preView;


    public TimeItemAddDialog(Activity activity, String petSrn, String userId) {
        super(activity);
        // TODO Auto-generated constructor stub
        this.activity = activity;
        this.petSrn = petSrn;
        this.userId = userId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_add_time_item);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String today = sdf.format(new Date());

        gridView = findViewById(R.id.gv_item);
        //editText = findViewById(R.id.et_item_date);
        editYear = findViewById(R.id.input_year);
        editMonth = findViewById(R.id.input_month);
        editDay = findViewById(R.id.input_day);
        btCancel = findViewById(R.id.bt_cancel);
        btOk = findViewById(R.id.bt_ok);

        editYear.setText(today.substring(0,4));
        editMonth.setText(today.substring(4,6));
        editDay.setText(today.substring(6,8));

        gridView.setAdapter(new ImageAdapter(activity, TimeListAdapter.types));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(preView!=null)
                    preView.setBackgroundResource(android.R.color.white);
                GridView g = (GridView)adapterView;
                ImageAdapter a = (ImageAdapter)g.getAdapter();
                a.notifyDataSetChanged();
                selItemCode = i;
                LinearLayoutCompat ll = (LinearLayoutCompat)view;
                ll.setBackgroundResource(R.color.colorAccent);
                preView = ll;
            }
        });
//        editText.addTextChangedListener(new TextWatcher() {
//            private String current = "";
//            private String ddmmyyyy = "DDMMYYYY";
//            private Calendar cal = Calendar.getInstance();
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (!s.toString().equals(current)) {
//                    String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
//                    String cleanC = current.replaceAll("[^\\d.]|\\.", "");
//
//                    int cl = clean.length();
//                    int sel = cl;
//                    for (int i = 2; i <= cl && i < 6; i += 2) {
//                        sel++;
//                    }
//                    //Fix for pressing delete next to a forward slash
//                    if (clean.equals(cleanC)) sel--;
//
//                    if (clean.length() < 8){
//                        clean = clean + ddmmyyyy.substring(clean.length());
//                    }else{
//                        //This part makes sure that when we finish entering numbers
//                        //the date is correct, fixing it otherwise
//                        int day  = Integer.parseInt(clean.substring(0,2));
//                        int mon  = Integer.parseInt(clean.substring(2,4));
//                        int year = Integer.parseInt(clean.substring(4,8));
//
//                        mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
//                        cal.set(Calendar.MONTH, mon-1);
//                        year = (year<1900)?1900:(year>2100)?2100:year;
//                        cal.set(Calendar.YEAR, year);
//                        // ^ first set year for the line below to work correctly
//                        //with leap years - otherwise, date e.g. 29/02/2012
//                        //would be automatically corrected to 28/02/2012
//
//                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
//                        clean = String.format("%02d%02d%02d",day, mon, year);
//                    }
//
//                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
//                            clean.substring(2, 4),
//                            clean.substring(4, 8));
//
//                    sel = sel < 0 ? 0 : sel;
//                    current = clean;
//                    editText.setText(current);
//                    editText.setSelection(sel < current.length() ? sel : current.length());
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickCancel();
            }
        });
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickOk();
            }
        });

    }

    public void clickCancel(){
        dismiss();
    }
    public void clickOk(){
        if(editYear.getText().toString().length()<4 || editMonth.getText().toString().length()<2 || editDay.getText().toString().length()<2)
            Toast.makeText(activity, R.string.please_date, Toast.LENGTH_LONG).show();
        else if(selItemCode == 99)
            Toast.makeText(activity,activity.getString(R.string.time_item_no_select_error),Toast.LENGTH_SHORT).show();
        else if(!isValidDateStr(editYear.getText().toString() + editMonth.getText().toString() + editDay.getText().toString())){
            Toast.makeText(activity, activity.getString(R.string.time_item_non_valid_date_error),Toast.LENGTH_SHORT).show();
        }
        else {
            //Toast.makeText(activity, "selItemName: " + selItemName + " date: " + editText.getText().toString(), Toast.LENGTH_LONG).show();
            //String inpDate = editText.getText().toString().replace("/","");
            String inpDate = editYear.getText().toString() + editMonth.getText().toString() + editDay.getText().toString();

            APIManager.getInstance(activity).addWeeklyCard(petSrn, userId, inpDate, selItemCode, new ResponseCallback() {
                @Override
                public void onError(int errorCode, String errorMsg) {
                    Toast.makeText(activity, activity.getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDataReceived(JSONObject jsonResponse) {
                    ((WeekCompareActivity) activity).getCardList();
                    dismiss();
                }

                @Override
                public void onReceiveResponse() {

                }
            });
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private List<String> values;

        public ImageAdapter(Context context, List<String> values) {
            this.context = context;
            this.values = values;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            int iconId = TimeListAdapter.iconIds[values.indexOf(values.get(position))];
            int itemNameId = TimeListAdapter.contentTexts[values.indexOf(values.get(position))];
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {
                // get layout from mobile.xml
                gridView = inflater.inflate(R.layout.adapter_grid_item, null);

                ImageView imageView = gridView.findViewById(R.id.iv_icon);
                imageView.setImageResource(iconId);

                // set value into textview
                TextView textView = (TextView) gridView.findViewById(R.id.tv_item_name);
                textView.setText(itemNameId);

            } else {
                gridView = (View) convertView;
            }

            return gridView;
        }


        @Override
        public int getCount() {
            return values.size();
        }

        @Override
        public String getItem(int position) {
            return values.get(position);
        }



        @Override
        public long getItemId(int position) {
            return 0;
        }

    }

    public static boolean isValidDateStr(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setLenient(false);
            sdf.parse(date);
        }
        catch (ParseException e) {
            return false;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

}