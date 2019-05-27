package kr.co.jmsmart.bingo.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by ZZQYU on 2019-02-22.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    View view = null;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }
    public DownloadImageTask(ImageView bmImage, View view) {
        this.bmImage = bmImage;
        this.view = view;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
        if(view!=null)view.setBackground(new BitmapDrawable(view.getResources(), result));
    }
}
