package de.mpg.mpdl.labcam.code.common.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.mpg.mpdl.labcam.Model.DataItem;

/**
 * author : yingli
 * time   : 8/31/17
 * desc   :
 */

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>{

    private List<DataItem> collectionList;

    @Override
    public CollectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(CollectionViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class CollectionViewHolder extends RecyclerView.ViewHolder{

        public CollectionViewHolder(View itemView) {
            super(itemView);
        }
    }
}
