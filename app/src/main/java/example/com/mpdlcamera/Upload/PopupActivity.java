package example.com.mpdlcamera.Upload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import example.com.mpdlcamera.R;

/**
 * Created by kiran on 05.11.15.
 */
public class PopupActivity extends Activity {

    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("logout", Context.MODE_PRIVATE);
        setContentView(R.layout.logout_confirm);

        Button yes = (Button) findViewById(R.id.buttonYes);
        Button no = (Button) findViewById(R.id.buttonNo);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor ed = preferences.edit();
                ed.putString("option","yes");
                ed.commit();
                finish();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor ed = preferences.edit();
                ed.putString("option","no");
                ed.commit();
                finish();
            }
        });
    }
}
