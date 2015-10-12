package example.com.mpdlcamera.Settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dd.processbutton.FlatButton;

import example.com.mpdlcamera.R;

/**
 * Created by Allen on 06.10s.15.
 */
public class SettingsActivity extends AppCompatActivity {


    private Activity activity = this;
    private final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private View rootView;
    Toolbar toolbar;
    String mOption;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        LinearLayout setting_backup = (LinearLayout) findViewById(R.id.setting_backup);
        TextView list_item_backup = (TextView) findViewById(R.id.list_item_backup);

        LinearLayout setting_local = (LinearLayout) findViewById(R.id.setting_local);
        TextView list_item_local = (TextView) findViewById(R.id.list_item_local);

        LinearLayout setting_server = (LinearLayout) findViewById(R.id.setting_server);
        TextView list_item_server = (TextView) findViewById(R.id.list_item_server);



        FlatButton btnDone = (FlatButton) findViewById(R.id.btnDone);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(preferences.contains("status")) {
            String option = preferences.getString("status", "");
            if (option.equalsIgnoreCase("wifi")) {
                mOption = getString(R.string.wifi);
            } else if (option.equalsIgnoreCase("both")) {
                mOption = getString(R.string.wifidata);
            } else
                mOption = getString(R.string.manual);
        }
        else
            mOption = getString(R.string.wifi);

        list_item_backup.setText(mOption);


        setting_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent settingsIntent = new Intent(activity, BackupSettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        setting_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent settingsIntent = new Intent(activity, LocalAlbumSettingsActivity.class);
                startActivity(settingsIntent);
            }
        });


        setting_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent settingsIntent = new Intent(activity, RemoteCollectionSettingsActivity.class);
                startActivity(settingsIntent);
            }
        });





    }

}







