package de.sjsolutions.pipay;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.sjsolutions.pipay.util.Rank;
import de.sjsolutions.pipay.util.TransactionLog;

public class SettingsFragment extends PreferenceFragmentCompat implements InputDialogFragment.OnDialogInputListener {
    private EditTextPreference prefUsername;
    private EditTextPreference prefPassword;
    private SwitchPreferenceCompat prefAdminmode;
    private EditTextPreference btnModifyBalance;
    private EditTextPreference btnCreateTransaction;
    private Preference btnShowWelcome;
    private Preference textUserId;
    private Preference textVersion;
    private FragmentListener listener;

    public final static String SETTING_USERNAME = "pref_username";
    public final static String SETTING_PIN = "pref_password";
    public final static String SETTING_ADMINMODE = "pref_adminmode";

    private static final String ADMIN_PASSWORD = "83fd9a2bf188c54614f77cc00ed7a512";
    private static final String SALT = "p1hbw5";

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
    public void onCreatePreferences(Bundle bundle, String settingsString/* ?? */) {
        addPreferencesFromResource(R.xml.preferences);

        prefUsername = (EditTextPreference) findPreference("pref_username");
        prefPassword = (EditTextPreference) findPreference("pref_password");
        prefAdminmode = (SwitchPreferenceCompat) findPreference("pref_adminmode");
        btnModifyBalance = (EditTextPreference) findPreference("button_modify_balance");
        btnCreateTransaction = (EditTextPreference) findPreference("button_create_transaction");
        btnShowWelcome = findPreference("button_show_welcome");
        textUserId = findPreference("text_userid");
        textVersion = findPreference("text_version");

        prefAdminmode.setVisible(!BuildConfig.FLAVOR.equals("noAdminMode"));

        prefUsername.setOnPreferenceChangeListener((pref, value) -> {
            String s = ((String) value).trim();

            if (s.length() < 2) {
                s = "Schüler";
            } else if (s.length() > PiPayActivity.MAX_USERNAME_LENGTH) {
                s = s.substring(0, PiPayActivity.MAX_USERNAME_LENGTH);
            }

            prefUsername.setText(getRank().formatUsername(s));
            return false;
        });

        prefAdminmode.setOnPreferenceClickListener(pref -> {
            prefPassword.setEnabled(prefAdminmode.isChecked());
            btnModifyBalance.setVisible(prefAdminmode.isChecked());
            btnCreateTransaction.setVisible(prefAdminmode.isChecked());
            btnShowWelcome.setVisible(prefAdminmode.isChecked());
            prefUsername.getOnPreferenceChangeListener().onPreferenceChange(prefUsername, prefUsername.getText());
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
            String s = (String) value;
            try {
                if (s.startsWith("~")) {
                    listener.addDebt(Double.parseDouble(s.substring(1)));
                } else {
                    listener.addBalance(Double.parseDouble(s));
                }
            } catch (NumberFormatException ignored) {
            }
            return false;
        });

        btnCreateTransaction.setOnPreferenceChangeListener((pref, value) -> {
            try {
                TransactionLog.getInstance(getContext()).insert("test", Double.parseDouble((String) value), "test");
            } catch (NumberFormatException ignored) {
            }
            return false;
        });

        btnShowWelcome.setOnPreferenceClickListener(pref -> {
            getActivity().getPreferences(Context.MODE_PRIVATE).edit().putBoolean(WelcomeFragment.PREF_SHOW_WELCOME, true).apply();
            return true;
        });

        textUserId.setSummary(listener.getUserId());
        textVersion.setSummary(BuildConfig.VERSION_NAME);

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
        boolean debugMode = 0 != (getActivity().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
        if (hash(input + SALT).equals(ADMIN_PASSWORD)/* || debugMode*/) {
            prefAdminmode.setChecked(true);
            prefAdminmode.getOnPreferenceClickListener().onPreferenceClick(prefAdminmode);
            dialog.dismiss();
        } else {
            prefAdminmode.setChecked(false);
            prefAdminmode.getOnPreferenceClickListener().onPreferenceClick(prefAdminmode);
            dialog.setStatusText(R.string.settings_wrong_password);
        }

    }

    private Rank getRank() {
        if (prefAdminmode.isChecked()) {
            return Rank.ADMIN;
        } else {
            double received = TransactionLog.getInstance(getContext()).calculateTotalReceived();
            return Rank.forAmount(received);
        }
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    private static String hash(String s) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(Charset.forName("US-ASCII")), 0, s.length());
            byte[] magnitude = digest.digest();
            BigInteger bi = new BigInteger(1, magnitude);
            String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
