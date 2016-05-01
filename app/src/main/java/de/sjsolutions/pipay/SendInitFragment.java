package de.sjsolutions.pipay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

import de.sjsolutions.pipay.util.QRUtils;
import de.sjsolutions.pipay.util.TransactionLog;
import de.sjsolutions.pipay.util.TransactionRequest;

public class SendInitFragment extends Fragment {
    private CompoundBarcodeView qrScanner;
    private ImageView imageQrCode;
    private TextView textStatus;
    private TableLayout tableResult;
    private TextView textAmount;
    private TextView textReceiver;
    private ImageButton btnScanAgain;
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

        qrScanner = (CompoundBarcodeView) root.findViewById(R.id.si_qrscanner);
        imageQrCode = (ImageView) root.findViewById(R.id.si_image_qrcode);
        textStatus = (TextView) root.findViewById(R.id.si_text_status);
        tableResult = (TableLayout) root.findViewById(R.id.si_table_result);
        textAmount = (TextView) root.findViewById(R.id.si_text_amount);
        textReceiver = (TextView) root.findViewById(R.id.si_text_receiver);
        btnScanAgain = (ImageButton) root.findViewById(R.id.si_button_scan_again);
        btnPay = (Button) root.findViewById(R.id.si_button_pay);

        qrScanner.getStatusView().setVisibility(View.INVISIBLE);
        qrScanner.decodeContinuous(onScan);

        btnScanAgain.setOnClickListener(view -> reset());

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

    private BarcodeCallback onScan = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            qrScanner.getBarcodeView().stopDecoding();
            TransactionRequest tr = QRUtils.decodeTransactionRequest(result);
            if (tr == null) {
                qrScanner.decodeContinuous(this);
            } else {
                processTransactionRequest(tr, result.getBitmapWithResultPoints(Color.RED));
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    private void processTransactionRequest(TransactionRequest tr, Bitmap qrCode) {
        request = tr;

        textAmount.setText(String.valueOf(request.amount).replace('.', ',') + getString(R.string.currency));
        textReceiver.setText(request.receiver);
        tableResult.setVisibility(View.VISIBLE);

        btnScanAgain.setVisibility(View.VISIBLE);

        if (adminMode || listener.getBalance() >= request.amount) {
            btnPay.setEnabled(true);
            textStatus.setVisibility(View.GONE);
        } else {
            textStatus.setText(R.string.si_text_not_enough_money);
        }

        imageQrCode.setImageBitmap(qrCode);
        imageQrCode.setVisibility(View.VISIBLE);
    }

    private void showPinDialog() {
        //TODO: extract dialog (http://stackoverflow.com/questions/13733304/callback-to-a-fragment-from-a-dialogfragment)
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.dialog_enter_pin, null);
        EditText inputPin = (EditText) root.findViewById(android.R.id.edit);
        TextView textDialogStatus = (TextView) root.findViewById(android.R.id.message);

        AlertDialog enterPinDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.si_dialog_enter_pin)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                    dialog.cancel();
                })
                .setView(root)
                .create();
        enterPinDialog.show();
        enterPinDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String input = inputPin.getText().toString();
            if (inputPin.getText().toString().equals(pin)) {
                enterPinDialog.dismiss();
                pay();
            } else {
                textDialogStatus.setText(R.string.si_dialog_wrong_pin);
                textDialogStatus.setVisibility(View.VISIBLE);
            }
        });
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
        btnScanAgain.setVisibility(View.INVISIBLE);
        btnPay.setEnabled(false);
        textStatus.setText(R.string.si_text_scan_code);
        textStatus.setVisibility(View.VISIBLE);
        tableResult.setVisibility(View.GONE);
        textAmount.setText("");
        textReceiver.setText("");
        imageQrCode.setVisibility(View.INVISIBLE);
        qrScanner.decodeContinuous(onScan);
    }

}
