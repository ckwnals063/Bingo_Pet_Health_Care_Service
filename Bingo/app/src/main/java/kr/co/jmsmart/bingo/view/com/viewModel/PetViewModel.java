package kr.co.jmsmart.bingo.view.com.viewModel;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.databinding.ActivityPetBinding;
import kr.co.jmsmart.bingo.util.APIManager;
import kr.co.jmsmart.bingo.data.Pet;
import kr.co.jmsmart.bingo.util.DownloadImageTask;
import kr.co.jmsmart.bingo.util.SharedPreferencesUtil;
import kr.co.jmsmart.bingo.view.base.CommonModel;
import kr.co.jmsmart.bingo.view.com.DeviceScanActivity;

/**
 * Created by ZZQYU on 2019-01-09.
 */

public class PetViewModel implements CommonModel{
    private final String TAG = "PetViewModel";
    private Activity activity;
    private ActivityPetBinding binding;
    private String userId;
    private int type;

    private String btnText;

    private Date selectDate;
    static final int DATE_DIALOG_ID = 999;

    public static final int TYPE_ADD = 0;
    public static final int TYPE_EDIT = 1;
    private static final String ACTION_REFRESH_PET_LIST = "refreshPetListAction";

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;

    private String petName , petType, weightKg ,weightLb ,petBirth;
    private char sex ;

    private int petSrn=-1;
    private String fileCode=null;

    private Bitmap bitmap;
    private Uri mImageCaptureUri;

    ArrayList<APIManager.PetType> petTypes;
    ArrayList<String> petTypeNames;

    public PetViewModel(Activity activity, ActivityPetBinding binding, String userId, int type){
        this.activity = activity;
        this.binding = binding;
        this.userId = userId;
        this.type = type;
        Resources r =  activity.getResources();

        btnText = type == TYPE_ADD ?r.getString(R.string.add):r.getString(R.string.edit);
        ((AppCompatActivity)activity).setTitle(
                type == TYPE_ADD ?
                        r.getString(R.string.add_pet)
                        :r.getString(R.string.edit_pet));
    }

    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                Date date = new Date();
                return new DatePickerDialog(activity, datePickerListener, date.getYear(), date.getMonth(), date.getDay());
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int year, int month, int day) {
            selectDate = new Date(year, month, day);
            String s = DateFormat.getDateInstance(DateFormat.SHORT).format(selectDate);
            binding.editPetDate.setText(s);
        }
    };



    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == binding.btnNext){

                //웹서버에 정보 추가
                petName = binding.editPetName.getText().toString();
                sex = binding.radioPet.getCheckedRadioButtonId() == R.id.radio_pet_male ? 'M' : 'F';
                petBirth = binding.editPetDate.getText().toString();

                if(petName.length() < 1 || petBirth.length() < 1 || binding.editPetWeight.getText().toString().length() < 1 || !isValidDateStr(petBirth)){
                    Toast.makeText(activity,activity.getString(R.string.pet_info_input_error),Toast.LENGTH_SHORT).show();
                }
                if(binding.spinnerWeight.getSelectedItemPosition()==0){
                    weightKg = binding.editPetWeight.getText().toString();
                }
                else {
                    weightLb = binding.editPetWeight.getText().toString();
                }

                Log.d("addPet", "PetViewModel: petBirth =" + petBirth);
                Log.d("addPet", "PetViewModel: petWeight =" + binding.editPetWeight.getText().toString());
                Log.d("addPet", "PetViewModel: weightKg =" + weightKg);
                Log.d("addPet", "PetViewModel: weightLb =" + weightLb);
                switch (type){
                    case TYPE_ADD:{
                        APIManager.getInstance(activity).newCompanionPet(petName,
                                petType,
                                weightKg,
                                weightLb,
                                sex,
                                petBirth,
                                userId,
                                new ResponseCallback() {
                                    @Override
                                    public void onError(int errorCode, String errorMsg) {
                                        Toast.makeText(activity, R.string.fail_pet_data,Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onDataReceived(JSONObject jsonResponse) {
                                        try {
                                            uploadProfile(jsonResponse.getString("petSrn"));
                                        }catch (Exception e){}
                                        Intent bIntent = new Intent(ACTION_REFRESH_PET_LIST);
                                        LocalBroadcastManager.getInstance(activity).sendBroadcast(bIntent);
                                        try {
                                            Intent intent = new Intent(activity, DeviceScanActivity.class);
                                            intent.putExtra("petId", jsonResponse.getString("petSrn"));
                                            intent.putExtra("userId", userId);
                                            intent.putExtra("type",DeviceScanViewModel.TYPE_DEVICE_INSERT);
                                            activity.startActivity(intent);
                                        }catch (Exception e){
                                            Log.i("PetViewModel", Log.getStackTraceString(e));
                                        }
                                        activity.finish();
                                    }

                                    @Override
                                    public void onReceiveResponse() {

                                    }
                                });
                        break;
                    }
                    case TYPE_EDIT:{
                        APIManager.getInstance(activity).updateCompanionPet(
                                petSrn+"",
                                petName,
                                petType,
                                weightKg,
                                weightLb,
                                sex,
                                petBirth,
                                userId,
                                new ResponseCallback() {
                                    @Override
                                    public void onError(int errorCode, String errorMsg) {
                                        Toast.makeText(activity, R.string.fail_pet_data,Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onDataReceived(JSONObject jsonResponse) {
                                        uploadProfile(petSrn+"");
                                        Intent bIntent = new Intent(ACTION_REFRESH_PET_LIST);
                                        LocalBroadcastManager.getInstance(activity).sendBroadcast(bIntent);
                                        activity.setResult(1);
                                        activity.finish();
                                    }

                                    @Override
                                    public void onReceiveResponse() {

                                    }
                                });
                        break;

                    }
                }

            }
        }
    };

    private void initPetInfo(){
        if(type == TYPE_EDIT){

            Pet pet = (Pet)activity.getIntent().getSerializableExtra("pet");
            petSrn = pet.getId();
            fileCode = pet.getFileCode();
            if(fileCode!=null){
                new DownloadImageTask(binding.imgDog).execute(APIManager.getProfileUrl(fileCode));
            }
            binding.editPetName.setText(pet.getPetNm());
            binding.spinnerPetType.setSelection(petTypeNames.indexOf(pet.getBreedNm()));
            if(pet.getSex() == 'M') {
                binding.radioPetMale.setChecked(true);
                binding.radioPetFemale.setChecked(false);
            }
            else {
                binding.radioPetMale.setChecked(false);
                binding.radioPetFemale.setChecked(true);
            }
            if(binding.spinnerWeight.getSelectedItemPosition()==0)
                binding.editPetWeight.setText(pet.getKg()+"");
            else binding.editPetWeight.setText(pet.getLb()+"");

            try {
                binding.editPetDate.setText(pet.getBirth());
            }catch (Exception e){}
        }
    }


    @Override
    public void onCreate() {

        if(type == TYPE_EDIT){
            binding.editPetName.setClickable(false);
            binding.editPetName.setFocusableInTouchMode(false);

            binding.spinnerPetType.setEnabled(false);
            binding.spinnerWeight.setEnabled(false);

            binding.editPetDate.setClickable(false);
            binding.editPetDate.setFocusableInTouchMode(false);

            binding.radioPetFemale.setClickable(false);
            binding.radioPetMale.setClickable(false);
        }

        binding.editPetDate.setOnClickListener(listener);

        binding.btnNext.setText(btnText);
        binding.btnNext.setOnClickListener(listener);

        petTypes = APIManager.getInstance(activity).getPetTypeList();
        petTypeNames = getPetTypeList(petTypes);

        binding.spinnerPetType.setAdapter(new ArrayAdapter<String>(activity,R.layout.support_simple_spinner_dropdown_item, petTypeNames));

        binding.spinnerWeight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    weightKg = binding.editPetWeight.getText().toString();
                    weightLb = "";
                }
                else{
                    weightKg = "";
                    weightLb = binding.editPetWeight.getText().toString();
                }
                SharedPreferencesUtil.setDefaultUnit(activity, (String)parent.getAdapter().getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.spinnerPetType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                petType = petTypes.get(position).getPetCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.spinnerWeight.setSelection(0);
        if(SharedPreferencesUtil.isExistDefaultUnit(activity))
            binding.spinnerWeight.setSelection(SharedPreferencesUtil.getDefaultUnit(activity).equals("Kg")?0:1);

        binding.picBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlbum();
            }
        });

        //강아지 정보 세팅하기
        initPetInfo();
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
    public ArrayList<String> getPetTypeList(ArrayList<APIManager.PetType> types){
        ArrayList<String> result = new ArrayList<>();

        for(APIManager.PetType type : types){
            result.add(type.getPetNm());
        }

        return result;
    }
    public void openAlbum(){
        bitmap = null;
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        activity.startActivityForResult(intent, PICK_FROM_ALBUM);
    }
    public void uploadProfile(String petSrn){
        if(bitmap!=null)
            APIManager.getInstance(activity).uploadProfile(userId, petSrn, bitmap, new ResponseCallback() {
                @Override
                public void onError(int errorCode, String errorMsg) {
                    Log.i(TAG, "[errorCode] "+errorCode + " [errorMsg]" + errorMsg);
                }

                @Override
                public void onDataReceived(JSONObject jsonResponse) {
                    Log.i(TAG, "[uploadProfile] "+jsonResponse);
                }

                @Override
                public void onReceiveResponse() {

                }
            });
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode != Activity.RESULT_OK)
            return;
        switch(requestCode) {
            case PICK_FROM_ALBUM: {
                mImageCaptureUri = data.getData();
            }
            case PICK_FROM_CAMERA: {
                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정합니다.
                // 이후에 이미지 크롭 어플리케이션을 호출하게 됩니다.
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");
                // CROP할 이미지를 200*200 크기로 저장
                intent.putExtra("outputX", 480); // CROP한 이미지의 x축 크기
                intent.putExtra("outputY", 400); // CROP한 이미지의 y축 크기
                intent.putExtra("aspectX", 6); // CROP 박스의 X축 비율
                intent.putExtra("aspectY", 5); // CROP 박스의 Y축 비율
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                activity.startActivityForResult(intent, CROP_FROM_IMAGE); // CROP_FROM_CAMERA case문 이동
                break;
            }
            case CROP_FROM_IMAGE: {
                if(resultCode != Activity.RESULT_OK) {
                    return;
                }
                final Bundle extras = data.getExtras();

                if(extras != null) {
                    Bitmap photo = extras.getParcelable("data"); // CROP된 BITMAP
                    binding.imgDog.setImageBitmap(photo); // 레이아웃의 이미지칸에 CROP된 BITMAP을 보여줌
                    bitmap = photo;
                    break;
                }

            }
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
