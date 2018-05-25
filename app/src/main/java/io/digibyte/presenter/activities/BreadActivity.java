package io.digibyte.presenter.activities;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.databinding.ActivityBreadBinding;
import io.digibyte.presenter.activities.settings.SecurityCenterActivity;
import io.digibyte.presenter.activities.settings.SettingsActivity;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.presenter.entities.VerticalSpaceItemDecoration;
import io.digibyte.tools.adapter.TransactionListAdapter;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.list.items.ListItemTransactionData;
import io.digibyte.tools.manager.BRApiManager;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.manager.SyncManager;
import io.digibyte.tools.manager.TxManager;
import io.digibyte.tools.manager.TxManager.onStatusListener;
import io.digibyte.tools.sqlite.TransactionDataSource;
import io.digibyte.tools.threads.BRExecutor;
import io.digibyte.tools.util.BRCurrency;
import io.digibyte.tools.util.BRExchange;
import io.digibyte.tools.util.Utils;
import io.digibyte.tools.util.ViewUtils;
import io.digibyte.wallet.BRPeerManager;
import io.digibyte.wallet.BRWalletManager;
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;

/**
 * BreadWallet
 * <p/>
 * Created by Noah Seidman <noah@noahseidman.com> on 4/14/18.
 * Copyright (c) 2018 DigiByte Holdings
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

public class BreadActivity extends BRActivity implements BRWalletManager.OnBalanceChanged,
        BRPeerManager.OnTxStatusUpdate, BRSharedPrefs.OnIsoChangedListener,
        TransactionDataSource.OnTxAddedListener, SyncManager.onStatusListener, onStatusListener {
    ActivityBreadBinding bindings;
    private Unbinder unbinder;
    private Handler handler = new Handler(Looper.getMainLooper());

    private RecyclerView allRecycler;
    private TransactionListAdapter allAdapter;
    private RecyclerView sentRecycler;
    private TransactionListAdapter sentAdapter;
    private RecyclerView receivedRecycler;
    private TransactionListAdapter receivedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindings = DataBindingUtil.setContentView(this, R.layout.activity_bread);
        bindings.setPagerAdapter(new TxAdapter());
        bindings.txPager.setOffscreenPageLimit(2);
        bindings.tabLayout.setupWithViewPager(bindings.txPager);
        bindings.contentContainer.getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);
        ViewUtils.increaceClickableArea(bindings.qrButton);
        ViewUtils.increaceClickableArea(bindings.navDrawer);
        ViewUtils.increaceClickableArea(bindings.digiidButton);
        unbinder = ButterKnife.bind(this);
    }

    private void updateDigibyteDollarValues() {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(() -> {
            final String iso = BRSharedPrefs.getIso(BreadActivity.this);

            //current amount in satoshis
            final BigDecimal amount = new BigDecimal(
                    BRSharedPrefs.getCatchedBalance(BreadActivity.this));

            //amount in BTC units
            final BigDecimal btcAmount = BRExchange.getBitcoinForSatoshis(BreadActivity.this,
                    amount);
            final String formattedBTCAmount = BRCurrency.getFormattedCurrencyString(
                    BreadActivity.this, "DGB", btcAmount);

            //amount in currency units
            final BigDecimal curAmount = BRExchange.getAmountFromSatoshis(BreadActivity.this,
                    iso, amount);
            final String formattedCurAmount = BRCurrency.getFormattedCurrencyString(
                    BreadActivity.this, iso, curAmount);
            runOnUiThread(() -> {
                bindings.primaryPrice.setText(formattedBTCAmount);
                bindings.secondaryPrice.setText(String.format("%s", formattedCurAmount));
            });
        });
    }

    @Override
    public void onSyncManagerStarted() {
        bindings.syncContainer.setVisibility(View.VISIBLE);
        bindings.toolbarLayout.setVisibility(View.GONE);
        bindings.animationView.playAnimation();
        updateSyncText();
    }

    @Override
    public void onSyncManagerUpdate() {
        updateSyncText();
    }

    @Override
    public void onSyncManagerFinished() {
        bindings.syncContainer.setVisibility(View.GONE);
        bindings.toolbarLayout.setVisibility(View.VISIBLE);
        bindings.animationView.playAnimation();
        bindings.animationView.cancelAnimation();
    }

    @Override
    public void onSyncFailed() {
        bindings.syncContainer.setVisibility(View.GONE);
        bindings.toolbarLayout.setVisibility(View.VISIBLE);
        bindings.animationView.playAnimation();
        bindings.animationView.cancelAnimation();
    }

    private void updateSyncText() {
        bindings.syncText.setText(SyncManager.getInstance().getLastBlockTimestamp() == 0
                ? DigiByte.getContext().getString(R.string.NodeSelector_statusLabel) + ": "
                + DigiByte.getContext().getString(R.string.SyncingView_connecting)
                : Integer.toString((int) (SyncManager.getInstance().getProgress() * 100)) + "%"
                        + " - " + Utils.formatTimeStamp(
                        SyncManager.getInstance().getLastBlockTimestamp() * 1000,
                        "MMM. dd, yyyy 'at' hh:mm a"));
    }

    @Override
    public void onTxManagerUpdate(TxItem[] newTxItems) {
        BRWalletManager.getInstance().refreshBalance(DigiByte.getContext());
        if (newTxItems == null || newTxItems.length == 0) {
            return;
        }
        ArrayList<ListItemTransactionData> newTransactions = getNewTransactionsData(newTxItems);
        ArrayList<ListItemTransactionData> transactionsToAdd = removeAllExistingEntries(
                newTransactions);
        if (transactionsToAdd.size() > 0) {
            allAdapter.addTransactions(transactionsToAdd);
            sentAdapter.addTransactions(
                    convertNewTransactionsForAdapter(Adapter.SENT, transactionsToAdd));
            receivedAdapter.addTransactions(
                    convertNewTransactionsForAdapter(Adapter.RECEIVED, transactionsToAdd));
            allRecycler.smoothScrollToPosition(0);
            sentRecycler.smoothScrollToPosition(0);
            receivedRecycler.smoothScrollToPosition(0);
            notifyDataSetChangeForAll();
        } else {
            allAdapter.updateTransactions(newTransactions);
            sentAdapter.updateTransactions(newTransactions);
            receivedAdapter.updateTransactions(newTransactions);
            notifyDataSetChangeForAll();
        }
    }

    private enum Adapter {
        SENT, RECEIVED
    }

    private ArrayList<ListItemTransactionData> convertNewTransactionsForAdapter(Adapter adapter,
            ArrayList<ListItemTransactionData> transactions) {
        ArrayList<ListItemTransactionData> transactionList = new ArrayList<>();
        for (int index = 0; index < transactions.size(); index++) {
            ListItemTransactionData transactionData = transactions.get(index);
            TxItem item = transactions.get(index).transactionItem;
            if (adapter == Adapter.RECEIVED && item.getSent() == 0) {
                transactionList.add(transactionData);
            } else if (adapter == Adapter.SENT && item.getSent() > 0) {
                transactionList.add(transactionData);
            }
        }
        return transactionList;
    }

    private ArrayList<ListItemTransactionData> getNewTransactionsData(TxItem[] newTxItems) {
        ArrayList<ListItemTransactionData> newTransactionsData = new ArrayList();
        ArrayList<TxItem> newTransactions = new ArrayList(Arrays.asList(newTxItems));
        Collections.sort(newTransactions,
                (t1, t2) -> Long.valueOf(t1.getTimeStamp()).compareTo(t2.getTimeStamp()));
        for (TxItem tx : newTransactions) {
            newTransactionsData.add(new ListItemTransactionData(newTransactions.indexOf(tx),
                    newTransactions.size(), tx));
        }
        return newTransactionsData;
    }

    private ArrayList<ListItemTransactionData> removeAllExistingEntries(
            ArrayList<ListItemTransactionData> newTransactions) {
        return new ArrayList<ListItemTransactionData>(newTransactions) {{
            removeAll(allAdapter.getTransactions());
        }};
    }

    @Override
    public void onStatusUpdate() {
        TxManager.getInstance().updateTxList();
    }

    @Override
    public void onIsoChanged(String iso) {
        updateDigibyteDollarValues();
    }

    @Override
    public void onTxAdded() {
        TxManager.getInstance().updateTxList();
    }

    @Override
    public void onBalanceChanged(final long balance) {
        updateDigibyteDollarValues();
    }

    @Override
    public void showSendConfirmDialog(final String message, final int error, byte[] txHash) {
        BRExecutor.getInstance().forMainThreadTasks().execute(() -> {
            BRAnimator.showBreadSignal(BreadActivity.this,
                    error == 0 ? getString(R.string.Alerts_sendSuccess)
                            : getString(R.string.Alert_error),
                    error == 0 ? getString(R.string.Alerts_sendSuccessSubheader)
                            : message, error == 0 ? R.drawable.signal_icon_graphic
                            : R.drawable.ic_error_outline_black_24dp, () -> {
                        try {
                            getSupportFragmentManager().popBackStack();
                        } catch (IllegalStateException e) {
                        }
                    });
        });
    }

    @OnClick(R.id.nav_drawer)
    void onNavButtonClick(View view) {
        try {
            bindings.drawerLayout.openDrawer(Gravity.LEFT);
        } catch (IllegalArgumentException e) {
            //Race condition inflating the hierarchy?
        }
    }

    @OnClick(R.id.main_action)
    void onMenuButtonClick(View view) {
        BRAnimator.showMenuFragment(BreadActivity.this);
    }

    @OnClick(R.id.digiid_button)
    void onDigiIDButtonClick(View view) {
        BRAnimator.openScanner(this);
    }

    @OnClick(R.id.primary_price)
    void onPrimaryPriceClick(View view) {
        BRSharedPrefs.putPreferredBTC(BreadActivity.this, true);
        notifyDataSetChangeForAll();
    }

    @OnClick(R.id.secondary_price)
    void onSecondaryPriceClick(View view) {
        BRSharedPrefs.putPreferredBTC(BreadActivity.this, false);
        notifyDataSetChangeForAll();
    }

    private void notifyDataSetChangeForAll() {
        allAdapter.notifyDataChanged();
        sentAdapter.notifyDataChanged();
        receivedAdapter.notifyDataChanged();
    }

    @OnClick(R.id.security_center)
    void onSecurityCenterClick(View view) {
        startActivity(new Intent(this, SecurityCenterActivity.class));
    }

    @OnClick(R.id.settings)
    void onSettingsClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }


    @OnClick(R.id.lock)
    void onLockClick(View view) {
        BRAnimator.startBreadActivity(this, true);
    }

    @OnClick(R.id.qr_button)
    void onQRClick(View view) {
        BRAnimator.openScanner(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDigibyteDollarValues();
        BRWalletManager.getInstance().addBalanceChangedListener(this);
        BRPeerManager.getInstance().addStatusUpdateListener(this);
        BRSharedPrefs.addIsoChangedListener(this);
        TxManager.getInstance().addListener(this);
        SyncManager.getInstance().addListener(this);
        BRWalletManager.getInstance().refreshBalance(this);
        DigiByte.SyncBlockchainJob.scheduleJob();
        TxManager.getInstance().updateTxList();
        BRApiManager.getInstance().asyncUpdateCurrencyData(this);
        BRApiManager.getInstance().asyncUpdateFeeData(this);
        SyncManager.getInstance().startSyncingProgressThread(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BRWalletManager.getInstance().removeListener(this);
        BRPeerManager.getInstance().removeListener(this);
        BRSharedPrefs.removeListener(this);
        TxManager.getInstance().removeListener(this);
        SyncManager.getInstance().removeListener(this);
        SyncManager.getInstance().stopSyncingProgressThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        if (bindings.drawerLayout.isDrawerOpen(Gravity.START)) {
            handler.post(() -> bindings.drawerLayout.closeDrawer(Gravity.LEFT));
        } else {
            super.onBackPressed();
        }
    }

    private class TxAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            RecyclerView layout = (RecyclerView) LayoutInflater.from(BreadActivity.this).inflate(
                    R.layout.activity_bread_recycler, collection, false);
            SlideInDownAnimator slideInDownAnimator = new SlideInDownAnimator();
            slideInDownAnimator.setAddDuration(500);
            slideInDownAnimator.setChangeDuration(0);
            layout.addItemDecoration(new VerticalSpaceItemDecoration(4));
            layout.setItemAnimator(slideInDownAnimator);
            layout.setLayoutManager(new LinearLayoutManager(BreadActivity.this));
            switch (position) {
                case 0:
                    allAdapter = new TransactionListAdapter(layout);
                    layout.setAdapter(allAdapter);
                    allRecycler = layout;
                    break;
                case 1:
                    sentAdapter = new TransactionListAdapter(layout);
                    layout.setAdapter(sentAdapter);
                    sentRecycler = layout;
                    break;
                case 2:
                    receivedAdapter = new TransactionListAdapter(layout);
                    layout.setAdapter(receivedAdapter);
                    receivedRecycler = layout;
                    break;
            }
            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return object == view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                default:
                case 0:
                    return getString(R.string.all);
                case 1:
                    return getString(R.string.sent);
                case 2:
                    return getString(R.string.received);
            }
        }
    }
}