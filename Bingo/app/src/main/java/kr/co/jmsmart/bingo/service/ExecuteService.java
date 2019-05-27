package kr.co.jmsmart.bingo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.data.Command;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.data.Sleepdoc_10_min_data_type;
import kr.co.jmsmart.bingo.data.Sleepdoc_ext_interface_data_type;
public class ExecuteService extends Service {
    private static boolean isSyncStarted = false;
    private static String syncMac = "";

    private String TAG = "GET_DEVICE_DATA";

    private BleManager bManager;
    private BleDevice bleDevice;

    private ByteArrayOutputStream syncDataStream;
    private int count;
    private int totalSyncBytes;
    private String petSrn;
    private String userId;
    private String petNm;
    private ArrayList<Sleepdoc_10_min_data_type> dataList;

    private NotificationCompat.Builder builder;
    private NotificationManager notiManager;
    private NotificationChannel notificationChannel;

    private static String ACTION_GET_CMD = "bingo_action_get_cmd";
    private static String ACTION_VIEW_PROGRESS = "bingo_action_view_progress";
    private static String CHANNEL_ID = "BINGO_SERVICE_CHANNEL";
    private static String CHANNEL_NAME = "BINGO_EXECUTE_SERVICE";

    private UUID SYNC_SERVICE_UUID = UUID.fromString("0000fffa-0000-1000-8000-00805f9b34fb");//Sync Service UUID
    private UUID SYNC_CONTROL_CHAR_UUID = UUID.fromString("0000FFFA-0000-1000-8000-00805f9b34fb");//Sync characteristic1 UUID
    private UUID SYNC_DATA_CHAR_UUID = UUID.fromString("0000FFFB-0000-1000-8000-00805f9b34fb");//Sync characteristic2 UUID

    private static final int NOTIFICATION_ID = 999;

    private static final byte SYNC_CONTROL_START = 0x01;
    private static final byte SYNC_CONTROL_PREPARE_NEXT = 0x02;
    private static final byte SYNC_CONTROL_DONE = 0x03;

    private static final byte SYNC_NOTI_READY = 0x11;
    private static final byte SYNC_NOTI_NEXT_READY = 0x12;
    private static final byte SYNC_NOTI_DONE = 0x13;
    private static final byte SYNC_NOTI_ERROR = (byte)0xFF;

    BleWriteCallback syncControlWriteCallback = new BleWriteCallback() {
        @Override
        public void onWriteSuccess(int current, int total, byte[] justWrite) {
            Log.i("GET_DEVICE_DATA",  "write success, current: " + current
                    + " total: " + total
                    + " justWrite: " + HexUtil.formatHexString(justWrite, true));
        }
        @Override
        public void onWriteFailure(BleException exception) {
            Log.i("GET_DEVICE_DATA", "Fail\n"+exception.toString());
            Intent cast = new Intent(ACTION_VIEW_PROGRESS);
            cast.putExtra("isRunning",false);
            cast.putExtra("petSrn",petSrn);
            cast.putExtra("progress", 0);
            stopForeground(true);
            notiManager.cancelAll();
            LocalBroadcastManager.getInstance(ExecuteService.this).sendBroadcast(cast);
        }
    };

    BleReadCallback syncControlReadCallback = new BleReadCallback() {
        @Override
        public void onReadSuccess(byte[] values) {
            int len = values[0];
            if( len != 0 ) {
                syncDataStream.write(values, 1, len);
                Log.i(TAG, "Read Data len : " + len);
                Log.i("GET_DEVICE_DATA", "readDataSize :"+syncDataStream.size() + "  SleepdocDataSize : "+ Sleepdoc_ext_interface_data_type.size());

                if( syncDataStream.size() >= Sleepdoc_ext_interface_data_type.size() ) {
                    Sleepdoc_ext_interface_data_type extData;
                    byte[] stream = syncDataStream.toByteArray();
                    byte[] data = new byte[Sleepdoc_ext_interface_data_type.size()];
                    System.arraycopy(stream, stream.length-data.length, data, 0, data.length);
                    extData = new Sleepdoc_ext_interface_data_type(data);

                    if(totalSyncBytes == 0){
                        totalSyncBytes = extData.remainings * Sleepdoc_10_min_data_type.size();
                    }
                    if( syncDataStream.size() % Sleepdoc_ext_interface_data_type.size() == 0 ) {
                        Log.d(TAG,"sleepdoc_ext_interface_data_type 만큼 받음.");
                        for( int i=0; i<6; i++) {
                            Sleepdoc_10_min_data_type d = extData.d[i];
                            Log.d(TAG, String.format("  sleepdoc_10_min_data_type \t%d\t\t%d\t\t%d\t%d\t%d\t%d\t%d\t%d\t%d",
                                    d.s_tick, d.e_tick, d.steps, d.t_lux, d.avg_lux, d.avg_k, d.vector_x, d.vector_y, d.vector_z) );
                            Log.d(TAG, "device timezone: " + extData.time_zone);

                        }

                        for(int i = 0 ; i < 6; i++){
                            Sleepdoc_10_min_data_type min_data = extData.d[i];
                            dataList.add(min_data);
                            if(min_data.s_tick > 0 && min_data.s_tick != min_data.e_tick) {
                                APIManager.getInstance(ExecuteService.this).insertCompaionData(min_data, petSrn, userId, new ResponseCallback() {
                                    @Override
                                    public void onError(int errorCode, String errorMsg) {
                                        Log.d(TAG, "insert 에러 발생 : ");
                                    }

                                    @Override
                                    public void onDataReceived(JSONObject jsonResponse) {
                                        Log.d(TAG, "insert 성공");
                                    }

                                    @Override
                                    public void onReceiveResponse() {

                                    }
                                });
                            }
                        }

                    }
                }
                int progress = 100;
                if(totalSyncBytes > 0){
                    progress = (int) (syncDataStream.size() * 100 / totalSyncBytes);
                }
                if(progress < 0)
                    progress = 0;
                else if(progress > 100)
                    progress = 100;

                Intent cast = new Intent(ACTION_VIEW_PROGRESS);
                cast.putExtra("isRunning",true);
                cast.putExtra("petSrn",petSrn);
                cast.putExtra("progress", progress);

                builder.setProgress(100,progress,false);
                builder.setContentText("" + progress + "%");
                Notification noti = builder.build();
                notiManager.notify(NOTIFICATION_ID,noti);
                startForeground(999, noti);

                LocalBroadcastManager.getInstance(ExecuteService.this).sendBroadcast(cast);

                //다음 패킷 요청하기
                bManager.write(bleDevice, SYNC_SERVICE_UUID.toString(), SYNC_CONTROL_CHAR_UUID.toString(), new byte[]{SYNC_CONTROL_PREPARE_NEXT}, syncControlWriteCallback);
            }
            else if(isSyncStarted == true){
                Log.d("GET_DEVICE_DATA", "read End");
                byte[] stream = syncDataStream.toByteArray();
                Log.d(TAG, "onReadSuccess22: " + HexUtil.formatHexString(stream,true));
                bManager.write(bleDevice,SYNC_SERVICE_UUID.toString(), SYNC_CONTROL_CHAR_UUID.toString(), new byte[]{SYNC_CONTROL_DONE}, syncControlWriteCallback);
                isSyncStarted = false;
                Intent cmdService= new Intent(ExecuteService.this, CommandService.class);
                cmdService.setAction(ACTION_GET_CMD);
                startService(cmdService);

                Intent cast = new Intent(ACTION_VIEW_PROGRESS);
                cast.putExtra("isRunning",false);
                cast.putExtra("petSrn",petSrn);
                cast.putExtra("progress", 100);
                stopForeground(true);
                notiManager.cancelAll();
                LocalBroadcastManager.getInstance(ExecuteService.this).sendBroadcast(cast);
            }
        }
        @Override
        public void onReadFailure(BleException exception) {
            Log.i("GET_DEVICE_DATA", "read syncData Fail");
            isSyncStarted = false;
            Intent cmdService= new Intent(ExecuteService.this, CommandService.class);
            cmdService.setAction(ACTION_GET_CMD);
            startService(cmdService);
        }
    };


    public ExecuteService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bManager = BleManager.getInstance();
        totalSyncBytes = 0;
        dataList = new ArrayList<>();

        notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notiManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        }
        else{
            builder = new NotificationCompat.Builder(this);
        }
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
        builder.setSmallIcon(R.drawable.ic_launcher);
        //builder.setContentTitle(this.getString(R.string.notification_title));
        builder.setProgress(100,0,false);



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isSyncStarted = true;// read End에서 false
        Command cmd = (Command) intent.getSerializableExtra("Command");
        userId = cmd.getUserId();
        petSrn = cmd.getPetSrn();
        petNm = cmd.getPetNm();
        builder.setContentTitle(petNm + " " + getString(R.string.synchronizing));
        syncMac = cmd.getMac();

        if(bManager.isConnected(cmd.getMac())){
            BluetoothDevice bluetoothDevice = bManager.getBluetoothAdapter().getRemoteDevice(cmd.getMac());
            bleDevice = new BleDevice(bluetoothDevice,0,null,0);
            syncDataStream = new ByteArrayOutputStream();
            count = 0;

            ///////////////////////////////////////////// 이걸해줘야 notification이 제대로 온다
            BluetoothGatt gatt = bManager.getBluetoothGatt(bleDevice);
            BluetoothGattCharacteristic syncControlChar = gatt.getService(SYNC_SERVICE_UUID).getCharacteristic(SYNC_CONTROL_CHAR_UUID);
            //SYNC_CONTROL Notify ON
            final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
            BluetoothGattDescriptor descriptor = syncControlChar.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
            /////////////////////////////////////////////

            bManager.notify(bleDevice, SYNC_SERVICE_UUID.toString(), SYNC_CONTROL_CHAR_UUID.toString(), new BleNotifyCallback() {
                //Sync Notify ON Success
                @Override
                public void onNotifySuccess() {
                    Log.i("GET_DEVICE_DATA", "notify SYNC_CONTROL Success");
                    //노티 켜지면 씽크시작 값 쓰기
                    bManager.write(bleDevice, SYNC_SERVICE_UUID.toString(), SYNC_CONTROL_CHAR_UUID.toString(), new byte[]{SYNC_CONTROL_START}, syncControlWriteCallback);
                }

                @Override
                public void onNotifyFailure(BleException exception) {
                    Log.i("GET_DEVICE_DATA", "notify SYNC_CONTROL Fail");
                }
                @Override
                public void onCharacteristicChanged(byte[] data) {
                    Log.i("notify SYNC_CONTROL", HexUtil.formatHexString(data, true));
                    if(data[0]!=SYNC_NOTI_DONE) {
                        bManager.read(bleDevice, SYNC_SERVICE_UUID.toString(), SYNC_DATA_CHAR_UUID.toString(), syncControlReadCallback);
                    }
                }
            });
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean getIsSyncStarted(){
        return isSyncStarted;
    }

    public static String getSyncMac(){ return syncMac; }
}
