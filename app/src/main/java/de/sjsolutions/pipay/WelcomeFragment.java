package de.sjsolutions.pipay;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import de.sjsolutions.pipay.util.Rank;

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
        listener.setTitle(R.string.empty);
    }

    @Override
    public void onPause() {
        super.onPause();
        listener.getSettings().edit()
                .putString(SettingsFragment.SETTING_USERNAME, inputUsername.getText().toString())
                .putString(SettingsFragment.SETTING_PIN, inputPIN.getText().toString())
                .apply();
        listener.onSettingsChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_welcome, container, false);
        btnNext = (Button) root.findViewById(R.id.wc_button_next);
        inputUsername = (EditText) root.findViewById(R.id.wc_input_username);
        inputPIN = (EditText) root.findViewById(R.id.wc_input_pin);
        btnNext.setOnClickListener(view -> {
            String username = inputUsername.getText().toString().trim();
            if (username.isEmpty() || username.matches(".*[" + Rank.EMOJIS + "].*")
                    || username.length() > PiPayActivity.MAX_USERNAME_LENGTH) {
                Snackbar.make(inputUsername, R.string.wc_label_enter_name, Snackbar.LENGTH_SHORT).show();
                return;
            }

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(inputAmount.getWindowToken(), 0);
            getActivity().getPreferences(Context.MODE_PRIVATE).edit().putBoolean(PREF_SHOW_WELCOME, false).apply();
            Fragment mainFragment = new MenuFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainFragment)
                    .commit();
        });
        return root;
    }
}
