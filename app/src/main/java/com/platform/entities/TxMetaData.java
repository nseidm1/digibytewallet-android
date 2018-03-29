package com.platform.entities;

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
public class TxMetaData {

    /**
     * Key: “txn-<txHash>”
     * <p>
     * {
     * “classVersion”: 5, //used for versioning the schema
     * “bh”: 47583, //blockheight
     * “er”: 2800.1, //exchange rate
     * “erc”: “USD”, //exchange currency
     * “fr”: 300, //fee rate
     * “s”: fd, //size
     * “c”: 123475859 //created
     * “dId”: ”<UUID>” //DeviceId - This is a UUID that gets generated and then persisted so it can get sent with every tx
     * “comment”: “Vodka for Mihail”
     * }
     */

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
}
