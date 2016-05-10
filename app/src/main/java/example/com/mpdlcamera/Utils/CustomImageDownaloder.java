package example.com.mpdlcamera.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Created by yingli on 5/9/16.
 */
public class CustomImageDownaloder extends BaseImageDownloader {

    String apiKey;
    SharedPreferences mPrefs;
    public CustomImageDownaloder(Context context) {
        super(context);
        //user info
        mPrefs = context.getSharedPreferences("myPref", 0);
        apiKey = mPrefs.getString("apiKey", "");
        Log.e("CustomImageDownaloder",apiKey);
    }

    public CustomImageDownaloder(Context context, int connectTimeout, int readTimeout) {
        super(context, connectTimeout, readTimeout);
    }

    @Override
    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
        HttpURLConnection conn = super.createConnection(url, extra);
        conn.setRequestProperty("Authorization", "Bearer "+apiKey);
        return conn;
    }
}