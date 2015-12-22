package example.com.mpdlcamera.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.dd.processbutton.FlatButton;

import example.com.mpdlcamera.R;

/**
 * Created by kiran on 06.10.15.
 */

/**
 * It is possible that different users using the same device
 * SharedPreference is not enough for storing different personal settings
 * We use a seting table here
 */
public class BackupSettingsActivity extends AppCompatActivity {

    Context context = this;
    String mOption = null;
    private View rootView;
    Toolbar toolbar;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_settings_main);

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Switch logoutAfterUpload = (Switch) findViewById(R.id.lau);
        Switch removePhotos = (Switch) findViewById(R.id.rpfd);
       // Switch rgl = (Switch) findViewById(R.id.rgl);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        //show options
        textView = (TextView) findViewById(R.id.backup_item_list);
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

        textView.setText(mOption);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.netOption);


    /*    if(preferences.contains("lau")) {
                if(preferences.getString("lau","").equalsIgnoreCase("On")) {
                    lau.setChecked(true);
                }
                else
                    lau.setChecked(false);
        }
        else
            lau.setChecked(false);

        if(preferences.contains("rpfd")) {
            if(preferences.getString("rpfd","").equalsIgnoreCase("On")) {
                rpfd.setChecked(true);
            }
            else
                rpfd.setChecked(false);
        }
        else
            rpfd.setChecked(false);

*/
      /*  if(preferences.contains("rgl")) {
            if(preferences.getString("rgl","").equalsIgnoreCase("On")) {
                rgl.setChecked(true);
            }
            else
                rgl.setChecked(false);
        }
        else
            rgl.setChecked(false);

        */

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentSetPref = new Intent(getApplicationContext(), PrefActivity.class);
                startActivityForResult(intentSetPref,0);

          /*      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


                View popupView = inflater.inflate(R.layout.popup_backup, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        580,
                        440);

                popupWindow.setFocusable(true);
                popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                popupWindow.setAnimationStyle(R.style.AnimationPopup);

                RadioButton radOne = (RadioButton) popupView.findViewById(R.id.radioOne);

                RadioButton radTwo = (RadioButton) popupView.findViewById(R.id.radioTwo);
                RadioButton radThree = (RadioButton) popupView.findViewById(R.id.radioThree);

                popupWindow.showAtLocation(findViewById(R.id.textView), Gravity.CENTER, 0, 0);



                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                if (preferences.contains("status")) {

                    String defOption = preferences.getString("status", "");
                    if (defOption.equalsIgnoreCase("wifi")) {
                        radOne.setChecked(true);
                    } else if (defOption.equalsIgnoreCase("both")) {
                        radTwo.setChecked(true);
                    } else
                        radThree.setChecked(true);

                } else {
                    radOne.setChecked(true);
                }


                Button done = (Button) popupView.findViewById(R.id.btnDone);

                done.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intentSetPref = new Intent(getApplicationContext(), PrefActivity.class);
                        startActivityForResult(intentSetPref,0);

                        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

                        RelativeLayout rl = (RelativeLayout) v.getParent();


                        final RadioGroup radioOptionGroup = (RadioGroup) rl.findViewById(R.id.radioNetwork);


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


                        popupWindow.dismiss();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        TextView tv = (TextView) findViewById(R.id.backup_item);

                        String option = preferences.getString("status", "");
                        if (option.equalsIgnoreCase("wifi")) {
                            mOption = getString(R.string.wifi);
                        } else if (option.equalsIgnoreCase("both")) {
                            mOption = getString(R.string.wifidata);
                        } else
                            mOption = getString(R.string.manual);
                        tv.setText(mOption);


                    }
                });


                Button cancel = (Button) popupView.findViewById(R.id.cancel);
                cancel.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

                */
            }
        });




      /*  lau.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = preferences.edit();
                if(isChecked) {


                    editor.putString("lau", "On");
                    editor.apply();
                }
                else {
                    editor.putString("lau","Off");
                    editor.apply();
                }


            }
        });

        rpfd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {



                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = preferences.edit();
                if(isChecked) {


                    editor.putString("rpfd", "On");
                    editor.apply();
                } else {
                    editor.putString("rpfd", "Off");
                    editor.apply();
                }


            }
        });

        */

      /*  rgl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = preferences.edit();
                if (isChecked) {


                    editor.putString("rgl", "On");
                    editor.apply();
                } else {
                    editor.putString("rgl", "Off");
                    editor.apply();
                }


            }
        });
*/

        FlatButton btnDone = (FlatButton) findViewById(R.id.btnDone);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent settingsIntent = new Intent(context, SettingsActivity.class );
                startActivity(settingsIntent);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(context, SettingsActivity.class );
                startActivity(settingsIntent);
            }
        });




    }

    /*

     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    /*    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String prefList = sharedPreferences.getString("status", "");

        tv.setText(prefList);

        String statusLau = sharedPreferences.getString("lau","");


*/
    }






}
