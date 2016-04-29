package de.sjsolutions.pipay;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class WelcomeFragment extends Fragment {
    private FragmentListener listener;
    private Button btnNext;
    private EditText inputUsername;
    private EditText inputPIN;

    public final static String PREF_SHOW_WELCOME = "show_welcome";

    public WelcomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
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
        toolbar.setTitle("");
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getPreferences(Context.MODE_PRIVATE).edit().putBoolean(PREF_SHOW_WELCOME, false).apply();
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        defaultPref.edit()
                .putString(SettingsFragment.SETTING_USERNAME, inputUsername.getText().toString())
                .putString(SettingsFragment.SETTING_PIN, inputPIN.getText().toString())
                .apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_welcome, container, false);
        btnNext = (Button) root.findViewById(R.id.wc_button_next);
        inputUsername = (EditText) root.findViewById(R.id.wc_input_username);
        inputPIN = (EditText) root.findViewById(R.id.wc_input_pin);
        btnNext.setOnClickListener(view -> {
            if (inputUsername.length() == 0) {
                Snackbar.make(inputUsername, R.string.wc_label_enter_name, Snackbar.LENGTH_SHORT).show();
                return;
            }

            Fragment mainFragment = new MainFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainFragment)
                    .commit();
        });
        return root;
    }

}
