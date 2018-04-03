package io.digibyte.tools.adapter;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.digibyte.DigiByte;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.list.ListItemData;
import io.digibyte.tools.list.items.ListItemTransactionData;
import io.digibyte.tools.list.items.ListItemTransactionViewHolder;
import io.digibyte.tools.manager.BRSharedPrefs;


/**
 * BreadWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 7/27/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class TransactionListAdapter extends RecyclerView.Adapter<ListItemTransactionViewHolder> {
    public static final String TAG = TransactionListAdapter.class.getName();

    private ArrayList<ListItemTransactionData> listItemData = new ArrayList<>();
    private ArrayList<ListItemTransactionData> searchHolder = null;

    private RecyclerView recyclerView;

    public TransactionListAdapter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        setHasStableIds(true);
    }

    public void clearTransactions() {
        int countToRemove = listItemData.size();
        listItemData.clear();
        notifyItemRangeRemoved(0, countToRemove);
    }

    public void updateTransactions(ArrayList<ListItemTransactionData> transactions) {
        for (ListItemTransactionData listItemTransactionData : listItemData) {
            int indexOfPotentialChange = listItemData.indexOf(listItemTransactionData);
            if (indexOfPotentialChange < 0 || indexOfPotentialChange >= transactions.size()) {
                continue;
            }
            TxItem newTxItem = transactions.get(indexOfPotentialChange).getTransactionItem();
            int confirms = BRSharedPrefs.getLastBlockHeight(DigiByte.getContext())
                    - newTxItem.getBlockHeight() + 1;
            if (confirms <= 4) {
                listItemTransactionData.update(newTxItem);
                ListItemTransactionViewHolder listItemTransactionViewHolder =
                        (ListItemTransactionViewHolder) recyclerView
                                .findViewHolderForAdapterPosition(
                                indexOfPotentialChange);
                if (listItemTransactionViewHolder != null &&
                        isPositionOnscreen(indexOfPotentialChange)) {
                    listItemTransactionViewHolder.process(listItemTransactionData);
                }
            }
        }
    }

    private boolean isPositionOnscreen(int position) {
        LinearLayoutManager linearLayoutManager =
                (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
        return position >= firstVisiblePosition && position <= lastVisiblePosition;
    }

    public void addTransactions(ArrayList<ListItemTransactionData> transactions) {
        for (ListItemTransactionData transaction : transactions) {
            listItemData.add(0, transaction);
            notifyItemInserted(0);
        }
    }

    public void showSearchResults(ArrayList<ListItemTransactionData> searchTransactions) {
        if (searchTransactions == null) {
            return;
        }
        if (searchHolder == null) {
            searchHolder = listItemData;
        }
        listItemData = searchTransactions;
        notifyDataSetChanged();
    }

    public void clearSearchResults() {
        if (searchHolder != null) {
            listItemData = searchHolder;
            searchHolder = null;
        }
        notifyDataSetChanged();
    }

    public ArrayList<ListItemTransactionData> getTransactions() {
        return listItemData;
    }

    @Override
    public ListItemTransactionViewHolder onCreateViewHolder(ViewGroup aParent, int aResourceId) {
        LayoutInflater layoutInflater = LayoutInflater.from(aParent.getContext());
        View view = layoutInflater.inflate(aResourceId, aParent, false);
        return new ListItemTransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListItemTransactionViewHolder holder, int aPosition) {
        holder.process(this.getListItemDataForPosition(aPosition));
        holder.getView().setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ListItemTransactionViewHolder listItemTransactionViewHolder =
                    (ListItemTransactionViewHolder) recyclerView.findContainingViewHolder(view);
            int adapterPosition = listItemTransactionViewHolder.getAdapterPosition();
            BRAnimator.showTransactionPager((Activity) view.getContext(),
                    listItemData, adapterPosition);
        }
    };

    @Override
    public int getItemViewType(int aPosition) {
        return this.getListItemDataForPosition(aPosition).resourceId;
    }

    @Override
    public int getItemCount() {
        return listItemData.size();
    }

    private ListItemData getListItemDataForPosition(int aPosition) {
        return listItemData.get(aPosition);
    }

    @Override
    public long getItemId(int position) {
        return Integer.valueOf(listItemData.get(
                position).getTransactionItem().hashCode()).longValue();
    }
}