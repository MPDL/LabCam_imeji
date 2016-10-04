package de.mpg.mpdl.labcam.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by soultice on 02-Oct-16.
 */

public class OCRtextHandler {

    public static String getText(Context innercontext, Bitmap bitmap) {
        Frame newFrame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();
        //initialize Textrecognizer
        TextRecognizer textRecognizer = new TextRecognizer.Builder(innercontext).build();
        //parse text
        SparseArray<TextBlock> currentText = textRecognizer.detect(newFrame);

        if (!textRecognizer.isOperational()) {
            Log.e(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
//            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;
//
//            if (hasLowStorage) {
//                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
//                Log.w(TAG, getString(R.string.low_storage_error));
           }
        //Add text to an arraylist
        ArrayList<String> fullText = new ArrayList<String>();
        if (currentText.size() == 0) {
            fullText.add("No text found");
        }
        for (int i = 0; i < currentText.size(); ++i) {
            TextBlock item = currentText.valueAt(i);
            if (item != null && item.getValue() != null) {
                fullText.add(item.getValue());
            }
        }
        //Convert arraylist to stringbuilder
        StringBuilder sb = new StringBuilder();
        for (String s : fullText) {
            sb.append(s);
            sb.append("\t");
        }
        String message = sb.toString();
        return message;
    }
}