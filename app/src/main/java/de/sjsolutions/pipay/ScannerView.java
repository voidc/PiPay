package de.sjsolutions.pipay;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.DecoderFactory;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Collections;
import java.util.List;

import de.sjsolutions.pipay.util.QRUtils;
import de.sjsolutions.pipay.util.TransactionConfirmation;
import de.sjsolutions.pipay.util.TransactionRequest;

public class ScannerView extends FrameLayout {
    private BarcodeView barcodeView;
    private ImageView imageResult;
    private ImageButton btnRetry;

    private ScannerResultListener resultListener;
    private ScannerResetListener resetListener;
    private ScannerType type;
    private int colorAccent;

    public ScannerView(Context context) {
        super(context);
        initializeView();
    }

    public ScannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView();
    }

    public ScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView();
    }

    private void initializeView() {
        inflate(getContext(), R.layout.view_qrscanner, this);
        barcodeView = (BarcodeView) findViewById(R.id.scanner_barcode_view);
        imageResult = (ImageView) findViewById(R.id.scanner_image_result);
        btnRetry = (ImageButton) findViewById(R.id.scanner_btn_retry);

        DecoderFactory df = new DefaultDecoderFactory(Collections.singleton(BarcodeFormat.QR_CODE), null, null);
        barcodeView.setDecoderFactory(df);

        btnRetry.setOnClickListener(view -> {
            resetListener.onReset();
            scan();
        });

        colorAccent = ContextCompat.getColor(getContext(), R.color.colorAccent);
    }

    public void pause() {
        barcodeView.pause();
    }

    public void resume() {
        barcodeView.resume();
    }

    public void setResetListener(ScannerResetListener resetListener) {
        this.resetListener = resetListener;
    }

    public <T> void startScan(ScannerType type, ScannerResultListener<T> listener) {
        this.type = type;
        this.resultListener = listener;
        scan();
    }

    private void scan() {
        imageResult.setVisibility(INVISIBLE);
        btnRetry.setVisibility(INVISIBLE);
        barcodeView.decodeContinuous(onScan);
    }

    private void setResult(BarcodeResult result) {
        imageResult.setImageBitmap(result.getBitmapWithResultPoints(colorAccent));
        imageResult.setVisibility(VISIBLE);
        btnRetry.setVisibility(VISIBLE);
    }

    private BarcodeCallback onScan = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            barcodeView.stopDecoding();
            switch (type) {
                case TRANSACTION_REQUEST:
                    TransactionRequest tr = QRUtils.decodeTransactionRequest(result);
                    if (tr != null) {
                        setResult(result);
                        resultListener.onResult(tr);
                    } else {
                        barcodeView.decodeContinuous(onScan);
                    }
                    break;
                case TRANSACTION_CONFIRMATION:
                    TransactionConfirmation tc = QRUtils.decodeTransactionConfirmation(result);
                    if (tc != null) {
                        setResult(result);
                        resultListener.onResult(tc);
                    } else {
                        barcodeView.decodeContinuous(onScan);
                    }
                    break;
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    public enum ScannerType {
        TRANSACTION_REQUEST,
        TRANSACTION_CONFIRMATION
    }

    public interface ScannerResultListener<T> {
        void onResult(T result);
    }

    public interface ScannerResetListener {
        void onReset();
    }
}
