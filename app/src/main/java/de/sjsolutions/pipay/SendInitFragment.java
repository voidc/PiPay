package de.sjsolutions.pipay;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.sjsolutions.pipay.util.TransactionLog;
import de.sjsolutions.pipay.util.TransactionRequest;

public class SendInitFragment extends Fragment implements InputDialogFragment.OnDialogInputListener {
    private ScannerView qrScanner;
    private TextView textStatus;
    private View tableResult;
    private TextView textReceiver;
    private TextView textAmount;
    private Button btnPay;

    private FragmentListener listener;
    private TransactionRequest request;
    private String pin;
    private boolean adminMode;

    public SendInitFragment() {
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
        listener.setTitle(R.string.title_send_init);
        pin = listener.getSettings().getString(SettingsFragment.SETTING_PIN, "");
        adminMode = listener.getSettings().getBoolean(SettingsFragment.SETTING_ADMINMODE, false);
        qrScanner.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        qrScanner.pause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_send_init, container, false);

        qrScanner = (ScannerView) root.findViewById(R.id.si_qrscanner);
        textStatus = (TextView) root.findViewById(R.id.si_text_status);
        tableResult = root.findViewById(R.id.si_table_result);
        textReceiver = (TextView) root.findViewById(R.id.si_text_receiver);
        textAmount = (TextView) root.findViewById(R.id.si_text_amount);
        btnPay = (Button) root.findViewById(R.id.si_button_pay);

        qrScanner.setResetListener(this::reset);
        qrScanner.startScan(ScannerView.ScannerType.TRANSACTION_REQUEST, this::processTransactionRequest);

        btnPay.setOnClickListener(view -> {
            if (adminMode || listener.getBalance() >= request.amount) {
                if (pin == null || pin.isEmpty())
                    pay();
                else
                    showPinDialog();
            }
        });

        return root;
    }

    private void processTransactionRequest(TransactionRequest tr) {
        request = tr;

        TransactionLog tl = TransactionLog.getInstance(getContext());
        if (tl.contains(tr.id)) {
            listener.showSnackbar(getString(R.string.si_sb_already_payed));
            SendConfirmFragment scf = SendConfirmFragment.newInstance(request);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, scf)
                    .addToBackStack(null)
                    .commit();
        }

        textAmount.setText(String.valueOf(request.amount).replace('.', ',') + getString(R.string.currency));
        textReceiver.setText(request.receiver);
        tableResult.setVisibility(View.VISIBLE);
        textStatus.setVisibility(View.INVISIBLE);

        if (adminMode || listener.getBalance() >= request.amount) {
            btnPay.setEnabled(true);
        } else {
            textAmount.setTextColor(Color.RED);
        }
    }

    private void showPinDialog() {
        InputDialogFragment pinDialog = InputDialogFragment.newInstance(R.layout.dialog_enter_pin, R.string.si_dialog_enter_pin);
        pinDialog.setTargetFragment(this, 0);
        pinDialog.show(getFragmentManager(), "PIN_DIALOG");
    }

    @Override
    public void onDialogInput(String input, InputDialogFragment dialog) {
        if (input.equals(pin)) {
            dialog.dismiss();
            pay();
        } else {
            dialog.setStatusText(R.string.si_dialog_wrong_pin);
        }
    }

    private void pay() {
        if (!adminMode)
            listener.addBalance(-request.amount);
        TransactionLog.getInstance(getContext()).insert(request.id, -request.amount, request.receiver);
        String amount = String.valueOf(request.amount).replace('.', ',') + getString(R.string.currency);
        listener.showSnackbar(getString(R.string.si_sb_transaction_success, amount, request.receiver));
        SendConfirmFragment scf = SendConfirmFragment.newInstance(request);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, scf)
                .addToBackStack(null)
                .commit();
    }

    private void reset() {
        request = null;
        btnPay.setEnabled(false);
        textStatus.setText(R.string.si_text_scan_code);
        textStatus.setVisibility(View.VISIBLE);
        tableResult.setVisibility(View.INVISIBLE);
        textAmount.setText("");
        textReceiver.setText("");
    }
}
