package de.sjsolutions.pipay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Random;

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

        final String currency = getString(R.string.currency);
        EditText inputAmount = (EditText) root.findViewById(R.id.input_amount);
        ImageView imageQrCode = (ImageView) root.findViewById(R.id.image_qrcode);
        TextView textEnterAmount = (TextView) root.findViewById(R.id.text_enter_amount);

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
                    Snackbar.make(inputAmount, R.string.toast_invalid_amount, Snackbar.LENGTH_LONG).show();
                    return false;
                }
                BitmapDrawable qrCode = generateQRCode(amount, 256);
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

    private BitmapDrawable generateQRCode(double amount, int size) throws IOException, WriterException {
        String id = generateId(16);

        StringWriter sw = new StringWriter(32);
        JsonWriter writer = new JsonWriter(sw);
        writer.beginObject()
                .name("id").value(id)
                .name("amount").value(amount)
                .name("recipient").value(username)
                .endObject().close();
        String json = sw.toString();

        BitMatrix bitMatrix = new MultiFormatWriter().encode(json, BarcodeFormat.QR_CODE, size, size);
        Log.d("[PiPay RIF]", bitMatrix.getWidth() + " " + bitMatrix.getHeight());
        int[] pixels = new int[size * size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                pixels[i * size + j] = bitMatrix.get(j, i) ? 0xFF000000 : 0x00000000;
            }
        }

        Bitmap qrCode = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        qrCode.setPixels(pixels, 0, size, 0, 0, size, size);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), qrCode);
        drawable.setAntiAlias(false);
        return drawable;
    }

    private String generateId(int length) {
        String symbols = "abcdefghijklmnopqrstuvwxyz0123456789";
        char[] buffer = new char[length];
        for (int i = 0; i < length; i++) {
            buffer[i] = symbols.charAt(random.nextInt(symbols.length()));
        }
        return new String(buffer);
    }

}
