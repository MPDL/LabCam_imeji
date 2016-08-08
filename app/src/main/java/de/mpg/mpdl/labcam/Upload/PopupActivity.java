package de.mpg.mpdl.labcam.Upload;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import de.mpg.mpdl.labcam.R;

/**
 * Created by kiran on 05.11.15.
 * Activity for the popup which asks the user for the confirmation of logout
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

        //If it is yes, then logout
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor ed = preferences.edit();
                ed.putString("option","yes");
                ed.commit();
                finish();
            }
        });

        //If it is no, then dismiss the popup window to do nothing
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
