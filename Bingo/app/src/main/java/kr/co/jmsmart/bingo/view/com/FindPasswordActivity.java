package kr.co.jmsmart.bingo.view.com;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.wapplecloud.libwapple.ResponseCallback;

import org.json.JSONObject;

import kr.co.jmsmart.bingo.R;
import kr.co.jmsmart.bingo.util.APIManager;

public class FindPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);
        setTitle(getString(R.string.find_password));
    }
    public void onYesClick(View v){
        EditText edit = findViewById(R.id.edit_ID);

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.show();
        APIManager.getInstance(this).findPassword(edit.getText().toString(), new ResponseCallback() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                dialog.cancel();
                Toast.makeText(getBaseContext(),getString(R.string.find_password_error),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDataReceived(JSONObject jsonResponse) {
                dialog.cancel();
                Toast.makeText(getBaseContext() ,getString(R.string.find_password_ok),Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onReceiveResponse() {

            }
        });
    }
    public void onNoClick(View v){
        finish();
    }
}
