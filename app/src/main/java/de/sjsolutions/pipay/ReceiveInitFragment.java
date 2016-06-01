package de.sjsolutions.pipay;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private TextView textFee;
    private Button btnScanConfirmation;
    private ImageButton btnEnterAmount;

    private String username;
    private boolean qrCodeGenerated = false;
    private TransactionRequest request;
    private FragmentListener listener;

    public ReceiveInitFragment() {
    }

    public static ReceiveInitFragment newInstance(TransactionRequest request) {
        ReceiveInitFragment rif = new ReceiveInitFragment();
        Bundle args = new Bundle();
        args.putParcelable("request", request);
        rif.setArguments(args);
        return rif;
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
        listener.setTitle(R.string.title_receive_init);
        username = listener.getSettings().getString(SettingsFragment.SETTING_USERNAME, "Schüler");
        if (getArguments() != null) {
            request = getArguments().getParcelable("request");
        }
        if (request != null)
            generateQRCode();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_receive_init, container, false);
        String currency = getString(R.string.currency);

        inputAmount = (EditText) root.findViewById(R.id.ri_input_amount);
        imageQrCode = (ImageView) root.findViewById(R.id.ri_image_qrcode);
        textEnterAmount = (TextView) root.findViewById(R.id.ri_text_enter_amount);
        textFee = (TextView) root.findViewById(R.id.ri_text_fee);
        btnEnterAmount = (ImageButton) root.findViewById(R.id.ri_button_enter_amount);
        btnScanConfirmation = (Button) root.findViewById(R.id.ri_button_scan_confirmation);

        inputAmount.addTextChangedListener(onAmountChanged);

        inputAmount.setOnEditorActionListener((view, action, event) -> {
            if (!qrCodeGenerated)
                onAmountEntered();
            return false;
        });

        inputAmount.setOnClickListener(view -> {
            int sel = inputAmount.length() - currency.length();
            if (inputAmount.length() >= currency.length() && inputAmount.getSelectionEnd() > sel)
                inputAmount.setSelection(inputAmount.length() - currency.length());
        });

        btnEnterAmount.setOnClickListener(view -> {
            if (!qrCodeGenerated) {
                onAmountEntered();
            } else {
                inputAmount.setText("");
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
        private String beforeText = "";
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            beforeText = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String currency = getString(R.string.currency);
            String text = s.toString();
            String text2 = text.replace('.', ',').replaceAll("[^0-9,]", "").replaceAll(",(?=.*,)", "") + currency;
            if (!text.equals(text2)) {
                inputAmount.setText(text2);
                inputAmount.setSelection(text2.length() - currency.length());
            }
            if (!beforeText.isEmpty() || text.equals(currency))
                setQrCodeGenerated(false);
        }
    };

    private void onAmountEntered() {
        String amountStr = inputAmount.getText().toString().replaceAll("[^0-9,]", "").replace(',', '.');
        if (!amountStr.isEmpty() && !amountStr.equals(".")) {
            double amount = Double.parseDouble(amountStr);
            if (amount > 0) {
                double net = Math.ceil(amount * (1 - PiPayActivity.TRANSACTION_FEE) * 100) / 100;
                textFee.setText(String.valueOf(net).replace('.', ',') + getString(R.string.currency));
                request = new TransactionRequest(amount, username);
                generateQRCode();
                return;
            }
        }
        Snackbar.make(inputAmount, R.string.ri_sb_invalid_amount, Snackbar.LENGTH_SHORT).show();
    }

    private void generateQRCode() {
        BitmapDrawable qrCode = null;
        try {
            qrCode = new BitmapDrawable(getResources(), QRUtils.encodeTransactionRequest(request));
        } catch (IOException | WriterException e) {
            return;
        }
        qrCode.setAntiAlias(false);
        imageQrCode.setImageDrawable(qrCode);
        setQrCodeGenerated(true);
    }

    private void setQrCodeGenerated(boolean generated) {
        qrCodeGenerated = generated;
        if (generated) {
            textEnterAmount.setVisibility(View.INVISIBLE);
            btnScanConfirmation.setEnabled(true);
            Drawable clear = ContextCompat.getDrawable(getContext(), R.drawable.ic_clear_black_24dp);
            btnEnterAmount.setImageDrawable(clear);
        } else {
            imageQrCode.setImageDrawable(null);
            textEnterAmount.setVisibility(View.VISIBLE);
            textFee.setText("");
            btnScanConfirmation.setEnabled(false);
            Drawable clear = ContextCompat.getDrawable(getContext(), R.drawable.ic_done_black_24dp);
            btnEnterAmount.setImageDrawable(clear);
        }
    }

    public boolean isQrCodeGenerated() {
        return qrCodeGenerated;
    }
}
