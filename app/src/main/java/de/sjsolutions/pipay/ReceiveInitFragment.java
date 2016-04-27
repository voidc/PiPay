package de.sjsolutions.pipay;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;

import java.io.IOException;

import de.sjsolutions.pipay.util.QRUtils;
import de.sjsolutions.pipay.util.TransactionRequest;

public class ReceiveInitFragment extends Fragment {
    private EditText inputAmount;
    private ImageView imageQrCode;
    private TextView textEnterAmount;
    private Button btnScanConfirmation;

    private String username;
    private String currency;
    private TransactionRequest request;

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
        username = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pref_username", "Schüler");
        currency = getString(R.string.currency);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_receive_init, container, false);

        inputAmount = (EditText) root.findViewById(R.id.ri_input_amount);
        imageQrCode = (ImageView) root.findViewById(R.id.ri_image_qrcode);
        textEnterAmount = (TextView) root.findViewById(R.id.ri_text_enter_amount);
        btnScanConfirmation = (Button) root.findViewById(R.id.ri_button_scan_confirmation);

        inputAmount.addTextChangedListener(onAmountChanged);

        inputAmount.setOnEditorActionListener((view, action, event) -> {
            String amountStr = view.getText().toString().replaceAll("[^0-9,]", "").replace(',', '.');
            double amount = Double.parseDouble(amountStr);
            if (amount > 0) {
                generateQRCode(amount);
                return true;
            } else {
                Snackbar.make(inputAmount, R.string.ri_sb_invalid_amount, Snackbar.LENGTH_LONG).show();
                return false;
            }
        });

        btnScanConfirmation.setOnClickListener(view -> {
            ReceiveConfirmFragment rcf = ReceiveConfirmFragment.newInstance(request);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, rcf)
                    .addToBackStack(null)
                    .commit();
        });

        return root;
    }

    private TextWatcher onAmountChanged = new TextWatcher() {
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
            btnScanConfirmation.setEnabled(false);
            request = null;
        }
    };

    private void generateQRCode(double amount) {
        request = new TransactionRequest(amount, username);
        BitmapDrawable qrCode = null;
        try {
            qrCode = new BitmapDrawable(getResources(), QRUtils.encodeTransactionRequest(request));
        } catch (IOException | WriterException e) {
            return;
        }
        qrCode.setAntiAlias(false);
        textEnterAmount.setVisibility(View.INVISIBLE);
        btnScanConfirmation.setEnabled(true);
        imageQrCode.setImageDrawable(qrCode);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputAmount.getWindowToken(), 0);
    }

}
