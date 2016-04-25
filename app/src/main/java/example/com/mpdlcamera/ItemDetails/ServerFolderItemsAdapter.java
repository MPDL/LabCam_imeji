package example.com.mpdlcamera.ItemDetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.activeandroid.query.Select;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.camPicassoLoader;

/**
 * Created by yingli on 4/25/16.
 */
public class ServerFolderItemsAdapter extends RecyclerView.Adapter<ServerFolderItemsAdapter.ViewHolder>  {



    private Activity activity;
    private ArrayList<String> galleryItems;

    public ServerFolderItemsAdapter(Activity activity, ArrayList<String> galleryItems) {
        this.activity = activity;
        this.galleryItems = galleryItems;
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
//
//        //show image
//        Picasso myPicasso = new Picasso.Builder(activity).downloader(new camPicassoLoader(activity)).build();
//        myPicasso.load(filePath)
//                .resize(size.x / 2, size.y/3)
//                .centerInside()
//                .error(R.drawable.error_alert).into(holder.imageView);

        Picasso.with(activity)
                .load(filePath)
                .centerInside().resize(size.x/2, size.y/3)
                .into(holder.imageView);

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
