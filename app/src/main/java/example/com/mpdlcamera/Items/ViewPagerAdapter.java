package example.com.mpdlcamera.Items;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import example.com.mpdlcamera.R;

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

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
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
            Picasso.with(context)
                    .load(imagePathList.get(position))
                    .resize(size.x, size.y)
                    .centerInside()
                    .into(imageView);
        }

        if (onItemClickListener!=null){

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onItemLongClick(v, position);
                    return false;
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

    public interface OnItemClickListener{
        void onItemLongClick(View view,int position);
    }
}
