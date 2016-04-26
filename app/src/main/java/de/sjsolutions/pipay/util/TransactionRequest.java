package de.sjsolutions.pipay.util;

public class TransactionRequest {
    public final String id;
    public final double amount;
    public final String receiver;

    public TransactionRequest(String id, double amount, String receiver) {
        this.id = id;
        this.amount = amount;
        this.receiver = receiver;
    }

    public TransactionRequest(double amount, String receiver) {
        this.id = QRUtils.generateId();
        this.amount = amount;
        this.receiver = receiver;
    }
}
