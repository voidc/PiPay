package de.sjsolutions.pipay.util;

import android.os.Parcel;
import android.os.Parcelable;

public class TransactionRequest implements Parcelable {
    public final String id;
    public final double amount;
    public final String receiver;

    public TransactionRequest(String id, double amount, String receiver) {
        this.id = id;
        this.amount = amount;
        this.receiver = receiver;
    }

    public TransactionRequest(double amount, String receiver) {
        this.id = QRUtils.generateId(QRUtils.ID_LENGTH);
        this.amount = amount;
        this.receiver = receiver;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeDouble(amount);
        dest.writeString(receiver);
    }

    public static final Parcelable.Creator<TransactionRequest> CREATOR = new Parcelable.Creator<TransactionRequest>() {

        @Override
        public TransactionRequest createFromParcel(Parcel source) {
            return new TransactionRequest(source.readString(), source.readDouble(), source.readString());
        }

        @Override
        public TransactionRequest[] newArray(int size) {
            return new TransactionRequest[size];
        }
    };
}
