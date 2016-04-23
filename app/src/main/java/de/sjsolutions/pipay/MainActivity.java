package de.sjsolutions.pipay;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements FragmentListener {
    private Toolbar toolbar;
    private double balance = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            balance = savedInstanceState.getDouble("balance", 0.0);
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

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        Fragment mainFragment = new MainFragment();
        fm.beginTransaction()
                .replace(R.id.fragment_container, mainFragment)
                //.addToBackStack(null)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putDouble("balance", balance);
    }

    @Override
    public void addBalance(double amount) {
        balance = Math.max(0.0, balance + amount);
    }

    @Override
    public double getBalance() {
        return balance;
    }
}
