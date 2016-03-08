package example.com.mpdlcamera.Gallery.SectionedGridView;

import android.content.Context;
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

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import example.com.mpdlcamera.R;

/**
 * Created by yingli on 2/23/16.
 */
public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleViewHolder> {
    private static final int COUNT = 100;

    private final Context mContext;
    private final List<String> mItems;

    private LayoutInflater inflater;

    //remember selected positions
    public Set<Integer> positionSet = new HashSet<>();


    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;
        public final ImageView checkMark;

        public SimpleViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.header_grid_image);
            checkMark = (ImageView) view.findViewById(R.id.header_grid_check_mark);
        }
    }

    public SimpleAdapter(Context context,List<String> galleryItems) {
        mContext = context;
        mItems = galleryItems;
        inflater = LayoutInflater.from(context);
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.header_grid_item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        String filePath = mItems.get(position);
        Uri uri = Uri.fromFile(new File(filePath));

        Picasso.with(mContext)
                .load(uri)
                .resize(235,235)
                .centerCrop()
                .into(holder.imageView);

        if(positionSet.contains(position)){
            holder.checkMark.setVisibility(View.VISIBLE);
        }else {
            holder.checkMark.setVisibility(View.GONE);
        }


        if (onItemClickListener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v,position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onItemLongClick(v,position);
                    return false;
                }
            });
        }
    }

    public void remove(String str){
        mItems.remove(str);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public String getItem(int pos){
        return mItems.get(pos);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setPositionSet(Set<Integer> positionSet){
        this.positionSet = positionSet;
        Log.e("positionSet",positionSet.size()+"");
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view,int position);
    }
}