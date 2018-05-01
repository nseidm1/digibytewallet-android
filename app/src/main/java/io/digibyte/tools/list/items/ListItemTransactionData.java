package io.digibyte.tools.list.items;

import java.util.Date;

import io.digibyte.R;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.tools.list.ListItemData;
import io.digibyte.tools.util.BRDateUtil;

public class ListItemTransactionData extends ListItemData {
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
}