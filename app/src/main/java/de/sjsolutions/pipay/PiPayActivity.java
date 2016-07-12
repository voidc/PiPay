package de.sjsolutions.pipay;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

import de.sjsolutions.pipay.util.TransactionLog;

public class PiPayActivity extends AppCompatActivity implements FragmentListener {
    private Toolbar toolbar;
    private double balance = 0.0;
    private double debt = 0.0;
    private SharedPreferences settings;

    private final int CAM_PERMISSION_REQUEST = 1;
    public static final String PREF_BALANCE = "balance";
    public static final String PREF_DEBT = "debt";

    // ### CONSTANTS ###
    public static final double TRANSACTION_FEE = 0.05;
    public static final double INTEREST = 0.1;
    public static final double MAX_AMOUNT = 150.0;
    public static final int MAX_USERNAME_LENGTH = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pipay);

        TransactionLog.getInstance(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();

        fm.addOnBackStackChangedListener(() -> {
            boolean up = fm.getBackStackEntryCount() > 0;
            ActionBar ab = getSupportActionBar();
            ab.setDisplayShowHomeEnabled(up);
            ab.setDisplayHomeAsUpEnabled(up);
        });

        toolbar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        Fragment startFragment;
        boolean showWelcome = getPreferences(Context.MODE_PRIVATE).getBoolean(WelcomeFragment.PREF_SHOW_WELCOME, true);
        if (showWelcome) {
            startFragment = new WelcomeFragment();
        } else {
            startFragment = new MenuFragment();
        }

        fm.beginTransaction()
                .replace(R.id.fragment_container, startFragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        balance = Double.longBitsToDouble(getPreferences(Context.MODE_PRIVATE).getLong(PREF_BALANCE, 0));
        debt = Double.longBitsToDouble(getPreferences(Context.MODE_PRIVATE).getLong(PREF_DEBT, 0));
        onSettingsChanged();

        int camPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (camPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAM_PERMISSION_REQUEST);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveBalance();
    }

    private void saveBalance() {
        getPreferences(Context.MODE_PRIVATE).edit()
                .putLong(PREF_BALANCE, Double.doubleToRawLongBits(balance))
                .putLong(PREF_DEBT, Double.doubleToRawLongBits(debt))
                .commit();
    }

    @Override
    public void addBalance(double amount) {
        if (balance == Double.NaN) balance = 0;
        double delta = amount - debt;
        if (amount > 0) {
            amount = Math.max(delta, 0);
            debt = Math.max(-delta, 0);
        } else if (debt < 0) {
            amount = Math.min(delta, 0);
            debt = Math.min(-delta, 0);
        }
        balance = Math.max(0.0, balance + amount);
        saveBalance();
    }

    @Override
    public void addDebt(double amount) {
        debt += amount;
        saveBalance();
    }

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public double getDebt() {
        return debt;
    }

    @Override
    public void setTitle(int titleId) {
        toolbar.setTitle(titleId);
    }

    @Override
    public void setTitle(String title) {
        toolbar.setTitle(title);
    }

    public SharedPreferences getSettings() {
        return settings;
    }

    @Override
    public void onSettingsChanged() {
        settings = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void showSnackbar(String text) {
        Snackbar.make(findViewById(R.id.fragment_container), text, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAM_PERMISSION_REQUEST) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() { //TODO: Code noch etwas unschön
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof ReceiveInitFragment) {
            if (((ReceiveInitFragment) currentFragment).isQrCodeGenerated()) {
                Snackbar.make(toolbar, "Transaktion abbrechen?", Snackbar.LENGTH_LONG)
                        .setAction("Ja", view -> {
                            super.onBackPressed();
                        }).show();
                return;
            }
        }
        super.onBackPressed();
    }

}
