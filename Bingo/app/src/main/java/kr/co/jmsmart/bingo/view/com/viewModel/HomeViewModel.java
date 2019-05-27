package kr.co.jmsmart.bingo.view.com.viewModel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.adapter.DataCardListAdapter;
import kr.co.jmsmart.bingo.data.Command;
import kr.co.jmsmart.bingo.data.UserDevice;
import kr.co.jmsmart.bingo.databinding.ActivityHomeBinding;
import kr.co.jmsmart.bingo.service.CommandService;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.util.DownloadImageTask;
import kr.co.jmsmart.bingo.util.MentUtil;
import kr.co.jmsmart.bingo.util.SharedPreferencesUtil;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.com.DailyReportActivity;
import kr.co.jmsmart.bingo.view.com.DeviceScanActivity;
import kr.co.jmsmart.bingo.view.com.FriendListActivity;
import kr.co.jmsmart.bingo.view.com.LoginActivity;
import kr.co.jmsmart.bingo.view.com.ManageDeviceActivity;
import kr.co.jmsmart.bingo.view.com.ManagePetActivity;
import kr.co.jmsmart.bingo.view.com.MyPageActivity;
import kr.co.jmsmart.bingo.view.com.PetActivity;
import kr.co.jmsmart.bingo.view.com.SettingActivity;
import kr.co.jmsmart.bingo.view.com.SubActivity;
import kr.co.jmsmart.bingo.view.com.WeekCompareActivity;

/**
 * Created by ZZQYU on 2019-01-19.
 */

public class HomeViewModel implements CommonModel {
    private static String TAG = "HomeViewModel";
    public ActivityHomeBinding binding;

    private JSONArray petArray;
    private String[] petNames;

    private JSONObject petData;

    private Activity activity = null;
    private String userId;
    private String petSrn;
    private long pressedTime;
    private BroadcastReceiver receiver;

    private BleManager bManager;

    private ProgressDialog progressDialog;
    private ArrayAdapter<String> spinnerAdapter = null;

    private ArrayList<UserDevice> userDevices;


    BleGattCallback first;

    private static String ACTION_SET_CMD = "bingo_action_set_cmd";
    private static String ACTION_VIEW_PROGRESS = "bingo_action_view_progress";

    private UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private UUID  BATTERY_CHAR_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    private static final UUID DEVICE_INFORMATION_SERVICE_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    private static final UUID SW_REVISION_CHAR_UUID = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");

    private static final UUID GENERAL_SERVICE_UUID = UUID.fromString("0000fffe-0000-1000-8000-00805f9b34fb");
    private static final UUID SYSCMD_CHAR_UUID = UUID.fromString("0000ffff-0000-1000-8000-00805f9b34fb");

    private static final byte SYSCMD_SET_RTC = (byte)0x06;

    private static final byte SYSCMD_GET_UUID = (byte)0x0B;




    public ResponseCallback getMyPetListCallback = new ResponseCallback() {
        @Override
        public void onError(int errorCode, String errorMsg) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, R.string.info_add_pet, Toast.LENGTH_SHORT).show();
                }
            });
            Intent intent = new Intent(activity , PetActivity.class);
            intent.putExtra("userId",userId);
            activity.startActivity(intent);
            activity.finish();
        }
        @Override
        public void onDataReceived(JSONObject jsonResponse) {
            try {
                //JSONObject petDataJson = null;
                petArray = jsonResponse.getJSONArray("myPetList");
                Log.d(TAG, "펫 리스트 받기 성공");

                if(petArray.length() == 0){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, R.string.info_add_pet, Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent intent = new Intent(activity , PetActivity.class);
                    intent.putExtra("userId",userId);
                    activity.startActivity(intent);
                    activity.finish();
                }

                if(petArray.length() > 0) {
                    petNames = new String[petArray.length()];
                    for (int i = 0; i < petArray.length(); i++) {
                        petNames[i] = petArray.getJSONObject(i).getString("petNm");
                    }


                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinnerAdapter = new ArrayAdapter<String>(activity, R.layout.support_simple_spinner_dropdown_item, petNames);
                            binding.spinnerNavSub.setAdapter(spinnerAdapter);
                            binding.spinnerNavSub.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    try {
                                        petSrn = petArray.getJSONObject(position).getString("petSrn");
                                        //APIManager.getInstance(activity).getMyMainProficiency(userId,petSrn, mainProficiencyCallback);
                                        Log.d(TAG, "Spinner Item Selected! petSrn = " + petSrn);
                                        String fileCode = petArray.getJSONObject(position).getString("fileCode");
                                        if(!fileCode.equals("null"))
                                            new DownloadImageTask(binding.image, binding.navView.getHeaderView(0).findViewById(R.id.nav_root)).execute(APIManager.getProfileUrl(fileCode));
                                        else {
                                            binding.image.setImageResource(R.drawable.dog);
                                            binding.navView.getHeaderView(0).findViewById(R.id.nav_root).setBackgroundResource(R.drawable.dog);
                                        }
                                        initBatteryTextView(petSrn);
                                        initMainProficiency(petSrn, userId);
                                    }
                                    catch (JSONException e) { e.printStackTrace(); }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    Log.d(TAG, "Spinner Nothing Selected");
                                }
                            });
                            spinnerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onReceiveResponse() {

        }
    };




    public HomeViewModel(Activity activity, ActivityHomeBinding binding){
        this.binding=binding;
        this.activity = activity;
        bManager = BleManager.getInstance();
        bManager.init(activity.getApplication());
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getString(R.string.connecting));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_BACK && !event.isCanceled()) {
                    if(progressDialog.isShowing()) {
                        //your logic here for back button pressed event
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void initBatteryTextView(final String petSrn){
        final String mac = getMacBySrn(petSrn);
        Log.i(TAG, "mac : "+ mac + ", srn: "+ petSrn + "    getMacBySrn : " + getMacBySrn(petSrn));

        //기기 등록이 안 되어 있는 경우
        if(mac==null){

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.info_add_device);
            builder.setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(activity, DeviceScanActivity.class);
                            intent.putExtra("petId", petSrn);
                            intent.putExtra("userId", userId);
                            intent.putExtra("type",DeviceScanViewModel.TYPE_DEVICE_INSERT);
                            activity.startActivity(intent);
                            activity.finish();
                        }
                    });
            builder.setNegativeButton(android.R.string.no,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            builder.setCancelable(true);
            builder.show();
        }

        binding.tvBattery.setText("-");
        binding.tvSync.setText("-");
        //setListItem(null);
        binding.tvBigHeart.setText("-");
        binding.tvSmHeart.setText("-");
        binding.tvHeartMent.setText("");
        binding.pinkBox.setVisibility(View.GONE);

        binding.swipeView.setRefreshing(false);
        if(!TextUtils.isEmpty(mac) && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            if (!bManager.isConnected(mac)) {
                first = new BleGattCallback() {
                    @Override
                    public void onStartConnect() {
                        progressDialog.show();
                    }
                    @Override
                    public void onConnectFail(BleDevice bleDevice, BleException exception) {
                        progressDialog.dismiss();
                        //연결 실패시 재 시도
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.conect_fail_msg);
                        builder.setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //연결 시도
                                        bManager.connect(mac, first);
                                    }
                                });
                        builder.setNegativeButton(android.R.string.no,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        builder.setCancelable(true);
                        builder.show();
                    }
                    @Override
                    public void onConnectSuccess(BleDevice bleDeviceResult, BluetoothGatt gatt, int status) {
                        progressDialog.dismiss();
                        //연결 성공시 ble데이터 가져오기
                        loadDeviceData(petSrn);
                    }
                    @Override
                    public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                        progressDialog.dismiss();
                    }
                };

                //연결 시도
                bManager.connect(mac, first);
            } else {//연결되어있다
                //ble데이터 가져오기
                loadDeviceData(petSrn);
            }
        }
    }

    public void refreshBatteryTextView(final String petSrn){
        final String mac = getMacBySrn(petSrn);
        Log.i(TAG, "mac : "+ mac + ", srn: "+ petSrn + "    getMacBySrn : " + getMacBySrn(petSrn));
        binding.tvBattery.setText("-");
        binding.tvSync.setText("-");
        //setListItem(null);
        binding.tvBigHeart.setText("-");
        binding.tvSmHeart.setText("-");
        binding.tvHeartMent.setText("");
        binding.pinkBox.setVisibility(View.GONE);

        binding.swipeView.setRefreshing(false);
        if(!TextUtils.isEmpty(mac) && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            if (!bManager.isConnected(mac)) {
                first = new BleGattCallback() {
                    @Override
                    public void onStartConnect() {
                        progressDialog.show();
                    }
                    @Override
                    public void onConnectFail(BleDevice bleDevice, BleException exception) {
                        progressDialog.dismiss();
                        //연결 실패시 재 시도
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.conect_fail_msg);
                        builder.setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //연결 시도
                                        bManager.connect(mac, first);
                                    }
                                });
                        builder.setNegativeButton(android.R.string.no,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        builder.setCancelable(true);
                        builder.show();
                    }
                    @Override
                    public void onConnectSuccess(BleDevice bleDeviceResult, BluetoothGatt gatt, int status) {
                        progressDialog.dismiss();
                        //연결 성공시 ble데이터 가져오기
                        loadDeviceDataNoSync(petSrn);
                    }
                    @Override
                    public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                        progressDialog.dismiss();
                    }
                };

                //연결 시도
                bManager.connect(mac, first);
            } else {//연결되어있다
                //ble데이터 가져오기
                loadDeviceDataNoSync(petSrn);
            }
        }
    }

    public void loadDeviceData(final String petSrn){

        Log.d(TAG, "loadDeviceData");
        new Thread() {
            @Override
            public void run() {
                super.run();
                final String mac = getMacBySrn(petSrn);
                if (mac == null || mac == "") {
                    for(UserDevice device : userDevices){
                        Log.d(TAG, "petSrn : " + device.getPetSrn() + " Mac : " + device.getMac() +" petNm : " + device.getPetNm());
                    }
                }
                else {
                    BluetoothDevice bluetoothDevice = bManager.getBluetoothAdapter().getRemoteDevice(mac);
                    final BleDevice bleDevice = new BleDevice(bluetoothDevice, 0, null, 0);
                    Calendar c = Calendar.getInstance();
                    //c.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                    TimeZone tz = c.getTimeZone();
                    int time = (int) (c.getTimeInMillis() / 1000);
                    int gmtOffset = (int) (tz.getRawOffset() / 1000);
                    Log.i(TAG, "time: " + time + ", gmt : " + gmtOffset);

                    byte[] op = new byte[9];
                    op[0] = SYSCMD_SET_RTC;

                    ByteBuffer bb1 = ByteBuffer.wrap(new byte[4]);
                    bb1.order(ByteOrder.LITTLE_ENDIAN);
                    bb1.putInt(time);
                    ByteBuffer bb2 = ByteBuffer.wrap(new byte[4]);
                    bb2.order(ByteOrder.LITTLE_ENDIAN);
                    bb2.putInt(gmtOffset);

                    System.arraycopy(bb1.array(), 0, op, 1, 4);
                    System.arraycopy(bb2.array(), 0, op, 5, 4);
                    bManager.write(bleDevice, GENERAL_SERVICE_UUID.toString(), SYSCMD_CHAR_UUID.toString(), op,
                            new BleWriteCallback() {
                                @Override
                                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                    Log.i(TAG, "timeSet: 성공");
                                    //버전 가져오기
                                    bManager.read(bleDevice, DEVICE_INFORMATION_SERVICE_UUID.toString(), SW_REVISION_CHAR_UUID.toString(), new BleReadCallback() {
                                        @Override
                                        public void onReadSuccess(byte[] data) {
                                            bleDevice.setDeviceVersion(new String(data));
                                            //배터리 정보 가져오기
                                            bManager.read(bleDevice, BATTERY_SERVICE_UUID.toString(), BATTERY_CHAR_UUID.toString(), new BleReadCallback() {
                                                @Override
                                                public void onReadSuccess(byte[] data) {
                                                    binding.tvBattery.setText(data[0] + "%");
                                                    Log.i(TAG, "battery: " + data[0] + "%, version: " + bleDevice.getDeviceVersion());
                                                    //uuid확인
                                                    bManager.write(bleDevice, GENERAL_SERVICE_UUID.toString(), SYSCMD_CHAR_UUID.toString(), new byte[]{SYSCMD_GET_UUID},
                                                            new BleWriteCallback() {
                                                                @Override
                                                                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                                                    Log.i(TAG, "getUUID : " + "current : " + current + " total : " + total + " justWrite :" + HexUtil.formatHexString(justWrite));
                                                                    bManager.read(bleDevice, GENERAL_SERVICE_UUID.toString(), SYSCMD_CHAR_UUID.toString(), new BleReadCallback() {
                                                                        @Override
                                                                        public void onReadSuccess(byte[] data) {
                                                                            String uData = HexUtil.formatHexString(data);
                                                                            Log.i(TAG, "getUUID : " + "data : " + uData);
                                                                            //setUUID
                                                                            if(uData.contains("00000000000000000")){
                                                                                ManageDeviceViewModel.setUUID(bleDevice, activity);
                                                                            }
                                                                            else{ //서비스 시작
                                                                                Intent cmdService = new Intent(activity, CommandService.class);
                                                                                cmdService.setAction(ACTION_SET_CMD);
                                                                                cmdService.putExtra("Command", new Command(bleDevice.getMac(), userId, petSrn, getNameBySrn(petSrn)));
                                                                                Log.d(TAG, "Start Command Service with userId = " + userId + "  and petSrn" + petSrn);
                                                                                activity.startService(cmdService);
                                                                            }
                                                                        }
                                                                        @Override
                                                                        public void onReadFailure(BleException exception) {

                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onWriteFailure(BleException exception) {

                                                                }
                                                            });
                                                }
                                                @Override
                                                public void onReadFailure(BleException exception) {
                                                    Log.i(TAG, "battery read Fail");
                                                }
                                            });
                                        }

                                        @Override
                                        public void onReadFailure(BleException exception) {
                                            Log.d(TAG, "version read fail");
                                        }
                                    });
                                }

                                @Override
                                public void onWriteFailure(BleException exception) {
                                    Log.i(TAG, "timeSet: 실패");
                                }
                            });
                }
            }
        }.start();

    }


    public void loadDeviceDataNoSync(final String petSrn){
        new Thread() {
            @Override
            public void run() {
                super.run();
                final String mac = getMacBySrn(petSrn);
                if (mac == null || mac == "") {
                    for(UserDevice device : userDevices){
                        Log.d(TAG, "petSrn : " + device.getPetSrn() + " Mac : " + device.getMac() +" petNm : " + device.getPetNm());
                    }
                }
                else {
                    BluetoothDevice bluetoothDevice = bManager.getBluetoothAdapter().getRemoteDevice(mac);
                    final BleDevice bleDevice = new BleDevice(bluetoothDevice, 0, null, 0);
                    Calendar c = Calendar.getInstance();
                    //c.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                    TimeZone tz = c.getTimeZone();
                    int time = (int) (c.getTimeInMillis() / 1000);
                    int gmtOffset = (int) (tz.getRawOffset() / 1000);
                    Log.i(TAG, "time: " + time + ", gmt : " + gmtOffset);

                    byte[] op = new byte[9];
                    op[0] = SYSCMD_SET_RTC;

                    ByteBuffer bb1 = ByteBuffer.wrap(new byte[4]);
                    bb1.order(ByteOrder.LITTLE_ENDIAN);
                    bb1.putInt(time);
                    ByteBuffer bb2 = ByteBuffer.wrap(new byte[4]);
                    bb2.order(ByteOrder.LITTLE_ENDIAN);
                    bb2.putInt(gmtOffset);

                    System.arraycopy(bb1.array(), 0, op, 1, 4);
                    System.arraycopy(bb2.array(), 0, op, 5, 4);
                    bManager.write(bleDevice, GENERAL_SERVICE_UUID.toString(), SYSCMD_CHAR_UUID.toString(), op,
                            new BleWriteCallback() {
                                @Override
                                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                    Log.i(TAG, "timeSet: 성공");
                                    //버전 가져오기
                                    bManager.read(bleDevice, DEVICE_INFORMATION_SERVICE_UUID.toString(), SW_REVISION_CHAR_UUID.toString(), new BleReadCallback() {
                                        @Override
                                        public void onReadSuccess(byte[] data) {
                                            bleDevice.setDeviceVersion(new String(data));
                                            //배터리 정보 가져오기
                                            bManager.read(bleDevice, BATTERY_SERVICE_UUID.toString(), BATTERY_CHAR_UUID.toString(), new BleReadCallback() {
                                                @Override
                                                public void onReadSuccess(byte[] data) {
                                                    binding.tvBattery.setText(data[0] + "%");
                                                    Log.i(TAG, "battery: " + data[0] + "%, version: " + bleDevice.getDeviceVersion());
                                                    //uuid확인
                                                    bManager.write(bleDevice, GENERAL_SERVICE_UUID.toString(), SYSCMD_CHAR_UUID.toString(), new byte[]{SYSCMD_GET_UUID},
                                                            new BleWriteCallback() {
                                                                @Override
                                                                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                                                    Log.i(TAG, "getUUID : " + "current : " + current + " total : " + total + " justWrite :" + HexUtil.formatHexString(justWrite));
                                                                    bManager.read(bleDevice, GENERAL_SERVICE_UUID.toString(), SYSCMD_CHAR_UUID.toString(), new BleReadCallback() {
                                                                        @Override
                                                                        public void onReadSuccess(byte[] data) {
                                                                            String uData = HexUtil.formatHexString(data);
                                                                            Log.i(TAG, "getUUID : " + "data : " + uData);
                                                                            //setUUID
                                                                            if(uData.contains("00000000000000000")){
                                                                                ManageDeviceViewModel.setUUID(bleDevice, activity);
                                                                            }
                                                                        }
                                                                        @Override
                                                                        public void onReadFailure(BleException exception) {

                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onWriteFailure(BleException exception) {

                                                                }
                                                            });
                                                }
                                                @Override
                                                public void onReadFailure(BleException exception) {
                                                    Log.i(TAG, "battery read Fail");
                                                }
                                            });
                                        }

                                        @Override
                                        public void onReadFailure(BleException exception) {
                                            Log.d(TAG, "version read fail");
                                        }
                                    });
                                }

                                @Override
                                public void onWriteFailure(BleException exception) {
                                    Log.i(TAG, "timeSet: 실패");
                                }
                            });
                }
            }
        }.start();

    }




    public void initView(){
        ((AppCompatActivity)activity).setSupportActionBar(binding.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_mypage) {
                    Intent intent = new Intent(activity, MyPageActivity.class);
                    intent.putExtra("userId",userId);
                    activity.startActivity(intent);
                    activity.finish();
                } else if (id == R.id.nav_setting) {
                    Intent intent = new Intent(activity , SettingActivity.class);
                    intent.putExtra("userId",userId);
                    activity.startActivity(intent);
                } else if (id == R.id.nav_manage) {
                    Intent intent = new Intent(activity , ManagePetActivity.class);
                    intent.putExtra("userId",userId);
                    activity.startActivity(intent);
                    activity.finish();
                } else if (id == R.id.nav_device) {
                    Intent intent = new Intent(activity , ManageDeviceActivity.class);
                    intent.putExtra("userId",userId);
                    activity.startActivity(intent);
                    activity.finish();
                } else if (id == R.id.nav_logout) {
                    APIManager.getInstance(activity).logout(userId, new ResponseCallback() {
                        @Override
                        public void onError(int errorCode, String errorMsg) {
                            Log.d(TAG, "Logout 실패");
                        }

                        @Override
                        public void onDataReceived(JSONObject jsonResponse) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, R.string.success_logout,Toast.LENGTH_SHORT).show();
                                }
                            });
                            SharedPreferencesUtil.removeLoginInfo(activity);
                            Intent intent = new Intent(activity, LoginActivity.class);
                            activity.startActivity(intent);
                            activity.finishAffinity();
                        }

                        @Override
                        public void onReceiveResponse() {

                        }
                    });
                }

                binding.drawerLayout.closeDrawer(GravityCompat.START);


                return true;
            }
        });


        binding.swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshBatteryTextView(petSrn);
                initMainProficiency(petSrn, userId);
            }
        });
        binding.fabDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, DailyReportActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("petSrn", petSrn);
                intent.putExtra("petNm", (String)binding.spinnerNavSub.getSelectedItem());
                activity.startActivity(intent);
            }
        });
        binding.fabWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, WeekCompareActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("petSrn", petSrn);
                intent.putExtra("petNm", (String)binding.spinnerNavSub.getSelectedItem());
                activity.startActivity(intent);
            }
        });

    }



    @Override
    public void onCreate() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(activity, activity.getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
        }
        //동기화퍼센트
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isRunning = intent.getBooleanExtra("isRunning",false);
                //Log.d(TAG, "isRunning : " + isRunning);
                if(isRunning){
                    String syncPetSrn = intent.getStringExtra("petSrn");
                    int intProgress = intent.getIntExtra("progress",0);
                    intProgress--;
                    if(intProgress==-1)intProgress=0;
                    //Log.d(TAG, "sync pet : " + syncPetSrn);
                    //Log.d(TAG, "progress : " + intProgress);

                    binding.progress.setIndeterminate(true);
                    binding.progressBox.setVisibility(View.VISIBLE);
                    binding.pinkBox.setVisibility(View.GONE);
                    try {
                        for (int i = 0; i < petArray.length(); i++) {
                            JSONObject temp = petArray.getJSONObject(i);
                            if (temp.getString("petSrn").equals(syncPetSrn)) {
                                String nm = temp.getString("petNm");
                                binding.tvProgressInfo.setText(nm+activity.getString(R.string.sync)+ " " + intProgress + "%");
                                break;
                            }

                        }
                    }catch (Exception e){}

                }
                else{
                    binding.progress.setIndeterminate(false);
                    binding.progressBox.setVisibility(View.GONE);
                    binding.pinkBox.setVisibility(View.VISIBLE);
                    initMainProficiency(petSrn,userId);
                }

                // isRunning = 현재 데이터를 읽는 중인지 , true면 읽는중 false면 완료
                // petSrn = 현재 동기화중인 혹은 동기화를 끝낸 반려견
                // progress = 동기화 퍼센트
            }
        };
        IntentFilter filter = new IntentFilter(ACTION_VIEW_PROGRESS);
        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver,filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = activity.getWindow();
            //w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            w.setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
        }
        userId = activity.getIntent().getStringExtra("userId");
        ((TextView)binding.navView.getHeaderView(0).findViewById(R.id.nav_tv_user_id)).setText(userId);

        new Thread(){
            @Override
            public void run() {
                super.run();
                userDevices = APIManager.getInstance(activity).getUserDeviceList(userId);

                //펫 목록 확인
                APIManager.getInstance(activity).getMypetList(userId, getMyPetListCallback);

            }
        }.start();
        initView();

        binding.listView.setDividerHeight(0);
        binding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position > 3)
                    return;

                Log.d(TAG, "click position number : " + position);
                Intent intent = new Intent(activity, SubActivity.class);
                String[] targets = null;
                switch (position){
                    case 0 :
                    case 1 : targets = new String[]{"sun","uv","vit"};
                            break;
                    case 2 :
                    case 3 : targets = new String[]{"act","play","rest", "cal"};
                            break;
                }
                if(petData != null) {
                    intent.putExtra("targets", targets);
                    intent.putExtra("json", petData.toString());
                    intent.putExtra("userId",userId);
                    intent.putExtra("petSrn",petSrn);
                    intent.putExtra("petNm", (String)binding.spinnerNavSub.getSelectedItem());
                    activity.startActivity(intent);
                }
            }
        });
    }

    AppBarLayout.OnOffsetChangedListener abListner = new AppBarLayout.OnOffsetChangedListener() {
        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            binding.swipeView.setEnabled(verticalOffset == 0);
        }
    };

    @Override
    public void onResume() {
        binding.appBar.addOnOffsetChangedListener(abListner);
    }

    @Override
    public void onPause() {
        binding.appBar.removeOnOffsetChangedListener(abListner);
    }

    @Override
    public void onDestroy() {

    }


    public String getMacBySrn(String srn){
        if(userDevices==null)return null;
        for(UserDevice device : userDevices){
            if(device.getPetSrn().equals(srn)){
                if(device==null) return null;
                else return device.getMac();
            }
        }
        return "";
    }

    public String getNameBySrn(String srn){
        for(UserDevice device : userDevices){
            if(device.getPetSrn().equals(srn))
                return device.getPetNm();
        }
        return "";
    }

    public void initMainProficiency(final String petSrn, final String userId){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.progress.setIndeterminate(true);
                binding.progressBox.setVisibility(View.VISIBLE);
                binding.pinkBox.setVisibility(View.GONE);
            }
        });
        APIManager.getInstance(activity).getMyMainProficiency(userId, petSrn, new ResponseCallback() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                Log.d(TAG, "Main Data Receive Error");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.progress.setIndeterminate(false);
                        binding.progressBox.setVisibility(View.GONE);
                    }
                });

            }

            @Override
            public void onDataReceived(final JSONObject jsonResponse) {
                petData = jsonResponse;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        binding.progress.setIndeterminate(false);
                        binding.progressBox.setVisibility(View.GONE);
                        binding.pinkBox.setVisibility(View.VISIBLE);

                        binding.tvSync.setText(String.format(activity.getResources().getString(R.string.sync_time),
                                DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date())+ new SimpleDateFormat("HH:mm:ss").format(new Date())));
                        setHeartItem(petData);
                        binding.swipeView.setRefreshing(false);

                        APIManager.getInstance(activity).getMyMainAfter(userId, petSrn, new ResponseCallback() {
                            @Override
                            public void onError(int errorCode, String errorMsg) {
                                Log.d(TAG, "Main Data Receive Error");
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.progress.setIndeterminate(false);
                                        binding.progressBox.setVisibility(View.GONE);
                                    }
                                });
                            }

                            @Override
                            public void onDataReceived(JSONObject jsonResponse) {
                                petData = jsonResponse;
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.progress.setIndeterminate(false);
                                        binding.progressBox.setVisibility(View.GONE);
                                        binding.pinkBox.setVisibility(View.VISIBLE);
                                        setCardItem(petData);
                                        binding.swipeView.setRefreshing(false);
                                    }
                                });
                            }

                            @Override
                            public void onReceiveResponse() {

                            }
                        });
                    }
                });

            }

            @Override
            public void onReceiveResponse() {

            }
        });


    }

    public void setHeartItem(final  JSONObject petData){
        MentUtil mentUtil = MentUtil.getInstance(activity);
        if(petData != null) {
            JSONObject cData = petData.optJSONObject("cData");
            String target = "lovely";
            String loveRate = "F";
            if(cData.optInt("idx") != -1) {
                loveRate = cData.optString(target + "GradeText");
            }
            binding.tvBigHeart.setText(loveRate);
            binding.tvSmHeart.setText(loveRate);
            binding.tvHeartMent.setText(mentUtil.getMainLoveMent(spinnerAdapter.getItem(binding.spinnerNavSub.getSelectedItemPosition()), 0));
            binding.tvPinkMent.setText(String.format(activity.getResources().getString(R.string.home_ment)
                    , spinnerAdapter.getItem(binding.spinnerNavSub.getSelectedItemPosition())
                    , loveRate));
            binding.tvPinkMent.setText(mentUtil.getMainLoveCard(spinnerAdapter.getItem(binding.spinnerNavSub.getSelectedItemPosition()), loveRate));
        }
    }

    public void setCardItem(final JSONObject petData){
        if(petData != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final DataCardListAdapter adapter = new DataCardListAdapter(true, true, (String)binding.spinnerNavSub.getSelectedItem(), petData);
                    adapter.setDisplayItem(Arrays.asList(new String[]{"dep", "sun", "bark", "rest"}));
                    APIManager.getInstance(activity).getRankData(userId, petSrn, new ResponseCallback() {
                        @Override
                        public void onError(int errorCode, String errorMsg) {
                            adapter.setRankingMent(activity.getString(R.string.ranking_nodata_ment));
                            binding.listView.setAdapter(adapter);
                            binding.listView.invalidate();
                        }

                        @Override
                        public void onDataReceived(JSONObject jsonResponse) {
                            try{
                                String rate = jsonResponse.getJSONArray("cData").getJSONObject(0).getString("rate");
                                String grade = jsonResponse.getJSONArray("cData").getJSONObject(0).getString("lovelyGradeText");
                                if(grade.equals("D") || grade.equals("F"))
                                    adapter.setRankingMent(String.format(activity.getString(R.string.ranking_low_ment), (String)binding.spinnerNavSub.getSelectedItem()));
                                else
                                    adapter.setRankingMent(String.format(activity.getString(R.string.ranking_ment), (String)binding.spinnerNavSub.getSelectedItem(), rate));
                            }catch (Exception e){
                                Log.i(TAG, Log.getStackTraceString(e));
                                adapter.setRankingMent(activity.getString(R.string.ranking_nodata_ment));
                            }
                            binding.listView.setAdapter(adapter);
                            binding.listView.invalidate();
                        }

                        @Override
                        public void onReceiveResponse() {

                        }
                    });


                }
            });
        }
    }



    public void onBackPressed(){
        if ( pressedTime == 0 ) {
            Toast.makeText(activity, activity.getString(R.string.press_one_more_text) , Toast.LENGTH_LONG).show();
            pressedTime = System.currentTimeMillis();
        }
        else {
            int seconds = (int) (System.currentTimeMillis() - pressedTime);
            if ( seconds > 2000 ) {
                Toast.makeText(activity, activity.getString(R.string.press_one_more_text) , Toast.LENGTH_LONG).show();
                pressedTime = 0 ;
            }
            else {
                activity.finish();
//                finish(); // app 종료 시키기
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        activity.getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent=null;
        switch (item.getItemId()){
            case R.id.menu_share:{
                intent = new Intent(activity, FriendListActivity.class);
                break;
            }
            /*case R.id.menu_ranking:{
                intent = new Intent(activity, RankingActivity.class);
                break;
            }*/
        }

        intent.putExtra("userId", userId);
        activity.startActivity(intent);
        return true;
    }
}
