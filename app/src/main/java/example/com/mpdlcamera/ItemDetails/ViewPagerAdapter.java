package example.com.mpdlcamera.ItemDetails;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.camPicassoLoader;

/**
 * Created by yingli on 3/16/16.
 */
public class ViewPagerAdapter extends PagerAdapter {

    Context context;
    LayoutInflater inflater;
    Point size;
    List<String> imagePathList;
    boolean isLocalImage;
    private OnItemClickListener onItemClickListener;

    public Set<Integer> positionSet = new HashSet<>();

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void setPositionSet(Set<Integer> positionSet){
        this.positionSet = positionSet;
        Log.e("setPositionSet",positionSet.toString());
        notifyDataSetChanged();
    }

    public ViewPagerAdapter(Context context, Point size,boolean isLocalImage, List<String> imagePathList) {
        this.context = context;
        this.size = size;
        this.isLocalImage = isLocalImage;
        this.imagePathList = imagePathList;
    }

    @Override
    public int getCount() {
        return imagePathList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        ImageView imageView;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.viewpager_item, container,
                false);

        imageView = (ImageView) itemView.findViewById(R.id.detail_image);


        //check mark
        ImageView checkMark = (ImageView) itemView.findViewById(R.id.viewpager_check_mark);
        if(positionSet.contains(position)){
            checkMark.setVisibility(View.VISIBLE);
        }else {
            checkMark.setVisibility(View.GONE);
        }

        if(isLocalImage){
            Uri uri = Uri.fromFile(new File(imagePathList.get(position)));
            Picasso.with(context)
                    .load(uri)
                    .resize(size.x, size.y)
                    .centerInside()
                            //.centerCrop()
                            //.placeholder(R.drawable.progress_animation)
                    .into(imageView);
        }else {
            Picasso myPicasso = new Picasso.Builder(context).downloader(new camPicassoLoader(context)).build();
            myPicasso.load(imagePathList.get(position))
                    .resize(size.x, size.y)
                    .centerInside()
                    .error(R.drawable.error_alert).into(imageView);
        }

        if (onItemClickListener!=null){

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onItemLongClick(v, position);
                    return false;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, position);
                }
            });
        }

        ((ViewPager) container).addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }

    // view pager need to remove all views and reload them all
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view,int position);
    }
}
