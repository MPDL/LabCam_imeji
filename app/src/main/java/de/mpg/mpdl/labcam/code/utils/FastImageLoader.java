package de.mpg.mpdl.labcam.code.utils;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.mpg.mpdl.labcam.code.common.fragment.CollectionViewFragment;


public class FastImageLoader extends Application {

    //TODO accept parameters width and height optionally
    //TODO accept parameters apiKey and paths optionally
    //TODO make callbackfunction unspecific for usage in other methods

    public class AsyncImageLoader extends AsyncTask<Bundle, Void, RoundedBitmapDrawable>{

        private CollectionViewFragment.FragmentCallback mFragmentCallback;
        private Context mContext;

        public AsyncImageLoader(Context context,
                                CollectionViewFragment.FragmentCallback fragmentCallback) {
            mFragmentCallback = fragmentCallback;
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected RoundedBitmapDrawable doInBackground(Bundle... params){
            Bundle bundle = params[0];
            URL url;
            try {
                url = new URL(bundle.getString("url"));
            } catch (Exception e) {
                url = null;
            }

            int width = bundle.getInt("width");
            int height = bundle.getInt("height");
            String apiKey = bundle.getString("apiKey");
            if (url != null) {
                return getRoundedImage(width, height, url, mContext, apiKey);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(RoundedBitmapDrawable drawable) {
            mFragmentCallback.onTaskDone(drawable);
        }
    }

    public static int calcSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }
        return inSampleSize;
    }

    public static Bitmap getImage(int destWidth, int destHeight, String bitmapResource) {
        BitmapFactory.Options preLoaderOptions = new BitmapFactory.Options();
        preLoaderOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bitmapResource, preLoaderOptions);
        int[] outs = {preLoaderOptions.outWidth, preLoaderOptions.outHeight};

        BitmapFactory.Options loaderOptions = new BitmapFactory.Options();
        loaderOptions.inScaled = true;
        loaderOptions.inDensity = outs[0];
        loaderOptions.inSampleSize = calcSampleSize(loaderOptions, destWidth, destHeight);
        loaderOptions.inTargetDensity = destWidth * loaderOptions.inSampleSize;

        return BitmapFactory.decodeFile(bitmapResource, loaderOptions);
    }

    public static Bitmap getImage(int destWidth, int destHeight, URL resource) {
        BitmapFactory.Options preLoaderOptions = new BitmapFactory.Options();
        preLoaderOptions.inJustDecodeBounds = true;
        try {
            HttpURLConnection connection = (HttpURLConnection) resource.openConnection();
            connection.setUseCaches(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            BitmapFactory.decodeStream(input, null, preLoaderOptions);

        } catch (IOException e) {
            return null;
        }

        int[] outs = {preLoaderOptions.outWidth, preLoaderOptions.outHeight};
        BitmapFactory.Options loaderOptions = new BitmapFactory.Options();
        loaderOptions.inScaled = true;
        loaderOptions.inDensity = outs[0];
        loaderOptions.inSampleSize = calcSampleSize(loaderOptions, destWidth, destHeight);
        loaderOptions.inTargetDensity = destWidth * loaderOptions.inSampleSize;
        try {
            HttpURLConnection connection = (HttpURLConnection) resource.openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input, null, loaderOptions);
        } catch (IOException e) {
            return null;
        }

    }

    public static Bitmap getImage(int destWidth, int destHeight, int drawable, Context context){
        Resources res = context.getResources();
        BitmapFactory.Options preLoaderOptions = new BitmapFactory.Options();
        preLoaderOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, drawable, preLoaderOptions);
        int[] outs = {preLoaderOptions.outWidth, preLoaderOptions.outHeight};

        BitmapFactory.Options loaderOptions = new BitmapFactory.Options();
        loaderOptions.inScaled = true;
        loaderOptions.inDensity = outs[0];
        loaderOptions.inSampleSize = calcSampleSize(loaderOptions, destWidth, destHeight);
        loaderOptions.inTargetDensity = destWidth * loaderOptions.inSampleSize;

        Bitmap bitmap = BitmapFactory.decodeResource(res, drawable, loaderOptions);
        return Bitmap.createScaledBitmap(bitmap, destWidth, destHeight, false);
    }

    // Rounded Images

    public static RoundedBitmapDrawable getRoundedImage(int destWidth, int destHeight,
                                                        String bitmapResource, Context context) {
        Resources res = context.getResources();
        BitmapFactory.Options preLoaderOptions = new BitmapFactory.Options();
        preLoaderOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bitmapResource, preLoaderOptions);
        int[] outs = {preLoaderOptions.outWidth, preLoaderOptions.outHeight};

        BitmapFactory.Options loaderOptions = new BitmapFactory.Options();
        loaderOptions.inScaled = true;
        loaderOptions.inDensity = outs[0];
        loaderOptions.inSampleSize = calcSampleSize(loaderOptions, destWidth, destHeight);
        loaderOptions.inTargetDensity = destWidth * loaderOptions.inSampleSize;

        Bitmap scaledBitmap = BitmapFactory.decodeFile(bitmapResource, loaderOptions);
        return RoundedBitmapDrawableFactory.create(res,
                scaledBitmap);
    }

    public static RoundedBitmapDrawable getRoundedImage(int destWidth, int destHeight, URL resource,
                                                        Context context, String apiKey) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build();
        StrictMode.setThreadPolicy(policy);
        Resources res = context.getResources();
        BitmapFactory.Options preLoaderOptions = new BitmapFactory.Options();
        preLoaderOptions.inJustDecodeBounds = true;
        try {
            HttpURLConnection connection = (HttpURLConnection) resource.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " +apiKey);
            connection.connect();
            InputStream input = connection.getInputStream();
            BitmapFactory.decodeStream(input, null, preLoaderOptions);
        } catch (IOException e) {
            return null;
        }

        int[] outs = {preLoaderOptions.outWidth, preLoaderOptions.outHeight};
        BitmapFactory.Options loaderOptions = new BitmapFactory.Options();
        loaderOptions.inScaled = true;
        loaderOptions.inDensity = outs[0];
        loaderOptions.inSampleSize = calcSampleSize(loaderOptions, destWidth, destHeight);
        loaderOptions.inTargetDensity = destWidth * loaderOptions.inSampleSize;
        try {
            HttpURLConnection connection = (HttpURLConnection) resource.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " +apiKey);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, loaderOptions);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, destWidth, destHeight, false);
            return RoundedBitmapDrawableFactory.create(res,
                    scaledBitmap);
        } catch (IOException e) {
            return null;
        }

    }

    public static RoundedBitmapDrawable getRoundedImage(int destWidth, int destHeight,
                                                        int drawable, Context context){
        Resources res = context.getResources();
        BitmapFactory.Options preLoaderOptions = new BitmapFactory.Options();
        preLoaderOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, drawable, preLoaderOptions);
        int[] outs = {preLoaderOptions.outWidth, preLoaderOptions.outHeight};

        BitmapFactory.Options loaderOptions = new BitmapFactory.Options();
        loaderOptions.inScaled = true;
        loaderOptions.inDensity = outs[0];
        loaderOptions.inSampleSize = calcSampleSize(loaderOptions, destWidth, destHeight);
        loaderOptions.inTargetDensity = destWidth * loaderOptions.inSampleSize;

        Bitmap bitmap = BitmapFactory.decodeResource(res, drawable, loaderOptions);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, destWidth, destHeight, false);
        return RoundedBitmapDrawableFactory.create(res, scaledBitmap);
    }
}