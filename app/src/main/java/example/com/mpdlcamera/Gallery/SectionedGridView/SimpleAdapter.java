package example.com.mpdlcamera.Gallery.SectionedGridView;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.R;

/**
 * Created by yingli on 2/23/16.
 */
public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleViewHolder> {
    private static final int COUNT = 100;

    private final Context mContext;
    private final List<String> mItems;
    private int mCurrentItemId = 0;

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;

        public SimpleViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.header_grid_image);
        }
    }

    public SimpleAdapter(Context context,List<String> galleryItems) {
        mContext = context;
        mItems = galleryItems;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.header_grid_item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        String filePath = mItems.get(position);
        File imageFile = new File(filePath);
        Uri uri = Uri.fromFile(new File(filePath));
        Picasso.with(mContext)
                .load(uri)
                .resize(235,235)
                .centerCrop()
                .into(holder.imageView);
    }

    public void addItem(int position) {
//        final int id = mCurrentItemId++;
        mItems.add(position, "");
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}