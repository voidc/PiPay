package de.sjsolutions.pipay.util;


import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeResult;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class QRUtils {
    public static final int QR_SIZE = 256;
    public static final int ID_LENGTH = 16;
    public static final int TRANSACTION_REQUEST = 0;
    public static final int TRANSACTION_CONFIRMATION = 1;
    private static final String CHARSET = "UTF-8";

    private static byte[] ENCRYPTION_KEY;

    static {
        try {
            ENCRYPTION_KEY = "PiPayQR".getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static Random random = new Random();

    /* Test Codes:
    Generator: https://zxing.appspot.com/generator
    Request: {"id":"0123456789abcdef","amount":"1.23","receiver":"LuckyMe"}
    Confirmation: {"id":"<id>","amount":3.0,"sender":"MoneyBoy"}
     */

    public static TransactionRequest decodeTransactionRequest(BarcodeResult qrCode) {
        Log.d("QRUtils", "Scanned QR-Code: " + qrCode.getText());
        try {
            byte[] data = cipher(Base64.decode(qrCode.getText(), Base64.DEFAULT));
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
            int type = unpacker.unpackInt();
            if (type != TRANSACTION_REQUEST)
                return null;

            String id = unpacker.unpackString();
            double amount = unpacker.unpackDouble();
            String receiver = unpacker.unpackString();
            unpacker.close();

            if (id != null && !id.isEmpty() && amount > 0 && receiver != null && !receiver.isEmpty()) {
                return new TransactionRequest(id, amount, receiver);
            } else return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static TransactionConfirmation decodeTransactionConfirmation(BarcodeResult qrCode) {
        Log.d("QRUtils", "Scanned QR-Code: " + qrCode.getText());
        try {
            byte[] data = cipher(Base64.decode(qrCode.getText(), Base64.DEFAULT));
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
            int type = unpacker.unpackInt();
            if (type != TRANSACTION_CONFIRMATION)
                return null;

            String id = unpacker.unpackString();
            double amount = unpacker.unpackDouble();
            String sender = unpacker.unpackString();
            unpacker.close();

            if (id != null && !id.isEmpty() && amount > 0 && sender != null && !sender.isEmpty()) {
                return new TransactionConfirmation(id, amount, sender);
            } else return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] cipher(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] ^= ENCRYPTION_KEY[i % ENCRYPTION_KEY.length];
        }
        return data;
    }

    public static Bitmap encodeTransactionRequest(TransactionRequest request) throws IOException, WriterException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packInt(TRANSACTION_REQUEST)
                .packString(request.id)
                .packDouble(request.amount)
                .packString(request.receiver)
                .close();
        String serialized = Base64.encodeToString(cipher(packer.toByteArray()), Base64.DEFAULT);
        Log.d("QRUtils", "Generate QR-Code: " + serialized);
        String qrUrl = "https://zxing.org/w/chart?cht=qr&chs=350x350&chld=L&choe=UTF-8&chl=" + Uri.encode(serialized);
        Log.d("QRUtils", "QR-Code URL: " + qrUrl);


        BitMatrix bitMatrix = new MultiFormatWriter().encode(serialized, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
        int[] pixels = new int[QR_SIZE * QR_SIZE];
        for (int i = 0; i < QR_SIZE; i++) {
            for (int j = 0; j < QR_SIZE; j++) {
                pixels[i * QR_SIZE + j] = bitMatrix.get(j, i) ? 0xFF000000 : 0x00000000;
            }
        }

        Bitmap qrCode = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.ARGB_8888);
        qrCode.setPixels(pixels, 0, QR_SIZE, 0, 0, QR_SIZE, QR_SIZE);
        return qrCode;
    }

    public static Bitmap encodeTransactionConfirmation(TransactionConfirmation confirmation) throws IOException, WriterException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packInt(TRANSACTION_CONFIRMATION)
                .packString(confirmation.id)
                .packDouble(confirmation.amount)
                .packString(confirmation.sender)
                .close();
        String serialized = Base64.encodeToString(cipher(packer.toByteArray()), Base64.DEFAULT);
        Log.d("QRUtils", "Generate QR-Code: " + serialized);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(serialized, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
        int[] pixels = new int[QR_SIZE * QR_SIZE];
        for (int i = 0; i < QR_SIZE; i++) {
            for (int j = 0; j < QR_SIZE; j++) {
                pixels[i * QR_SIZE + j] = bitMatrix.get(j, i) ? 0xFF000000 : 0x00000000;
            }
        }

        Bitmap qrCode = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.ARGB_8888);
        qrCode.setPixels(pixels, 0, QR_SIZE, 0, 0, QR_SIZE, QR_SIZE);
        return qrCode;
    }

    public static String generateId(int length) {
        String symbols = "abcdefghijklmnopqrstuvwxyz0123456789";
        char[] buffer = new char[length];
        for (int i = 0; i < length; i++) {
            buffer[i] = symbols.charAt(random.nextInt(symbols.length()));
        }
        return new String(buffer);
    }
}
