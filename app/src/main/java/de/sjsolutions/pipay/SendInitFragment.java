package de.sjsolutions.pipay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        qrScanner.decodeContinuous(new BarcodeCallback() {
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
        });
        return root;
    }

    private void processTransactionRequest(TransactionRequest request, Bitmap qrCode) {
        textAmount.setText(getText(R.string.si_text_amount) + String.valueOf(request.amount).replace('.', ',') +
                getString(R.string.currency));
        textReceiver.setText(getText(R.string.si_text_receiver) + request.receiver);

        btnScanAgain.setEnabled(true);

        if (listener.getBalance() >= request.amount) { //TODO: don't check in admin mode
            btnPay.setEnabled(true);
        }

        imageQrCode.setImageBitmap(qrCode);
        imageQrCode.setVisibility(View.VISIBLE);
    }

}
