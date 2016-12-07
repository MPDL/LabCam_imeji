package de.mpg.mpdl.labcam.Utils.UiElements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


/**
 * Created by soultice on 02-Oct-16.
 */

public class OCRtextHandler {

    public static String getText(Context innercontext, Bitmap bitmap) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        Bitmap compressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

        Frame newFrame = new Frame.Builder()
                .setBitmap(compressed)
                .build();
        //initialize Textrecognizer
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(innercontext).build();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(innercontext).build();
        //parse text
        MultiDetector multiDetector = new MultiDetector.Builder()
                .add(barcodeDetector)
                .add(textRecognizer)
                .build();

        SparseArray<TextBlock> currentText = textRecognizer.detect(newFrame);
        Log.e("TEXTREC", Integer.toString(currentText.size()));

        SparseArray<Object> objectSparseArray = multiDetector.detect(newFrame);
        Log.e("COMBINEREC", Integer.toString(objectSparseArray.size()));

        ArrayList<String> fullText = new ArrayList<String>();

        for (int i = 0; i < objectSparseArray.size(); i++){
            Object currObject = objectSparseArray.valueAt(i);
            if(currObject instanceof TextBlock){
                TextBlock item = (TextBlock) currObject;
                if (item != null && item.getValue() != null) {
                    fullText.add(item.getValue());
                }
                Log.e("IS TEXTBLOCK", item.getValue());

            } else if(currObject instanceof Barcode){

                Barcode item = (Barcode) currObject;
                if (item != null && item.displayValue != null) {
                    fullText.add("\nBARCODE: " + item.displayValue);
                }
                Log.e("IS BARCODE", item.displayValue);
            }
        }
        multiDetector.release();
        textRecognizer.release();
        barcodeDetector.release();

        StringBuilder sb = new StringBuilder();
        for (String s : fullText) {
            sb.append(s);
            sb.append("\t");
        }
        String message = sb.toString();
        return message;

    }
}