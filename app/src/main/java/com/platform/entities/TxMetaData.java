package com.platform.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 6/22/17.
 * Copyright (c) 2017 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class TxMetaData implements Parcelable {

    public String deviceId;
    public String comment;
    public String exchangeCurrency;
    public int classVersion;
    public int blockHeight;
    public double exchangeRate;
    public long fee;
    public int txSize;
    public int creationTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TxMetaData that = (TxMetaData) o;

        if (classVersion != that.classVersion) return false;
        if (blockHeight != that.blockHeight) return false;
        if (Double.compare(that.exchangeRate, exchangeRate) != 0) return false;
        if (fee != that.fee) return false;
        if (txSize != that.txSize) return false;
        if (creationTime != that.creationTime) return false;
        if (deviceId != null ? !deviceId.equals(that.deviceId) : that.deviceId != null)
            return false;
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        return exchangeCurrency != null ? exchangeCurrency.equals(that.exchangeCurrency)
                : that.exchangeCurrency == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = deviceId != null ? deviceId.hashCode() : 0;
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (exchangeCurrency != null ? exchangeCurrency.hashCode() : 0);
        result = 31 * result + classVersion;
        result = 31 * result + blockHeight;
        temp = Double.doubleToLongBits(exchangeRate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (fee ^ (fee >>> 32));
        result = 31 * result + txSize;
        result = 31 * result + creationTime;
        return result;
    }

    public TxMetaData() {}

    protected TxMetaData(Parcel in) {
        deviceId = in.readString();
        comment = in.readString();
        exchangeCurrency = in.readString();
        classVersion = in.readInt();
        blockHeight = in.readInt();
        exchangeRate = in.readDouble();
        fee = in.readLong();
        txSize = in.readInt();
        creationTime = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceId);
        dest.writeString(comment);
        dest.writeString(exchangeCurrency);
        dest.writeInt(classVersion);
        dest.writeInt(blockHeight);
        dest.writeDouble(exchangeRate);
        dest.writeLong(fee);
        dest.writeInt(txSize);
        dest.writeInt(creationTime);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TxMetaData> CREATOR = new Parcelable.Creator<TxMetaData>() {
        @Override
        public TxMetaData createFromParcel(Parcel in) {
            return new TxMetaData(in);
        }

        @Override
        public TxMetaData[] newArray(int size) {
            return new TxMetaData[size];
        }
    };
}