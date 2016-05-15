package de.sjsolutions.pipay;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat implements InputDialogFragment.OnDialogInputListener {
    private EditTextPreference prefPassword;
    private SwitchPreferenceCompat prefAdminmode;
    private EditTextPreference btnModifyBalance;
    private Preference btnShowWelcome;
    private FragmentListener listener;

    public final static String SETTING_USERNAME = "pref_username";
    public final static String SETTING_PIN = "pref_password";
    public final static String SETTING_ADMINMODE = "pref_adminmode";

    private final String ADMIN_PASSWORD = "admin";

    public SettingsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (FragmentListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        listener.setTitle(R.string.title_settings);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

        prefPassword = (EditTextPreference) findPreference("pref_password");
        prefAdminmode = (SwitchPreferenceCompat) findPreference("pref_adminmode");
        btnModifyBalance = (EditTextPreference) findPreference("button_modify_balance");
        btnShowWelcome = findPreference("button_show_welcome");

        prefAdminmode.setOnPreferenceClickListener(pref -> {
            prefPassword.setEnabled(prefAdminmode.isChecked());
            btnModifyBalance.setVisible(prefAdminmode.isChecked());
            btnShowWelcome.setVisible(prefAdminmode.isChecked());
            return true;
        });

        prefAdminmode.setOnPreferenceChangeListener((pref, value) -> {
            if (!(Boolean) value) {
                return true;
            }
            InputDialogFragment pwdDialog = InputDialogFragment.newInstance(R.layout.dialog_enter_adminpwd,
                    R.string.settings_enter_adminpwd);
            pwdDialog.setTargetFragment(this, 0);
            pwdDialog.show(getFragmentManager(), "PWD_DIALOG");
            return false;
        });

        btnModifyBalance.setOnPreferenceChangeListener((pref, value) -> {
            try {
                listener.addBalance(Double.parseDouble((String) value));
            } catch (NumberFormatException ignored) {
            }
            return false;
        });

        btnShowWelcome.setOnPreferenceClickListener(pref -> {
            getActivity().getPreferences(Context.MODE_PRIVATE).edit().putBoolean(WelcomeFragment.PREF_SHOW_WELCOME, true).apply();
            return true;
        });

        //ensure that ui conforms to the switch
        prefAdminmode.getOnPreferenceClickListener().onPreferenceClick(prefAdminmode);
    }

    @Override
    public void onPause() {
        super.onPause();
        listener.onSettingsChanged();
    }

    @Override
    public void onDialogInput(String input, InputDialogFragment dialog) {
        if (input.equals(ADMIN_PASSWORD)) {
            prefAdminmode.setChecked(true);
            prefAdminmode.getOnPreferenceClickListener().onPreferenceClick(prefAdminmode);
            dialog.dismiss();
        } else {
            prefAdminmode.setChecked(false);
            prefAdminmode.getOnPreferenceClickListener().onPreferenceClick(prefAdminmode);
            dialog.setStatusText(R.string.settings_wrong_password);
        }

    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }
}
