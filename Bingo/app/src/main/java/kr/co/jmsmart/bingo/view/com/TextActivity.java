package kr.co.jmsmart.bingo.view.com;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import kr.co.jmsmart.bingo.R;

public class TextActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        progressDialog = new ProgressDialog(this);
        String title = getIntent().getStringExtra("title");
        setTitle(title);
        WebView web = (WebView) findViewById(R.id.webview);
        web.getSettings().setJavaScriptEnabled(true);
        web.setHorizontalScrollBarEnabled(false);
        web.setVerticalScrollBarEnabled(false);
        web.setBackgroundColor(0);
        progressDialog.show();
        String source = "";
        if(title.equals(getString(R.string.terms_conditions)))
            source = getString(R.string.terms_conditions_text1)+getString(R.string.terms_conditions_text2)+getString(R.string.terms_conditions_text3)+getString(R.string.terms_conditions_text4)+getString(R.string.terms_conditions_text5)+getString(R.string.terms_conditions_text6);
        else source = getString(R.string.privacy_policy_text1)+getString(R.string.privacy_policy_text2)+getString(R.string.privacy_policy_text3)+getString(R.string.privacy_policy_text4);
        web.loadData(source, "text/html", "UTF-8");
        progressDialog.dismiss();
    }

    public void onClick(View v){
        finish();
    }
}
