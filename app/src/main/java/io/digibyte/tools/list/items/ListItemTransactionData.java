package io.digibyte.tools.list.items;

import io.digibyte.R;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.tools.list.ListItemData;

public class ListItemTransactionData extends ListItemData {
    public final int transactionIndex;
    public final int transactionsCount;
    public final TxItem transactionItem;

    public ListItemTransactionData(int anIndex, int aTransactionsCount, TxItem aTransactionItem,
            OnListItemClickListener aListener) {
        super(R.layout.list_item_transaction, ListItemTransactionViewHolder.class, aListener);

        this.transactionIndex = anIndex;
        this.transactionsCount = aTransactionsCount;
        this.transactionItem = aTransactionItem;
    }

    public TxItem getTransactionItem() {
        return transactionItem;
    }

    @Override
    public boolean equals(Object obj) {
        ListItemTransactionData listItemTransactionData = (ListItemTransactionData) obj;
        return listItemTransactionData.transactionItem.equals(transactionItem);
    }
}