package de.mpg.mpdl.labcam.code.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.activity.DetailActivity;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.CustomImageDownaloder;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by yingli on 4/25/16.
 */
public class ServerFolderItemsAdapter extends RecyclerView.Adapter<ServerFolderItemsAdapter.ViewHolder>  {

    private Activity activity;
    private ArrayList<String> galleryItems;
    private String apiKey;
    private Map<String, String> headers = new HashMap<String,String>() {
    };
    public ServerFolderItemsAdapter(Activity activity, ArrayList<String> galleryItems) {
        this.activity = activity;
        this.galleryItems = galleryItems;

        apiKey = PreferenceUtil.getString(activity, Constants.SHARED_PREFERENCES, Constants.API_KEY, "");
        this.headers.put("Authorization","Bearer "+apiKey);
    }

    // get screen size
    private Point getPoint(){
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Log.v(size.x / 2 + " ", size.y / 2 + "");
        return size;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.image_view);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(activity).inflate(R.layout.gallery_grid_cell, parent, false);
        return new ViewHolder(view);    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Point size = getPoint();
        String filePath = galleryItems.get(position);

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(activity)
                .imageDownloader(new CustomImageDownaloder(activity))
                .writeDebugLogs()
                .build();

        ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
        layoutParams.width = (size.x / 2)-6;
        layoutParams.height = (size.y/4)+5;
        holder.imageView.setLayoutParams(layoutParams);

        ImageLoader.getInstance().init(configuration);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .extraForDownloader(headers)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();


        ImageLoader.getInstance().loadImage(filePath, options, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view,
                                          Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                holder.imageView.setImageBitmap(loadedImage);
            }

        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showDetailIntent = new Intent(activity, DetailActivity.class);
                showDetailIntent.putStringArrayListExtra("itemPathList", galleryItems);
                showDetailIntent.putExtra("positionInList",position);
                activity.startActivity(showDetailIntent);
            }
        });
//
    }

    @Override
    public int getItemCount() {
        return galleryItems.size();
    }

}
