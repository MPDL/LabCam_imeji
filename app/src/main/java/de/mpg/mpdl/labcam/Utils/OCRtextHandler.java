package de.mpg.mpdl.labcam.Utils;



import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.graphics.ImageFormat;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.nio.ByteBuffer;
import java.util.ArrayList;


/**
 * Created by soultice on 02-Oct-16.
 */

public class OCRtextHandler {

    public static String getText(Context innercontext, Bitmap bitmap) {

        if (bitmap == null){
            Log.e("Error: ", "Bitmap size is zero");
        }

        Log.e("Starting:", "BufferConfiguration");
        int bytes = bitmap.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer);

        Log.e("Starting:", "FrameBuilder");
        Frame newFrame = new Frame.Builder()
                .setImageData(buffer, bitmap.getWidth(), bitmap.getHeight(), ImageFormat.NV21)
                //.setBitmap(bitmap)
                .build();
        //initialize TextRecognizer and BarcodeDetector
        Log.e("Starting:", "DetectorInit");
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(innercontext).build();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(innercontext).build();
        MultiDetector multiDetector = new MultiDetector.Builder()
                .add(barcodeDetector)
                .add(textRecognizer)
                .build();

        //Detect text and barcodes in the given frame newFrame
        Log.e("Starting:", "Detectrion");
        SparseArray<Object> objectSparseArray = multiDetector.detect(newFrame);

        multiDetector.release();
        textRecognizer.release();
        barcodeDetector.release();

        Log.e("Starting:", "StringConversion");
        ArrayList<String> fullText = new ArrayList<String>();

        //parse text and barcodes, add them to the arraylist
        for (int i = 0; i < objectSparseArray.size(); i++){
            Object currObject = objectSparseArray.valueAt(i);
            if(currObject instanceof TextBlock){
                Log.e("IS", "TEXTBLOCK");
                TextBlock item = (TextBlock) currObject;
                if (item != null && item.getValue() != null) {
                    fullText.add(item.getValue());
                }
            } else if(currObject instanceof Barcode){
                Log.e("IS","BARCODE");
                Barcode item = (Barcode) currObject;
                if (item != null && item.displayValue != null) {
                    fullText.add(item.displayValue);
                }
            }
        }

        //convert arraylist to string
        StringBuilder sb = new StringBuilder();
        for (String s : fullText) {
            sb.append(s);
            sb.append("\t");
        }

        String message = sb.toString();
        return message;
    }
}