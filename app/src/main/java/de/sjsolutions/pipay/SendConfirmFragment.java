package de.sjsolutions.pipay;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.WriterException;

import java.io.IOException;

import de.sjsolutions.pipay.util.QRUtils;
import de.sjsolutions.pipay.util.TransactionConfirmation;
import de.sjsolutions.pipay.util.TransactionRequest;

public class SendConfirmFragment extends Fragment {
    private ImageView imageQrCode;
    private Button btnDone;

    private String username;
    private TransactionRequest request;
    private FragmentListener listener;

    public SendConfirmFragment() {
    }

    public static SendConfirmFragment newInstance(TransactionRequest request) {
        SendConfirmFragment scf = new SendConfirmFragment();
        Bundle args = new Bundle();
        args.putParcelable("request", request);
        scf.setArguments(args);
        return scf;
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
        ab.setTitle(R.string.title_send_confirm);
        username = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pref_username", "Schüler");
        generateQRCode();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_send_confirm, container, false);

        imageQrCode = (ImageView) root.findViewById(R.id.sc_image_qrcode);
        btnDone = (Button) root.findViewById(R.id.sc_button_done);

        btnDone.setOnClickListener(view -> {
            String amount = String.valueOf(request.amount).replace('.', ',') + getString(R.string.currency);
            listener.showSnackbar(getString(R.string.sc_sb_transaction_success, amount, request.receiver));
            getActivity().getSupportFragmentManager()
                    .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });

        return root;
    }


    private void generateQRCode() {
        TransactionConfirmation tc = new TransactionConfirmation(request.id, request.amount, username);
        BitmapDrawable qrCode = null;
        try {
            qrCode = new BitmapDrawable(getResources(), QRUtils.encodeTransactionConfirmation(tc));
        } catch (IOException | WriterException e) {
            return;
        }
        qrCode.setAntiAlias(false);
        imageQrCode.setImageDrawable(qrCode);
    }

}
