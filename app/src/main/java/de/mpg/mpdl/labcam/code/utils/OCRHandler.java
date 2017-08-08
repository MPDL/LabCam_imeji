package de.mpg.mpdl.labcam.code.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.mpg.mpdl.labcam.Model.LineAttributes;

public class OCRHandler {

    public interface FragmentCallback {
        void onTaskDone(ArrayList<LineAttributes> lineAttributes);
    }

    static public class TaskParams {

        Bitmap bitmap;
        Context context;

        public TaskParams(Bitmap bitmap, Context context) {
            this.bitmap = bitmap;
            this.context = context;
        }
    }

    public static float scaleX(float horizontal) {
        return horizontal * 1.0f;
    }

    public static float scaleY(float vertical) {
        return vertical * 1.0f;
    }

    public static float translateX(float x) {
        return scaleX(x);
    }

    public static float translateY(float y) {
        return scaleY(y);
    }


    static public class getText extends AsyncTask<TaskParams, Void, ArrayList<LineAttributes>> {

        public FragmentCallback mFragmentCallback;

        private ArrayList<LineAttributes> lineAttributes = new ArrayList<>();

        public getText(FragmentCallback fragmentCallback) {
            mFragmentCallback = fragmentCallback;
        }

        @Override
        protected ArrayList<LineAttributes> doInBackground(OCRHandler.TaskParams... params) {

            String message = new String();
            int count = params.length;
            for (int z = 0; z < count; z++) {
                if (isCancelled()) break;
                Bitmap bitmap = params[z].bitmap;
                Context context = params[z].context;

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Bitmap compressed = BitmapFactory.decodeStream(
                        new ByteArrayInputStream(out.toByteArray()));

                Frame newFrame = new Frame.Builder()
                        .setBitmap(compressed)
                        .build();

                //initialize Textrecognizer
                BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
                TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();


                //parse text
                MultiDetector multiDetector = new MultiDetector.Builder()
                        .add(barcodeDetector)
                        .add(textRecognizer)
                        .build();

                SparseArray<Object> objectSparseArray = multiDetector.detect(newFrame);

                ArrayList<String> fullText = new ArrayList<>();


                for (int i = 0; i < objectSparseArray.size(); i++) {
                    Object currObject = objectSparseArray.valueAt(i);
                    if (currObject instanceof TextBlock) {
                        TextBlock item = (TextBlock) currObject;
                        List<Line> lines = (List<Line>) ((TextBlock) currObject).getComponents();
                        for (Line elements : lines) {
                            RectF rect = new RectF(elements.getBoundingBox());
                            float height = rect.bottom - rect.top;
                            LineAttributes attr = new LineAttributes(
                                    rect.left, rect.top, rect.right, rect.bottom,
                                    elements.getValue(), height, false);
                            lineAttributes.add(attr);
                        }
                        if (item != null && item.getValue() != null) {
                            fullText.add(item.getValue());

                        }

                    } else if (currObject instanceof Barcode) {
                        Barcode item = (Barcode) currObject;
                        if (item != null && item.displayValue != null) {
                            fullText.add("\nBARCODE: " + item.displayValue);
                        }
                    }
                }
                multiDetector.release();
                textRecognizer.release();
                barcodeDetector.release();

                if (lineAttributes.size() > 0) {
                    Collections.sort(lineAttributes, new Comparator<LineAttributes>() {
                        @Override
                        public int compare(LineAttributes o1, LineAttributes o2) {
                            int result = Float.compare(o1.getHeight(), o2.getHeight());
                            return result;
                        }
                    });

                    float med = 0f;
                    int halfway;
                    if (lineAttributes.size() % 2 == 0 && lineAttributes.size() > 1) {
                        halfway = lineAttributes.size() / 2;
                    } else if (lineAttributes.size() > 1) {
                        halfway = lineAttributes.size() / 2 + 1;
                    } else {
                        halfway = 0;
                    }
                    med = lineAttributes.get(halfway).getHeight();
                    float max = lineAttributes.get(lineAttributes.size() - 1).getHeight();
                    float threshold = med + med / (2.5f);

                    if (max > threshold) {
                        for (int i = 0; i < lineAttributes.size(); i++) {
                            //Log.e("CURRHEIGHT", Float.toString(lineAttributes.get(i).getHeight()));
                            if (lineAttributes.get(i).getHeight() >= max - (max) / 10) {
                                lineAttributes.get(i).setIsHeadline(true);
                            } else if (i == lineAttributes.size() - 1) {
                                lineAttributes.get(i).setIsHeadline(true);
                            }
                        }
                    }

                    Collections.sort(lineAttributes, new Comparator<LineAttributes>() {
                        @Override
                        public int compare(LineAttributes o1, LineAttributes o2) {
                            int result = Float.compare(o1.getTop(), o2.getTop());
                            return result;
                        }
                    });

                    for (LineAttributes line : lineAttributes) {
                        Log.e("LINE: ", " HEIGHT: " + Float.toString(line.getHeight()) + " TEXT: " + line.getText());
                    }

                    return lineAttributes;
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
            //progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList<LineAttributes> lineAttributes) {
            mFragmentCallback.onTaskDone(lineAttributes);
        }

    }
}
