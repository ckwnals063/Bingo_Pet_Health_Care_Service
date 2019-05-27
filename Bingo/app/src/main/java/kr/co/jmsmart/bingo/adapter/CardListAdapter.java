package kr.co.jmsmart.bingo.adapter;


import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.clj.fastble.BleManager;

import java.util.List;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.data.UserDevice;
import kr.co.jmsmart.bingo.databinding.AdapterCardItemBinding;
import kr.co.jmsmart.bingo.data.Pet;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.util.DownloadImageTask;

public class CardListAdapter extends BaseAdapter {
    private Activity activity;
    private List<Item> itemList;
    private Intent intent;

    public CardListAdapter(Activity activity, List<Item> itemList) {
        this.activity = activity;
        this.itemList = itemList;
        this.intent = intent;
    }

    public void addItem(Item item) {
        itemList.add(item);
    }

    public void removeItem(Item item) {
        itemList.remove(item);
    }
    public void removeItem(int itemPosition) {
        itemList.remove(itemPosition);
    }

    public void clear(){
        itemList.clear();
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Item getItem(int position) {
        if (position > itemList.size())
            return null;
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AdapterCardItemBinding binding;
        if (convertView != null) {
            binding = (AdapterCardItemBinding) convertView.getTag();
        } else {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            binding = DataBindingUtil.inflate(inflater, R.layout.adapter_card_item, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        }
        Item item = itemList.get(position);
        binding.cardTitle.setText(item.getTitle());
        binding.cardText.setText(item.getSubText());
        binding.cardStatus.setVisibility(item.isConnected()?View.VISIBLE:View.GONE);

        Pet p = item.getPet();
        if(p!=null && p.getFileCode()!=null){
            new DownloadImageTask(binding.cardImg).execute(APIManager.getProfileUrl(p.getFileCode()));
        }
        else{
            binding.cardImg.setImageResource(R.drawable.default_dog);
        }

        return convertView;
    }


    public static class Item{
        private int id;
        private String title;
        private String subText;

        private boolean isConnected;

        private Pet pet=null;

        private UserDevice device;

        private Item(int id, String title, String subText, boolean isConnected) {
            this.id = id;
            this.title = title;
            this.subText = subText;
            this.isConnected = isConnected;
        }
        public Item(UserDevice device, String of) {
            this(Integer.parseInt(device.getPetSrn()),
                    device.getPetNm()+ of + " Bingo",
                    TextUtils.isEmpty(device.getMac()) ? "터치하여 새로운 기기 연결" : device.getMac(),
                    BleManager.getInstance().isConnected(device.getMac()));
            this.device = device;
        }
        public Item(Pet pet) {
            this.pet = pet;
            this.id = pet.getId();
            this.title = pet.getTitle();
            this.subText = pet.getSubText();
            this.isConnected = false;
        }

        public Pet getPet() {
            return pet;
        }
        public UserDevice getDevice(){return device;}
        public void setDevice(UserDevice device){
            this.device = device;
            this.id = Integer.parseInt(device.getPetSrn());
            this.subText = TextUtils.isEmpty(device.getMac()) ? "터치하여 새로운 기기 연결" : device.getMac();
            this.isConnected = BleManager.getInstance().isConnected(device.getMac());
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }


        public String getSubText(){
            return subText;
        }

        public void setSubText(String subText){
            this.subText = subText;
        }

        public void setConnected(boolean isConnected){
            this.isConnected = isConnected;
        }

        public boolean isConnected() {
            return isConnected;
        }
    }

}
