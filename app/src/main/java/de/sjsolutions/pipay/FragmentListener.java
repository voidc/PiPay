package de.sjsolutions.pipay;

import android.content.SharedPreferences;

public interface FragmentListener {
    void addBalance(double amount);

    void addDebt(double amount);

    double getBalance();

    double getDebt();

    String getUserId();

    void showSnackbar(String text);

    void setTitle(int titleId);

    void setTitle(String title);

    SharedPreferences getSettings();

    void onSettingsChanged();
}
