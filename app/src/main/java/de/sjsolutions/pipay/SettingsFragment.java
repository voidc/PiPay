package de.sjsolutions.pipay;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
    private FragmentListener listener;

    public final static String SETTING_USERNAME = "pref_username";
    public final static String SETTING_PIN = "pref_password";
    public final static String SETTING_ADMINMODE = "pref_adminmode";

    public SettingsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (FragmentListener) context;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

        EditTextPreference prefBalance = (EditTextPreference) findPreference("pref_balance");
        prefBalance.setPersistent(false);
        prefBalance.setVisible(false);
        prefBalance.setOnPreferenceChangeListener((pref, value) -> {
            listener.addBalance(Double.parseDouble((String) value));
            return false;
        });

        SwitchPreferenceCompat prefAdminmode = (SwitchPreferenceCompat) findPreference("pref_adminmode");
        prefAdminmode.setOnPreferenceChangeListener((pref, value) -> {
            //TODO: check password
            findPreference("pref_password").setEnabled((Boolean) value);
            prefBalance.setVisible((Boolean) value);
            return true;
        });

        //ensure that ui conforms to the switch
        prefAdminmode.getOnPreferenceChangeListener().onPreferenceChange(prefAdminmode, prefAdminmode.isChecked());
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setTitle(R.string.title_settings);
    }

}
