package de.mpg.mpdl.labcam.code.common.fragment;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.CustomImageDownaloder;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

/**
 * author : yingli
 * time   : 8/31/17
 * desc   :
 */

public class CollectionItemAdapter extends RecyclerView.Adapter<CollectionItemAdapter.ItemViewHolder>{

    List<String> mItemsList = new ArrayList<>();
    private Map<String, String> headers = new HashMap<>();
    DisplayImageOptions options;

    public CollectionItemAdapter(List<String> mItemsList) {
        this.mItemsList = mItemsList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.collection_item_cell, parent, false);

        String apiKey = PreferenceUtil.getString(parent.getContext(), Constants.SHARED_PREFERENCES, Constants.API_KEY, "");
        this.headers.put("Authorization","Bearer "+apiKey);

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(parent.getContext())
                .imageDownloader(new CustomImageDownaloder(parent.getContext()))
                .writeDebugLogs()
                .build();

        ImageLoader.getInstance().init(configuration);
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .extraForDownloader(headers)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnFail(R.drawable.error_alert)
                .build();

        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        String imageUrl;
        if(mItemsList!=null) {
            imageUrl = mItemsList.get(position);
        }else imageUrl = "";

        ImageLoader.getInstance().loadImage(imageUrl, options, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view,
                                          Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                holder.mImageView.setImageBitmap(loadedImage);
            }

        });

    }

    @Override
    public int getItemCount() {
        return mItemsList!=null? mItemsList.size() : 0;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder{
        ImageView mImageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.collection_image);
        }
    }
}
