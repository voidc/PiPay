package de.sjsolutions.pipay;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.sjsolutions.pipay.util.Rank;
import de.sjsolutions.pipay.util.TransactionConfirmation;
import de.sjsolutions.pipay.util.TransactionLog;
import de.sjsolutions.pipay.util.TransactionRequest;

public class ReceiveConfirmFragment extends Fragment {
    private ScannerView qrScanner;
    private TextView textStatus;

    private FragmentListener listener;
    private TransactionRequest request;
    private boolean adminMode;

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
        listener.setTitle(R.string.title_receive_confirm);
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
        View root = inflater.inflate(R.layout.fragment_receive_confirm, container, false);

        qrScanner = (ScannerView) root.findViewById(R.id.rc_qrscanner);
        textStatus = (TextView) root.findViewById(R.id.rc_text_status);

        qrScanner.setResetListener(() -> textStatus.setText(R.string.rc_text_scan_code));
        qrScanner.startScan(ScannerView.ScannerType.TRANSACTION_CONFIRMATION, this::processTransactionConfirmation);

        return root;
    }

    private void processTransactionConfirmation(TransactionConfirmation tc) {
        if (tc.id.equals(request.id) && tc.amount == request.amount) {
            double net = Math.ceil(tc.amount * (1 - PiPayActivity.TRANSACTION_FEE) * 100) / 100;
            if (!adminMode)
                listener.addBalance(net);
            TransactionLog.getInstance(getContext()).insert(tc.id, net, tc.sender);
            if (tc.sender.startsWith(Rank.ADMIN.symbol + "~")) {
                listener.addDebt(tc.amount * (1 + PiPayActivity.INTEREST));
            }
            String amount = String.valueOf(net).replace('.', ',') + getString(R.string.currency);
            listener.showSnackbar(getString(R.string.rc_sb_transaction_success, amount, tc.sender));
            getActivity().getSupportFragmentManager()
                    .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            textStatus.setText(R.string.rc_text_invalid_code);
        }
    }

}
