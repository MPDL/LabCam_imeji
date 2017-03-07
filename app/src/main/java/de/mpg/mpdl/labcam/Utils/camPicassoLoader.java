package de.mpg.mpdl.labcam.Utils;

import android.content.Context;
import android.net.Uri;

import com.squareup.picasso.UrlConnectionDownloader;

import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by yingli on 4/11/16.
 */
public class camPicassoLoader extends UrlConnectionDownloader {

    String apiKey;
    public camPicassoLoader(Context context) {
        super(context);
        apiKey = PreferenceUtil.getString(context, Constants.SHARED_PREFERENCES, Constants.API_KEY, "");
    }



    @Override
    protected HttpURLConnection openConnection(Uri path) throws IOException {
        HttpURLConnection c = super.openConnection(path);
        c.setRequestProperty("Authorization", "Bearer "+apiKey);
        return c;
    }
}