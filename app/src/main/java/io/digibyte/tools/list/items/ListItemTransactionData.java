package io.digibyte.tools.list.items;

import io.digibyte.R;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.tools.list.ListItemData;

public class ListItemTransactionData extends ListItemData {
    public int transactionIndex;
    public int transactionsCount;
    public TxItem transactionItem;

    public ListItemTransactionData(int anIndex, int aTransactionsCount, TxItem aTransactionItem) {
        super(R.layout.list_item_transaction);

        this.transactionIndex = anIndex;
        this.transactionsCount = aTransactionsCount;
        this.transactionItem = aTransactionItem;
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

    public void update(ListItemTransactionData transactionItem) {
        this.transactionItem = transactionItem.getTransactionItem();
    }
}