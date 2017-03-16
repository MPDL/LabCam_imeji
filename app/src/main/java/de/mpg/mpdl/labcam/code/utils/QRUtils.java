package de.mpg.mpdl.labcam.code.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by yingli on 11/22/16.
 */

public class QRUtils {

    public static parseQRResult processQRCode(Intent data, Activity activity, String LOG_TAG, String apiKey) {
        {
            Bundle bundle = data.getExtras();
            String QRText = bundle.getString("QRText");
            Log.v(LOG_TAG, QRText);
            String APIkey = "";           // APIkey get from QR code
            String url = "";
            String qrCollectionId = "";

            try {
                JSONObject jsonObject = new JSONObject(QRText);
                APIkey = jsonObject.getString("key");
                if (apiKey != null && !apiKey.equalsIgnoreCase("")) {  // login activity don't check apiKey
                    if (!apiKey.equals(APIkey)) {
                        Toast.makeText(activity, "this folder doesn't look like yours", Toast.LENGTH_LONG).show();
                        return new parseQRResult(APIkey, qrCollectionId);
                    }
                }
                url = jsonObject.getString("col");
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(activity, "parse QR code failed, please scan again", Toast.LENGTH_LONG).show();
                return new parseQRResult(APIkey, qrCollectionId);
            }

            URL u = null;
            try {
                u = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            String path = u.getPath();

            if (path != null) {  // parse url and get collectionId
                if (!path.contains("/")) { // wrong url
                    Toast.makeText(activity, "qrCode not legal", Toast.LENGTH_LONG).show();
                    return new parseQRResult(APIkey, qrCollectionId);
                }

                try {
                    qrCollectionId = path.substring(path.lastIndexOf("/") + 1);
                    Log.i(LOG_TAG, qrCollectionId);
                    return new parseQRResult(APIkey, qrCollectionId);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    Toast.makeText(activity, "qrCode not legal", Toast.LENGTH_LONG).show();
                    return new parseQRResult(APIkey, qrCollectionId);
                }

            }else {
                return new parseQRResult(APIkey, qrCollectionId);
            }
        }
    }

    public static class parseQRResult{
        String APIkey = "";
        String qrCollectionId = "";

        public parseQRResult(String APIkey, String qrCollectionId) {
            this.APIkey = APIkey;
            this.qrCollectionId = qrCollectionId;
        }

        public String getAPIkey() {
            return APIkey;
        }

        public String getQrCollectionId() {
            return qrCollectionId;
        }
    }
}