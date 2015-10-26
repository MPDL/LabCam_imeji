package example.com.mpdlcamera.Settings;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import example.com.mpdlcamera.R;

/**
 * Created by kiran on 22.10.15.
 */
public class PrefActivity extends PreferenceActivity{

 //   private View rootView;
  // Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

     /* rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
*/

        PrefFragment prefFragment = new PrefFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(android.R.id.content, prefFragment);
        fragmentTransaction.commit();
    }
}
