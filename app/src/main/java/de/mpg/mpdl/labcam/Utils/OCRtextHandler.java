package de.mpg.mpdl.labcam.Utils;


import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.util.SparseArray;

import de.mpg.mpdl.labcam.Objects.LineAttributes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

        SparseArray<Object> objectSparseArray = multiDetector.detect(newFrame);

        ArrayList<LineAttributes> lineAttributes = new ArrayList<>();

        for (int i = 0; i < objectSparseArray.size(); i++) {
            Object currObject = objectSparseArray.valueAt(i);
            if (currObject instanceof TextBlock) {
                TextBlock item = (TextBlock) currObject;
                List<Line> lines = (List<Line>) ((TextBlock) currObject).getComponents();
                for(Line elements: lines){
                    RectF rect = new RectF(elements.getBoundingBox());
                    float height = rect.bottom - rect.top;
                    LineAttributes attr = new LineAttributes(rect.left, rect.top, rect.right, rect.bottom, elements.getValue(), height, false);
                    lineAttributes.add(attr);
                }
            }
        }
        multiDetector.release();
        textRecognizer.release();
        barcodeDetector.release();


        Collections.sort(lineAttributes, new Comparator<LineAttributes>() {
            @Override
            public int compare(LineAttributes o1, LineAttributes o2) {
                return (Float.compare(o1.getTop(), o2.getTop()));
            }
        });

        StringBuilder sb = new StringBuilder();
        for (LineAttributes line : lineAttributes) {
            sb.append(line.getText());
            sb.append("\n");
        }
        return (sb.toString());
    }
}