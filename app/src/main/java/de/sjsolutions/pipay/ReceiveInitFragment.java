package de.sjsolutions.pipay;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

import de.sjsolutions.pipay.util.QRUtils;
import de.sjsolutions.pipay.util.TransactionRequest;

public class ReceiveInitFragment extends Fragment {
    private String username;
    private Random random = new Random();

    public ReceiveInitFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setTitle(R.string.title_receive_init);
        username = getActivity().getPreferences(Context.MODE_PRIVATE).getString("pref_username", "Schüler");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_receive_init, container, false);

        EditText inputAmount = (EditText) root.findViewById(R.id.ri_input_amount);
        ImageView imageQrCode = (ImageView) root.findViewById(R.id.ri_image_qrcode);
        TextView textEnterAmount = (TextView) root.findViewById(R.id.ri_text_enter_amount);

        final String currency = getString(R.string.currency);

        inputAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                String text2 = text.replace('.', ',').replaceAll("[^0-9,]", "") + currency;
                if (!text.equals(text2)) {
                    inputAmount.setText(text2);
                    inputAmount.setSelection(text2.length() - currency.length());
                }

                imageQrCode.setImageDrawable(null);
                textEnterAmount.setVisibility(View.VISIBLE);
            }
        });

        inputAmount.setOnEditorActionListener((view, action, event) -> {
            try {
                //TODO: make async
                String amountStr = view.getText().toString().replaceAll("[^0-9 ]", "").replace(',', '.');
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Snackbar.make(inputAmount, R.string.ri_sb_invalid_amount, Snackbar.LENGTH_LONG).show();
                    return false;
                }
                TransactionRequest tr = new TransactionRequest(amount, username);
                BitmapDrawable qrCode = new BitmapDrawable(getResources(), QRUtils.encodeTransactionRequest(tr));
                qrCode.setAntiAlias(false);
                textEnterAmount.setVisibility(View.INVISIBLE);
                imageQrCode.setImageDrawable(qrCode);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        return root;
    }

}
