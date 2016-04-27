package de.sjsolutions.pipay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

import de.sjsolutions.pipay.util.QRUtils;
import de.sjsolutions.pipay.util.TransactionConfirmation;
import de.sjsolutions.pipay.util.TransactionRequest;

public class ReceiveConfirmFragment extends Fragment {
    private CompoundBarcodeView qrScanner;
    private ImageView imageQrCode;
    private ImageButton btnScanAgain;

    private TextView textStatus;
    private FragmentListener listener;
    private TransactionRequest request;

    public ReceiveConfirmFragment() {
    }

    public static ReceiveConfirmFragment newInstance(TransactionRequest tr) {
        ReceiveConfirmFragment rcFragment = new ReceiveConfirmFragment();
        Bundle args = new Bundle();
        args.putParcelable("request", tr);
        rcFragment.setArguments(args);
        return rcFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        request = getArguments().getParcelable("request");
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
        ab.setTitle(R.string.title_receive_confirm);
        qrScanner.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        qrScanner.pause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_receive_confirm, container, false);

        qrScanner = (CompoundBarcodeView) root.findViewById(R.id.rc_qrscanner);
        imageQrCode = (ImageView) root.findViewById(R.id.rc_image_qrcode);
        btnScanAgain = (ImageButton) root.findViewById(R.id.rc_button_scan_again);
        textStatus = (TextView) root.findViewById(R.id.rc_text_status);

        qrScanner.getStatusView().setVisibility(View.INVISIBLE);
        qrScanner.decodeContinuous(onScan);

        btnScanAgain.setOnClickListener(view -> {
            textStatus.setText(R.string.rc_text_scan_code);
            imageQrCode.setVisibility(View.INVISIBLE);
            btnScanAgain.setVisibility(View.INVISIBLE);
            qrScanner.decodeContinuous(onScan);
        });

        return root;
    }

    private BarcodeCallback onScan = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            qrScanner.getBarcodeView().stopDecoding();
            TransactionConfirmation tr = QRUtils.decodeTransactionConfirmation(result);
            if (tr == null) {
                qrScanner.decodeContinuous(this);
            } else {
                processTransactionConfirmation(tr, result.getBitmapWithResultPoints(Color.RED));
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    private void processTransactionConfirmation(TransactionConfirmation tc, Bitmap qrCode) {
        imageQrCode.setImageBitmap(qrCode);
        imageQrCode.setVisibility(View.VISIBLE);

        if (tc.id.equals(request.id) && tc.amount == request.amount) {
            listener.addBalance(tc.amount);
            String amount = String.valueOf(tc.amount).replace('.', ',') + getString(R.string.currency);
            listener.showSnackbar(getString(R.string.rc_sb_transaction_success, amount, tc.sender));
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            textStatus.setText(R.string.rc_text_invalid_code);
            btnScanAgain.setVisibility(View.VISIBLE);
        }
    }

}
