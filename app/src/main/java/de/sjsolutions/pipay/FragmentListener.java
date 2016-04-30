package de.sjsolutions.pipay;

import android.content.SharedPreferences;

public interface FragmentListener {
    void addBalance(double amount);

    double getBalance();

    void showSnackbar(String text);

    void setTitle(int titleId);

    SharedPreferences getSettings();

    void onSettingsChanged();
}
