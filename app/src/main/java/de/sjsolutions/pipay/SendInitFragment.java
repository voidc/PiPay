package de.sjsolutions.pipay;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class SendInitFragment extends Fragment {
    private CompoundBarcodeView qrScanner;
    private ImageView imageQrCode;
    private TextView textAmount;
    private TextView textReceiver;

    public SendInitFragment() {
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

        qrScanner.getStatusView().setVisibility(View.INVISIBLE);
        qrScanner.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                Snackbar.make(root, result.getText(), Snackbar.LENGTH_LONG).show();
                if (!processQRCode(result)) {
                    //bad qr code TODO: scan again
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {

            }
        });
        return root;
    }

    private boolean processQRCode(BarcodeResult qrCode) {
        String id = null;
        double amount = 0;
        String receiver = null;

        JsonReader parser = new JsonReader(new StringReader(qrCode.getText()));
        try {
            parser.beginObject();
            while (parser.hasNext()) {
                switch (parser.nextName()) {
                    case "id":
                        id = parser.nextString();
                        break;
                    case "amount":
                        amount = parser.nextDouble();
                        break;
                    case "receiver":
                        receiver = parser.nextString();
                        break;
                }
            }
            parser.endObject();
            parser.close();
        } catch (IOException e) {
            return false;
        }

        textAmount.setText(getText(R.string.si_text_amount) + String.valueOf(amount).replace('.', ',') +
                getString(R.string.currency));
        textReceiver.setText(getText(R.string.si_text_receiver) + receiver);

        imageQrCode.setImageBitmap(qrCode.getBitmapWithResultPoints(Color.RED));
        imageQrCode.setVisibility(View.VISIBLE);
        return true;
    }

}
