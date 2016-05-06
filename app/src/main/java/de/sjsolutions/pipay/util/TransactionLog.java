package de.sjsolutions.pipay.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class TransactionLog extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "PiPay.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "tlog";
    public static final String COL_TRANSACTION_ID = "t_id";
    public static final String COL_TRANSACTION_AMOUNT = "t_amount";
    public static final String COL_TRANSACTION_PARTER = "t_partner";

    private final String SQL_MATCH_TRANSACTION = COL_TRANSACTION_ID + " = ?"; //ids must be globally unique

    private static TransactionLog instance;

    private TransactionLog(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static TransactionLog getInstance(Context context) {
        if (instance == null) {
            instance = new TransactionLog(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_NAME + "(" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COL_TRANSACTION_ID + " VARCHAR(" + QRUtils.ID_LENGTH + ")," +
                        COL_TRANSACTION_AMOUNT + " REAL," +
                        COL_TRANSACTION_PARTER + " VARCHAR(100)" +
                        ")"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insert(Transaction transaction) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues entry = new ContentValues();
        entry.put(COL_TRANSACTION_ID, transaction.id);
        entry.put(COL_TRANSACTION_AMOUNT, transaction.amount);
        entry.put(COL_TRANSACTION_PARTER, transaction.partner);
        db.insert(TABLE_NAME, null, entry);
    }

    public void insert(String id, double amount, String partner) {
        insert(new Transaction(id, amount, partner));
    }

    public boolean contains(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + SQL_MATCH_TRANSACTION, new String[]{id});
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public Transaction findById(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + SQL_MATCH_TRANSACTION, new String[]{id});
        if (c.getCount() <= 0) {
            c.close();
            return null;
        }
        c.moveToFirst();
        double amount = c.getDouble(c.getColumnIndex(COL_TRANSACTION_AMOUNT));
        String partner = c.getString(c.getColumnIndex(COL_TRANSACTION_PARTER));
        c.close();
        return new Transaction(id, amount, partner);
    }

    public void clear() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public Cursor getCursor() {
        SQLiteDatabase db = getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public class Transaction {
        public final String id;
        public final double amount;
        public final String partner;

        public Transaction(String id, double amount, String partner) {
            this.id = id;
            this.amount = amount;
            this.partner = partner;
        }
    }

}
