package de.mpg.mpdl.labcam.code.common.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import de.mpg.mpdl.labcam.R;

import java.util.List;

/**
 * Created by yingli on 2/7/17.
 */

public class TitleFragmentPagerAdapter extends FragmentPagerAdapter {

    /**
     * The m fragment list.
     */
    private List<Fragment> mFragmentList = null;

    private Context mContext;

    private LayoutInflater mInflater;

    private static final int[] TITLES = {
            R.string.tab_local,
            R.string.tab_server,
    };

    /**
     * titles是给TabLayout设置title用的
     *
     * @param fragmentManager
     * @param fragmentList
     */
    public TitleFragmentPagerAdapter(FragmentManager fragmentManager,
                                     List<Fragment> fragmentList, Context context) {
        super(fragmentManager);
        mFragmentList = fragmentList;
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    /**
     * 描述：获取数量.
     *
     * @return the count
     * @see android.support.v4.view.PagerAdapter#getCount()
     */
    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    /**
     * 描述：获取索引位置的Fragment.
     *
     * @param position the position
     * @return the item
     * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
     */
    @Override
    public Fragment getItem(int position) {

        Fragment fragment = null;
        if (position < mFragmentList.size()) {
            fragment = mFragmentList.get(position);
        }
        else {
            fragment = mFragmentList.get(0);
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TITLES[position]);
    }

    public View getTabView(int position) {
        View view = mInflater.inflate(R.layout.tab_with_icon, null);
        TextView tv = (TextView) view.findViewById(R.id.title);
        tv.setText(mContext.getString(TITLES[position]));

        return view;
    }

}

