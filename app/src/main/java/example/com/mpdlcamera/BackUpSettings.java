package example.com.mpdlcamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Model.User;

/**
 * Created by kiran on 21.09.15.
 */
public class BackUpSettings extends AppCompatActivity {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_settings);
        Button btnDone = (Button) findViewById(R.id.btnDone);
        RadioButton radOne = (RadioButton) findViewById(R.id.radioOne);
        RadioButton radTwo = (RadioButton) findViewById(R.id.radioTwo);
        RadioButton radApp = (RadioButton) findViewById(R.id.radioApp);
        RadioButton radBack = (RadioButton) findViewById(R.id.radioBack);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.contains("status")) {

            String defOption = preferences.getString("status", "");
            if (defOption.equalsIgnoreCase("wifi")) {
                radOne.setChecked(true);
            } else
                radTwo.setChecked(true);

        }
        else {
            radOne.setChecked(true);
        }


        if(preferences.contains("uploadStatus")) {

            String upOption = preferences.getString("uploadStatus", "");
            if (upOption.equalsIgnoreCase("app")) {
                radApp.setChecked(true);
            } else
                radBack.setChecked(true);

        }
        else {
            radApp.setChecked(true);
        }



    }

    public void networkOption(View view) {

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final RadioGroup radioOptionGroup = (RadioGroup) findViewById(R.id.radioNetwork);




                int selectedId = radioOptionGroup.getCheckedRadioButtonId();

                //RadioButton radioNetworkButton = (RadioButton) findViewById(selectedId);

                if(selectedId == R.id.radioOne) {

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("status","wifi");


                    editor.apply();
                }
                if(selectedId == R.id.radioTwo) {

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("status","both");
                    editor.apply();
                }

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);

            }




    }







