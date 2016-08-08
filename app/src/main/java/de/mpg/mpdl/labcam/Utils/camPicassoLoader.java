package de.mpg.mpdl.labcam.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.squareup.picasso.UrlConnectionDownloader;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by yingli on 4/11/16.
 */
public class camPicassoLoader extends UrlConnectionDownloader {

    String apiKey;
    SharedPreferences mPrefs;
    public camPicassoLoader(Context context) {
        super(context);
        //user info
        mPrefs = context.getSharedPreferences("myPref", 0);
        apiKey = mPrefs.getString("apiKey","");
    }



    @Override
    protected HttpURLConnection openConnection(Uri path) throws IOException {
        HttpURLConnection c = super.openConnection(path);
        c.setRequestProperty("Authorization", "Bearer "+apiKey);
        return c;
    }
}