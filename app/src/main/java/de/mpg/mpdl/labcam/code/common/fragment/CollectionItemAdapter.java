package de.mpg.mpdl.labcam.code.common.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mpg.mpdl.labcam.LabCam;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.activity.DetailActivity;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.moudle.glide.ImageLoader;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

/**
 * author : yingli
 * time   : 8/31/17
 * desc   :
 */

public class CollectionItemAdapter extends RecyclerView.Adapter<CollectionItemAdapter.ItemViewHolder>{

    List<String> mItemsList = new ArrayList<>();
    private Map<String, String> headers = new HashMap<>();
    Context context;

    public CollectionItemAdapter(List<String> mItemsList) {
        this.mItemsList = mItemsList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.collection_item_cell, parent, false);

        String apiKey = PreferenceUtil.getString(context, Constants.SHARED_PREFERENCES, Constants.API_KEY, "");
        this.headers.put("Authorization","Bearer "+apiKey);

        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        String imageUrl;
        if(mItemsList!=null) {
            imageUrl = mItemsList.get(position);
        }else imageUrl = "";

        ImageLoader.loadStringRes(holder.mImageView, imageUrl, ImageLoader.defConfig, null);

    }

    @Override
    public int getItemCount() {
        return mItemsList!=null? mItemsList.size() : 0;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView mImageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.collection_image);
            mImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent showDetailIntent = new Intent(context, DetailActivity.class);
            ArrayList itemPathList = new ArrayList(mItemsList);
            showDetailIntent.putStringArrayListExtra("itemPathList", itemPathList);
            showDetailIntent.putExtra("positionInList",getAdapterPosition());
            context.startActivity(showDetailIntent);
        }
    }
}
