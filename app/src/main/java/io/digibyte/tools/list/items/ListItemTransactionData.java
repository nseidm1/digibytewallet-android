package io.digibyte.tools.list.items;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import io.digibyte.R;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.tools.list.ListItemData;
import io.digibyte.tools.util.BRDateUtil;

public class ListItemTransactionData extends ListItemData implements Parcelable {
    public int transactionIndex;
    public int transactionsCount;
    public TxItem transactionItem;
    private String transactionDisplayTimeHolder;

    public ListItemTransactionData(int anIndex, int aTransactionsCount, TxItem aTransactionItem) {
        super(R.layout.list_item_transaction);

        this.transactionIndex = anIndex;
        this.transactionsCount = aTransactionsCount;
        this.transactionItem = aTransactionItem;
        this.transactionDisplayTimeHolder = BRDateUtil.getCustomSpan(new Date(transactionItem.getTimeStamp() * 1000));
    }

    public TxItem getTransactionItem() {
        return transactionItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListItemTransactionData that = (ListItemTransactionData) o;

        return transactionItem != null ? transactionItem.equals(that.transactionItem)
                : that.transactionItem == null;
    }

    @Override
    public int hashCode() {
        return transactionItem != null ? transactionItem.hashCode() : 0;
    }

    public void update(ListItemTransactionData transactionItemData) {
        this.transactionItem = transactionItemData.getTransactionItem();
        this.transactionDisplayTimeHolder = BRDateUtil.getCustomSpan(new Date(this.transactionItem.getTimeStamp() * 1000));
    }

    public String getTransactionDisplayTimeHolder() {
        return transactionDisplayTimeHolder;
    }

    protected ListItemTransactionData(Parcel in) {
        super(in);
        transactionIndex = in.readInt();
        transactionsCount = in.readInt();
        transactionItem = in.readParcelable(TxItem.class.getClassLoader());
        transactionDisplayTimeHolder = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(transactionIndex);
        dest.writeInt(transactionsCount);
        dest.writeParcelable(transactionItem, flags);
        dest.writeString(transactionDisplayTimeHolder);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ListItemTransactionData> CREATOR = new Parcelable.Creator<ListItemTransactionData>() {
        @Override
        public ListItemTransactionData createFromParcel(Parcel in) {
            return new ListItemTransactionData(in);
        }

        @Override
        public ListItemTransactionData[] newArray(int size) {
            return new ListItemTransactionData[size];
        }
    };
}