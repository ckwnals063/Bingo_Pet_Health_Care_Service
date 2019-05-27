package kr.co.jmsmart.bingo.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.clj.fastble.BleManager;

import org.apache.commons.codec.binary.StringUtils;

import java.util.ArrayList;

import kr.co.jmsmart.bingo.data.Command;

public class CommandService extends IntentService {

    private String TAG = "GET_DEVICE_DATA";
    private static String ACTION_SET_CMD = "bingo_action_set_cmd";
    private static String ACTION_GET_CMD = "bingo_action_get_cmd";
    private static String ACTION_VIEW_PROGRESS = "bingo_action_view_progress";

    private static ArrayList<Command> cmdQue = new ArrayList<>();
    public CommandService() {
        super("CommandService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if(action.equals(ACTION_SET_CMD)){
            setCmd((Command) intent.getSerializableExtra("Command"));
            Log.d(TAG, "onHandleIntent: " + cmdQue.size() + "  IsSyncStarted ? " + ExecuteService.getIsSyncStarted());
            if(cmdQue.size() == 1 && ExecuteService.getIsSyncStarted() == false){
                getCmd();
            }
        }
        else if(action.equals(ACTION_GET_CMD)){
            getCmd();
        }
    }

    public void setCmd(Command cmd){
        if(!isDuplicated(cmd.getMac())) cmdQue.add(cmd);
        else Log.d(TAG, "Is Duplicated");
    }

    public void getCmd(){
        if(cmdQue.size() > 0) {
            Command cmd = cmdQue.get(0);
            cmdQue.remove(0);
            if(!TextUtils.isEmpty(cmd.getMac()) && BleManager.getInstance().isConnected(cmd.getMac())) {
                Intent executeService = new Intent(this, ExecuteService.class);
                executeService.putExtra("Command", cmd);
                startService(executeService);
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean isDuplicated(String mac){
        boolean result = false;
        for(int i=0; i<cmdQue.size() && !result ; i++){
            if(TextUtils.equals(cmdQue.get(i).getMac(), mac) || TextUtils.equals(ExecuteService.getSyncMac(), mac))
                result = true;
        }
        return result;
    }

    public int getCmdSize(){
        return cmdQue.size();
    }
}
