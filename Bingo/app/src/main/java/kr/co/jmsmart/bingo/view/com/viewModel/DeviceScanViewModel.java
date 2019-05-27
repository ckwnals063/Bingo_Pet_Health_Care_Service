package kr.co.jmsmart.bingo.view.com.viewModel;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.adapter.DeviceAdapter;
import kr.co.jmsmart.bingo.databinding.ActivityDeviceScanBinding;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.com.HomeActivity;


public class DeviceScanViewModel implements CommonModel {

    private Activity activity = null;
    private ActivityDeviceScanBinding binding;

    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    public static final String TYPE_DEVICE_CHANGE = "deviceChange";
    public static final String TYPE_DEVICE_INSERT = "deviceInsert";


    private Animation operatingAnim;
    private DeviceAdapter mDeviceAdapter;
    private String userId;
    private String petId;
    private String type;
    private ProgressDialog progressDialog;

    //텍스트
    public final ObservableField<String> textScanInfo = new ObservableField<>("");
    public final ObservableField<Integer> imgVisible = new ObservableField<>(View.VISIBLE);


    public DeviceScanViewModel(Activity activity, ActivityDeviceScanBinding binding){
        this.activity = activity;
        this.binding = binding;
    }

    private void initView() {
        operatingAnim = AnimationUtils.loadAnimation(activity, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());

        mDeviceAdapter = new DeviceAdapter(activity, userId);
        binding.lvScanResult.setAdapter(mDeviceAdapter);
        binding.lvScanResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mac = mDeviceAdapter.getItem(position).getMac();
                Toast.makeText(activity,mac +" is Clicked",Toast.LENGTH_SHORT).show();
                if(type.equals(TYPE_DEVICE_CHANGE)) {
                    APIManager.getInstance(activity).updateDeviceMac(petId, userId, mac, new ResponseCallback() {
                        @Override
                        public void onError(int errorCode, String errorMsg) {
                            Toast.makeText(activity,activity.getString(R.string.server_error),Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDataReceived(JSONObject jsonResponse) {
                            Intent intent = new Intent(activity, HomeActivity.class);
                            intent.putExtra("userId", userId);
                            activity.startActivity(intent);
                            activity.finish();
                        }

                        @Override
                        public void onReceiveResponse() {

                        }
                    });
                }
                else if(type.equals(TYPE_DEVICE_INSERT)){
                    APIManager.getInstance(activity).addNewDevice(petId, mac, userId, new ResponseCallback() {
                        @Override
                        public void onError(int errorCode, String errorMsg) {
                            Toast.makeText(activity,activity.getString(R.string.server_error),Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDataReceived(JSONObject jsonResponse) {
                            Intent intent = new Intent(activity, HomeActivity.class);
                            intent.putExtra("userId", userId);
                            activity.startActivity(intent);
                            activity.finish();
                        }

                        @Override
                        public void onReceiveResponse() {
                        }
                    });
                }
            }
        });


    }
    public void onClick() {
        if (binding.btSync.getAnimation()==null) {
            checkPermissions();
        } else{
            BleManager.getInstance().cancelScan();
        }
    }



    private void startScan() {
        textScanInfo.set(activity.getString(R.string.ble_scanning_info));
        imgVisible.set(View.VISIBLE);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)binding.layoutScan.getLayoutParams();
        params.gravity = Gravity.TOP|Gravity.CENTER;
        binding.layoutScan.setLayoutParams(params);
        binding.lvScanResult.setVisibility(View.GONE);

        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                binding.btSync.startAnimation(operatingAnim);
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                mDeviceAdapter.notifyDataSetChanged();
                binding.btSync.clearAnimation();
                textScanInfo.set(String.format(activity.getString(R.string.ble_scan_result_info), scanResultList.size()));

                imgVisible.set(View.GONE);
                binding.lvScanResult.setVisibility(View.VISIBLE);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)binding.layoutScan.getLayoutParams();
                params.gravity = Gravity.CENTER;
                binding.layoutScan.setLayoutParams(params);
            }
        });
    }

    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            startScan();
                        }
                    }
                }
                break;
        }
    }


    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(activity, activity.getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(activity, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(activity, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    @Override
    public void onCreate() {
        try {
            petId = activity.getIntent().getStringExtra("petId");
            userId = activity.getIntent().getStringExtra("userId");
            type = activity.getIntent().getStringExtra("type");

            initView();

            BleManager.getInstance().init(activity.getApplication());
            BleManager.getInstance()
                    .enableLog(true)
                    .setReConnectCount(1, 3000)
                    .setConnectOverTime(10000)
                    .setOperateTimeout(3000);

            BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                    .setDeviceName(true, new String[]{"Bingo", "SleepDoc"})
                    .build();
            BleManager.getInstance().initScanRule(scanRuleConfig);

            onClick();
        }
        catch (Exception e){
            Log.i("qweasd", Log.getStackTraceString(e));
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

    public void onBackPressed(){
        Intent intent = new Intent(activity,HomeActivity.class);
        intent.putExtra("userId",userId);
        activity.startActivity(intent);
    }

}
