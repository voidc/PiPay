package de.sjsolutions.pipay.util;


import android.os.Environment;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

public class Backup {
    public final String userId;
    public final double balance;
    public final double debt;

    private Backup(String userId, double balance, double debt) {
        this.userId = userId;
        this.balance = balance;
        this.debt = debt;
    }

    private static final String FILE_NAME = "ppbak";
    private static byte[] ENCRYPTION_KEY;

    static {
        try {
            ENCRYPTION_KEY = "PiPayBAK".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static Backup loadBackup() {
        if (!checkExternalStorage()) {
            return null;
        }

        File backup = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
        try {
            RandomAccessFile ra = new RandomAccessFile(backup, "r");
            byte[] data = new byte[(int) ra.length()];
            ra.readFully(data);
            ra.close();

            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(cipher(data));
            String userId = unpacker.unpackString();
            double balance = unpacker.unpackDouble();
            double debt = unpacker.unpackDouble();
            unpacker.close();

            return new Backup(userId, balance, debt);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void backup(String userId, double balance, double debt) {
        if (!checkExternalStorage()) {
            return;
        }

        try {
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packString(userId)
                    .packDouble(balance)
                    .packDouble(debt)
                    .close();

            File backup = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
            FileOutputStream out = new FileOutputStream(backup);
            out.write(cipher(packer.toByteArray()));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkExternalStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private static byte[] cipher(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] ^= ENCRYPTION_KEY[i % ENCRYPTION_KEY.length];
        }
        return data;
    }
}
