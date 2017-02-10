package de.mpg.mpdl.labcam.code.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;

import de.mpg.mpdl.labcam.Model.LocalModel.Note;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.common.adapter.RecentNoteAdapter;
import de.mpg.mpdl.labcam.code.base.BaseCompatActivity;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Yunqing on 19.12.16.
 */


public class RecentNoteActivity extends BaseCompatActivity {

    Activity activity = this;
    RecentNoteAdapter recentNoteAdapter = null;

    //ui elements
    @BindView(R.id.listView_recent_text_notes)
    ListView recentTextListView;
    @BindView(R.id.tv_no_recent_text)
    TextView noRecentTextView;
    List<Note> noteList =new Select().from(Note.class)
                        .execute();


    @Override
    protected int getLayoutId() {
        return R.layout.activity_recent_text_notes;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_recent_task);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recentNoteAdapter = new RecentNoteAdapter(activity, noteList);
        recentNoteAdapter.notifyDataSetChanged();
        recentTextListView.setAdapter(recentNoteAdapter);

        //Display either "no recent upload" or the listView of uploaded tasks
        if(noteList.size() == 0) {
            noRecentTextView.setVisibility(View.VISIBLE);
            recentTextListView.setVisibility(View.GONE);
        }else {
            noRecentTextView.setVisibility(View.GONE);
            recentTextListView.setVisibility(View.VISIBLE);
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
