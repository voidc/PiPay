package de.sjsolutions.pipay;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainFragment extends Fragment {
    private FragmentListener listener;

    public MainFragment() {}

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
        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        toolbar.setTitle(R.string.app_name);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_main, container, false);

        TextView textBalance = (TextView) root.findViewById(R.id.text_balance);
        textBalance.setText(listener.getBalance() + " ST");

        Button btnSend = (Button) root.findViewById(R.id.button_send);
        btnSend.setOnClickListener(view -> {
            Fragment sendInitFragment = new SendInitFragment();
            MainFragment.this.getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, sendInitFragment)
                    .addToBackStack(null)
                    .commit();
        });

        Button btnReceive = (Button) root.findViewById(R.id.button_receive);
        btnReceive.setOnClickListener(view -> {
            Fragment receiveInitFragment = new ReceiveInitFragment();
            MainFragment.this.getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, receiveInitFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_adminmode:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
