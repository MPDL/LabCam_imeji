package example.com.mpdlcamera.ItemDetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.util.LruCache;
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
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.CustomImageDownaloder;
import example.com.mpdlcamera.Utils.camPicassoLoader;


/**
 * Created by yingli on 4/25/16.
 */
public class ServerFolderItemsAdapter extends RecyclerView.Adapter<ServerFolderItemsAdapter.ViewHolder>  {

    private Activity activity;
    private ArrayList<String> galleryItems;
    private String apiKey;
    private SharedPreferences mPrefs;

    public ServerFolderItemsAdapter(Activity activity, ArrayList<String> galleryItems) {
        this.activity = activity;
        this.galleryItems = galleryItems;

        mPrefs = activity.getSharedPreferences("myPref", 0);
        apiKey = mPrefs.getString("apiKey","");
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
    public void onBindViewHolder(ViewHolder holder, final int position) {

        //prepare data
        Point size = getPoint();
        String filePath = galleryItems.get(position);

        //创建默认的ImageLoader配置参数
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(activity)
                .imageDownloader(new CustomImageDownaloder(activity))
                .writeDebugLogs() //打印log信息
                .build();


        //Initialize ImageLoader with configuration.
//        ImageSize targetSize = new ImageSize(size.x / 2, size.y/3);
        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageLoader.getInstance().init(configuration);


        //显示图片的配置
        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.progress_image)
                .showImageOnFail(R.drawable.error_alert)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();



        imageLoader.displayImage(filePath, holder.imageView, options);

//        Picasso myPicasso = new Picasso.Builder(activity)
//                .downloader(new camPicassoLoader(activity))
//                .build();
//
//        myPicasso.load(filePath)
//                .resize(size.x / 2, size.y/3)
////                .networkPolicy(NetworkPolicy.OFFLINE)
//                .centerInside()
//                .error(R.drawable.error_alert).into(holder.imageView);

//        Picasso.with(activity)
//                .load(filePath)
//                .networkPolicy(NetworkPolicy.OFFLINE)
//                .centerInside().resize(size.x/2, size.y/3)
//                .into(holder.imageView);

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
