package de.sjsolutions.pipay;

import android.Manifest;
import android.content.Context;
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
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements FragmentListener {
    private Toolbar toolbar;
    private double balance = 0.0;
    private final int CAM_PERMISSION_REQUEST = 1;

    public static final String PREF_BALANCE = "balance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            balance = savedInstanceState.getDouble(PREF_BALANCE, 0.0);
        }

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
            startFragment = new MainFragment();
        }

        fm.beginTransaction()
                .replace(R.id.fragment_container, startFragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        balance = Double.longBitsToDouble(getPreferences(Context.MODE_PRIVATE).getLong(PREF_BALANCE, 0));

        int camPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (camPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAM_PERMISSION_REQUEST);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferences(Context.MODE_PRIVATE).edit()
                .putLong(PREF_BALANCE, Double.doubleToRawLongBits(balance))
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putDouble(PREF_BALANCE, balance);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void addBalance(double amount) {
        if (balance == Double.NaN) balance = 0;
        balance = Math.max(0.0, balance + amount);
    }

    @Override
    public double getBalance() {
        return balance;
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
