package example.com.mpdlcamera.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.squareup.picasso.UrlConnectionDownloader;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by yingli on 4/11/16.
 */
public class camPicassoLoader extends UrlConnectionDownloader {

    String apiKey;
    String username;
    String password;
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