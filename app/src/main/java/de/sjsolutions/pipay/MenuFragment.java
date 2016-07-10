package de.sjsolutions.pipay;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class MenuFragment extends Fragment {
    private FragmentListener listener;
    private TextView textBalance;
    private TextView textUsername;
    private boolean adminMode;

    public MenuFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (FragmentListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        listener.setTitle(R.string.app_name);
        adminMode = listener.getSettings().getBoolean(SettingsFragment.SETTING_ADMINMODE, false);
        textBalance.setText(formatBalance(listener.getBalance()));
        textUsername.setText(listener.getSettings().getString(SettingsFragment.SETTING_USERNAME, "Schüler"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_menu, container, false);

        textBalance = (TextView) root.findViewById(R.id.text_balance);
        textUsername = (TextView) root.findViewById(R.id.text_username);

        Button btnSend = (Button) root.findViewById(R.id.button_send);
        btnSend.setOnClickListener(view -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SendInitFragment())
                    .addToBackStack(null)
                    .commit();
        });

        Button btnReceive = (Button) root.findViewById(R.id.button_receive);
        btnReceive.setOnClickListener(view -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ReceiveInitFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return root;
    }

    private String formatBalance(double balance) {
        return adminMode ? "∞" : String.format("%.2f%s", balance, getString(R.string.currency));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.action_tlog:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TransactionLogFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareApp() {
        ApplicationInfo app = getActivity().getApplicationContext().getApplicationInfo();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.setPackage("com.android.bluetooth");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(app.sourceDir)));
        startActivity(Intent.createChooser(intent, getText(R.string.main_send_app)));
    }
}
