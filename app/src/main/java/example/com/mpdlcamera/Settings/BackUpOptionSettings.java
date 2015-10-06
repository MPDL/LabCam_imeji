package example.com.mpdlcamera.Settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.R;

/**
 * Created by kiran on 21.09.15.
 */
public class BackUpOptionSettings extends AppCompatActivity {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_settings);
        Button btnDone = (Button) findViewById(R.id.btnDone);
        RadioButton radOne = (RadioButton) findViewById(R.id.radioOne);
        RadioButton radTwo = (RadioButton) findViewById(R.id.radioTwo);
        RadioButton radThree = (RadioButton) findViewById(R.id.radioThree);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.contains("status")) {

            String defOption = preferences.getString("status", "");
            if (defOption.equalsIgnoreCase("wifi")) {
                radOne.setChecked(true);
            } else if(defOption.equalsIgnoreCase("both")) {
                radTwo.setChecked(true);
            }
            else
                radThree.setChecked(true);

        } else {
            radOne.setChecked(true);
        }


    }

    /**
     * On click event for the button in backup settings screen
     * @param view
     */
    public void networkOption(View view) {

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final RadioGroup radioOptionGroup = (RadioGroup) findViewById(R.id.radioNetwork);



        int selectedId = radioOptionGroup.getCheckedRadioButtonId();



        if (selectedId == R.id.radioOne) {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("status", "wifi");


            editor.apply();
        }
        if (selectedId == R.id.radioTwo) {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("status", "both");
            editor.apply();
        }

        if (selectedId == R.id.radioThree) {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("status", "manual");
            editor.apply();
        }


        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

}







