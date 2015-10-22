package example.com.mpdlcamera.Settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import java.util.List;

import example.com.mpdlcamera.R;

/**
 * Created by kiran on 22.10.15.
 */
public class PrefFragment extends PreferenceFragment {

    private SwitchPreference switchPreferenceLau;
    private SwitchPreference switchPreferenceRpfd;
    private ListPreference backupPreference;
    SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        addPreferencesFromResource(R.xml.preference);




        PreferenceManager preferenceManager = getPreferenceManager();

        switchPreferenceLau = (SwitchPreference) findPreference("L_A_U");
        switchPreferenceRpfd = (SwitchPreference) findPreference("R_P_F_D");
        backupPreference = (ListPreference) findPreference("status");

        if(backupPreference != null) {

            backupPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String value = (String) newValue;

                    backupPreference.setSummary(value);
                    return  true;
                }
            });

        }

        if(switchPreferenceLau != null) {

            switchPreferenceLau.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean statusLau = ((Boolean) newValue).booleanValue();

                    SharedPreferences.Editor e = mPrefs.edit();
                    e.putBoolean("L_A_U", statusLau);
                    e.commit();
                    return  true;
                }
            });

        }

        if(switchPreferenceRpfd != null) {

            switchPreferenceRpfd.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean statusRpfd = ((Boolean) newValue).booleanValue();

                    SharedPreferences.Editor e = mPrefs.edit();
                    e.putBoolean("R_P_F_D", statusRpfd);
                    e.commit();
                    return true;
                }
            });
        }



    }
}
