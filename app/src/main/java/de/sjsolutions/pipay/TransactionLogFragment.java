package de.sjsolutions.pipay;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import de.sjsolutions.pipay.util.TransactionLog;
import de.sjsolutions.pipay.util.TransactionRequest;

public class TransactionLogFragment extends ListFragment {
    private FragmentListener listener;
    private CursorAdapter adapter;

    public TransactionLogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean adminMode = listener.getSettings().getBoolean(SettingsFragment.SETTING_ADMINMODE, false);
        setHasOptionsMenu(adminMode);
        adapter = new TransactionAdapter(TransactionLog.getInstance(getContext()).getCursor());
        setListAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.getCursor().close();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (FragmentListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        listener.setTitle(R.string.title_transactionlog);
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long rowId) {
        super.onListItemClick(list, view, position, rowId);
        Cursor c = adapter.getCursor();
        c.moveToPosition(position);

        String id = c.getString(c.getColumnIndex(TransactionLog.COL_TRANSACTION_ID));
        double amount = c.getDouble(c.getColumnIndex(TransactionLog.COL_TRANSACTION_AMOUNT));
        String partner = c.getString(c.getColumnIndex(TransactionLog.COL_TRANSACTION_PARTER));

        if (amount < 0) {
            TransactionRequest tr = new TransactionRequest(id, -amount, partner);
            SendConfirmFragment scf = SendConfirmFragment.newInstance(tr);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, scf)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_transaction_log, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear_log) {
            TransactionLog log = TransactionLog.getInstance(getContext());
            log.clear();
            adapter.changeCursor(log.getCursor());
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class TransactionAdapter extends CursorAdapter {

        public TransactionAdapter(Cursor c) {
            super(getContext(), c, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.listitem_transaction, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            double amount = cursor.getDouble(cursor.getColumnIndex(TransactionLog.COL_TRANSACTION_AMOUNT));
            String partner = cursor.getString(cursor.getColumnIndex(TransactionLog.COL_TRANSACTION_PARTER));

            TextView textAmount = (TextView) view.findViewById(R.id.tl_text_amount);
            textAmount.setText(formatAmount(amount));
            textAmount.setTextColor(amount > 0 ? 0xFF00AA00 : 0xFFFF0000);

            TextView textPartner = (TextView) view.findViewById(R.id.tl_text_partner);
            String prefix = amount > 0 ? getString(R.string.tl_text_from) : getString(R.string.tl_text_to);
            textPartner.setText(prefix + partner);
        }

        private String formatAmount(double amount) {
            String str = String.valueOf(amount).replace('.', ',') + getString(R.string.currency);
            if (amount > 0)
                str = "+" + str;
            return str;
        }
    }
}
