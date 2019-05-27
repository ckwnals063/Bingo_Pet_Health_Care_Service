package kr.co.jmsmart.bingo.adapter;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;

import java.util.ArrayList;
import java.util.List;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.data.UserDevice;
import kr.co.jmsmart.bingo.util.APIManager;

public class DeviceAdapter extends BaseAdapter {

    private Context context;
    private List<BleDevice> bleDeviceList;
    private ArrayList<UserDevice> userDevices;

    public DeviceAdapter(Context context, String userId) {
        this.context = context;
        bleDeviceList = new ArrayList<>();
        userDevices = APIManager.getInstance(context).getUserDeviceList(userId);
    }

    public void addDevice(BleDevice bleDevice) {
        removeDevice(bleDevice);
        bleDeviceList.add(bleDevice);
    }

    public void removeDevice(BleDevice bleDevice) {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (bleDevice.getKey().equals(device.getKey())) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearConnectedDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (BleManager.getInstance().isConnected(device)) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearScanDevice() {
        bleDeviceList.clear();
    }

    public void clear() {
        clearConnectedDevice();
        clearScanDevice();
    }

    @Override
    public int getCount() {
        return bleDeviceList.size();
    }

    @Override
    public BleDevice getItem(int position) {
        if (position > bleDeviceList.size())
            return null;
        return bleDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(context, R.layout.adapter_device, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.img_blue = (ImageView) convertView.findViewById(R.id.img_blue);
            holder.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
            holder.txt_mac = (TextView) convertView.findViewById(R.id.txt_mac);
            holder.btn_connect = convertView.findViewById(R.id.btn_connect);
        }

        final BleDevice bleDevice = getItem(position);
        if (bleDevice != null) {
            boolean isConnected = BleManager.getInstance().isConnected(bleDevice);
            String name = bleDevice.getName();
            if(userDevices!=null){
                for(UserDevice ud:userDevices){
                    if(ud.getMac().equals(bleDevice.getMac())){
                        name = ud.getPetNm() +context.getString(R.string.of) + "Bingo";
                    }
                }
            }
            String mac = bleDevice.getMac();
            int rssi = bleDevice.getRssi();
            holder.txt_name.setText(name);
            holder.txt_mac.setText(mac);

        }



        return convertView;
    }

    class ViewHolder {
        ImageView img_blue;
        TextView txt_name;
        TextView txt_mac;
        CardView btn_connect;
    }



}
