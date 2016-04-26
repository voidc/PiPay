package de.sjsolutions.pipay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

import de.sjsolutions.pipay.util.QRUtils;
import de.sjsolutions.pipay.util.TransactionRequest;

public class SendInitFragment extends Fragment {
    private CompoundBarcodeView qrScanner;
    private ImageView imageQrCode;
    private TextView textAmount;
    private TextView textReceiver;
    private Button btnScanAgain;
    private Button btnPay;

    private FragmentListener listener;
    private TransactionRequest currentRequest;
    private String pin;

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
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setTitle(R.string.title_send_init);
        pin = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pref_password", "");
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
        textAmount = (TextView) root.findViewById(R.id.si_text_amount);
        textReceiver = (TextView) root.findViewById(R.id.si_text_receiver);
        btnScanAgain = (Button) root.findViewById(R.id.si_button_scan_again);
        btnPay = (Button) root.findViewById(R.id.si_button_pay);

        qrScanner.getStatusView().setVisibility(View.INVISIBLE);
        qrScanner.decodeContinuous(onScan);

        btnScanAgain.setOnClickListener(view -> reset());

        btnPay.setOnClickListener(view -> {
            if (listener.getBalance() >= currentRequest.amount)
                showPinDialog();
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

    private void processTransactionRequest(TransactionRequest request, Bitmap qrCode) {
        currentRequest = request;

        textAmount.setText(getText(R.string.si_text_amount) + String.valueOf(request.amount).replace('.', ',') +
                getString(R.string.currency));
        textReceiver.setText(getText(R.string.si_text_receiver) + request.receiver);

        btnScanAgain.setEnabled(true);

        if (listener.getBalance() >= request.amount) { //TODO: don't check in admin mode
            btnPay.setEnabled(true);
        } else {
            textAmount.setText(R.string.si_text_not_enough_money);
        }

        imageQrCode.setImageBitmap(qrCode);
        imageQrCode.setVisibility(View.VISIBLE);
    }

    private void showPinDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.dialog_enter_pin, null);
        EditText inputPin = (EditText) root.findViewById(android.R.id.edit);
        TextView textStatus = (TextView) root.findViewById(android.R.id.message);

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
            Log.d("PiPay PinDialog", input + " = " + pin);
            if (inputPin.getText().toString().equals(pin)) {
                enterPinDialog.dismiss();
                pay();
            } else {
                textStatus.setText(R.string.si_dialog_wrong_pin);
                textStatus.setVisibility(View.VISIBLE);
            }
        });
    }

    private void pay() {
        listener.addBalance(-currentRequest.amount);
        reset();
        Snackbar.make(getView(), "Transaktion erfolgreich!", Snackbar.LENGTH_LONG).show();
    }

    private void reset() {
        currentRequest = null;
        btnScanAgain.setEnabled(false);
        btnPay.setEnabled(false);
        textAmount.setText(R.string.si_text_scan_code);
        textReceiver.setText("");
        imageQrCode.setVisibility(View.INVISIBLE);
        qrScanner.decodeContinuous(onScan);
    }

/*    public static class EnterPinDialog extends DialogFragment {
        private boolean authorized = false;
        private DialogInterface.OnDismissListener dialogListener;

        public EnterPinDialog() {
        }

        public EnterPinDialog(DialogInterface.OnDismissListener dialogListener) { //halts maul
            this.dialogListener = dialogListener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if(dialogListener == null)
                dismiss();

            String pin = getActivity().getPreferences(Context.MODE_PRIVATE).getString("pref_password", "");
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View root = inflater.inflate(R.layout.dialog_enter_pin, null);
            EditText inputPin = (EditText) root.findViewById(android.R.id.edit);
            TextView textStatus = (TextView) root.findViewById(android.R.id.message);

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.si_dialog_enter_pin)
                    .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                        if(inputPin.getText().equals(pin)) {
                            authorized = true;
                            getDialog().dismiss();
                        } else {
                            textStatus.setText(R.string.si_dialog_wrong_pin);
                            textStatus.setVisibility(View.VISIBLE);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                        getDialog().cancel();
                    })
                    .setView(root)
                    .setOnDismissListener(dialogListener)
                    .create();
        }

        public boolean isAuthorized() {
            return authorized;
        }
    }*/

}
