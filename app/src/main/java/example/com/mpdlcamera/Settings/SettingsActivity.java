package example.com.mpdlcamera.Settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.dd.processbutton.FlatButton;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.R;

/**
 * Created by Allen on 06.10s.15.
 */
public class SettingsActivity extends AppCompatActivity {


    private Activity activity = this;
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private View rootView;
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        TextView list_item_backup = (TextView) findViewById(R.id.list_item_backup);
        TextView list_item_local = (TextView) findViewById(R.id.list_item_local);
        TextView list_item_server = (TextView) findViewById(R.id.list_item_server);


        FlatButton btnDone = (FlatButton) findViewById(R.id.btnDone);


    }

}







