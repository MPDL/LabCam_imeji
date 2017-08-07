package de.mpg.mpdl.labcam.code.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;

import de.mpg.mpdl.labcam.Model.LocalModel.Voice;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.base.BaseCompatActivity;
import de.mpg.mpdl.labcam.code.common.adapter.RecentVoiceAdapter;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Yunqing on 20.12.16.
 */

public class RecentVoiceActivity extends BaseCompatActivity {

    Activity activity = this;
    RecentVoiceAdapter recentVoiceAdapter = null;
    private String userId;
    private String serverName;

    //ui elements
    @BindView(R.id.listView_recent_voice)
    ListView recentVoiceListView;
    @BindView(R.id.tv_no_recent_voice)
    TextView noRecentVoiceView;
    List<Voice> voiceList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_recent_voice_notes;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        userId =  PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");
        serverName = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME, "");
        voiceList =new Select().from(Voice.class).where("userId = ?", userId).where("serverName = ?", serverName).orderBy("createTime DESC")
                .execute();

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_recent_task);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recentVoiceAdapter = new RecentVoiceAdapter(activity, voiceList, userId, serverName);
        recentVoiceAdapter.notifyDataSetChanged();
        recentVoiceListView.setAdapter(recentVoiceAdapter);

        //Display either "no recent voice" or the listView of voice
        if(voiceList.size() == 0) {
            noRecentVoiceView.setVisibility(View.VISIBLE);
            recentVoiceListView.setVisibility(View.GONE);
        }else {
            noRecentVoiceView.setVisibility(View.GONE);
            recentVoiceListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
