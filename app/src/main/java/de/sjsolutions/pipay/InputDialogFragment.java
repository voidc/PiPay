package de.sjsolutions.pipay;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class InputDialogFragment extends DialogFragment {
    private OnDialogInputListener listener;
    private EditText inputPin;
    private TextView textDialogStatus;

    public static InputDialogFragment newInstance(int layoutId, int titleId) {
        InputDialogFragment inputDialog = new InputDialogFragment();
        Bundle args = new Bundle();
        args.putInt("layout", layoutId);
        args.putInt("title", titleId);
        inputDialog.setArguments(args);
        return inputDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        listener = (OnDialogInputListener) getTargetFragment();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(args.getInt("layout"), null);
        inputPin = (EditText) root.findViewById(android.R.id.edit);
        textDialogStatus = (TextView) root.findViewById(android.R.id.message);

        AlertDialog inputDialog = new AlertDialog.Builder(getActivity())
                .setTitle(args.getInt("title"))
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                    dialog.cancel();
                })
                .setView(root)
                .create();
        inputDialog.show();
        inputDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String input = inputPin.getText().toString();
            listener.onDialogInput(input, this);
        });
        return inputDialog;
    }

    public void setStatusText(int stringId) {
        textDialogStatus.setText(stringId);
        textDialogStatus.setVisibility(View.VISIBLE);
    }

    public void hideStatusText() {
        textDialogStatus.setVisibility(View.INVISIBLE);
    }

    public interface OnDialogInputListener {
        void onDialogInput(String input, InputDialogFragment dialog);
    }
}
