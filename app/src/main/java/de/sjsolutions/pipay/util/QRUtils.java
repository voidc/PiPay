package de.sjsolutions.pipay.util;


import android.graphics.Bitmap;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeResult;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;

public class QRUtils {
    public static final int QR_SIZE = 256;
    public static final int ID_LENGTH = 16;
    private static Random random = new Random();

    /* Test Code:
    https://zxing.org/w/chart?cht=qr&chs=350x350&chld=L&choe=UTF-8&chl=%7B%22id%22%3A%220123456789abcdef%22%2C%22amount%22%3A%221.23%22%2C%22receiver%22%3A%22LuckyMe%22%7D
     */

    public static TransactionRequest decodeTransactionRequest(BarcodeResult qrCode) {
        Log.d("QRUtils", "Scanned QR-Code: " + qrCode.getText());
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
        } catch (IOException | IllegalStateException | NumberFormatException e) {
            return null;
        }

        if (id != null && !id.isEmpty() && amount > 0 && receiver != null && !receiver.isEmpty()) {
            return new TransactionRequest(id, amount, receiver);
        } else return null;
    }

    public static TransactionConfirmation decodeTransactionConfirmation(BarcodeResult qrCode) {
        Log.d("QRUtils", "Scanned QR-Code: " + qrCode.getText());
        String id = null;
        double amount = 0;
        String sender = null;

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
                    case "sender":
                        sender = parser.nextString();
                        break;
                }
            }
            parser.endObject();
            parser.close();
        } catch (IOException | IllegalStateException | NumberFormatException e) {
            return null;
        }

        if (id != null && !id.isEmpty() && amount > 0 && sender != null && !sender.isEmpty()) {
            return new TransactionConfirmation(id, amount, sender);
        } else return null;
    }

    public static Bitmap encodeTransactionRequest(TransactionRequest request) throws IOException, WriterException {
        StringWriter sw = new StringWriter(32);
        JsonWriter writer = new JsonWriter(sw);
        writer.beginObject()
                .name("id").value(request.id)
                .name("amount").value(request.amount)
                .name("receiver").value(request.receiver)
                .endObject().close();
        String json = sw.toString();
        Log.d("QRUtils", "Generate QR-Code: " + json);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(json, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
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

    public static String generateId() {
        String symbols = "abcdefghijklmnopqrstuvwxyz0123456789";
        char[] buffer = new char[ID_LENGTH];
        for (int i = 0; i < ID_LENGTH; i++) {
            buffer[i] = symbols.charAt(random.nextInt(symbols.length()));
        }
        return new String(buffer);
    }
}
