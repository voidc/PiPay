package de.sjsolutions.pipay.util;

import android.os.Parcel;
import android.os.Parcelable;

public class TransactionConfirmation implements Parcelable {
    public final String id;
    public final double amount;
    public final String sender;

    public TransactionConfirmation(String id, double amount, String sender) {
        this.id = id;
        this.amount = amount;
        this.sender = sender;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeDouble(amount);
        dest.writeString(sender);
    }

    public static final Parcelable.Creator<TransactionConfirmation> CREATOR = new Parcelable.Creator<TransactionConfirmation>() {

        @Override
        public TransactionConfirmation createFromParcel(Parcel source) {
            return new TransactionConfirmation(source.readString(), source.readDouble(), source.readString());
        }

        @Override
        public TransactionConfirmation[] newArray(int size) {
            return new TransactionConfirmation[size];
        }
    };

}
