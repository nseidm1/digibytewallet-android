package io.digibyte.presenter.activities;

import static io.digibyte.tools.animation.BRAnimator.t1Size;
import static io.digibyte.tools.animation.BRAnimator.t2Size;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.support.transition.TransitionManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

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
import io.digibyte.presenter.activities.intro.WriteDownActivity;
import io.digibyte.presenter.activities.settings.FingerprintActivity;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.presenter.customviews.BRSearchBar;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.presenter.entities.VerticalSpaceItemDecoration;
import io.digibyte.presenter.fragments.FragmentManage;
import io.digibyte.tools.adapter.TransactionListAdapter;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.list.ListItemViewHolder;
import io.digibyte.tools.list.items.ListItemPromptData;
import io.digibyte.tools.list.items.ListItemPromptViewHolder;
import io.digibyte.tools.list.items.ListItemSyncingData;
import io.digibyte.tools.list.items.ListItemSyncingViewHolder;
import io.digibyte.tools.list.items.ListItemTransactionData;
import io.digibyte.tools.manager.BRApiManager;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.manager.PromptManager;
import io.digibyte.tools.manager.SyncManager;
import io.digibyte.tools.manager.SyncService;
import io.digibyte.tools.manager.TxManager;
import io.digibyte.tools.manager.TxManager.onStatusListener;
import io.digibyte.tools.security.BitcoinUrlHandler;
import io.digibyte.tools.sqlite.TransactionDataSource;
import io.digibyte.tools.threads.BRExecutor;
import io.digibyte.tools.util.BRConstants;
import io.digibyte.tools.util.BRCurrency;
import io.digibyte.tools.util.BRExchange;
import io.digibyte.tools.util.Utils;
import io.digibyte.wallet.BRPeerManager;
import io.digibyte.wallet.BRWalletManager;
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 8/4/15.
 * Copyright (c) 2016 breadwallet LLC
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
        TransactionDataSource.OnTxAddedListener, SyncManager.onStatusListener, onStatusListener,
        BRSearchBar.OnSearchUpdateListener {
    ActivityBreadBinding bindings;
    private Unbinder unbinder;
    private Handler handler = new Handler(Looper.getMainLooper());
    private TransactionListAdapter listViewAdapter;
    private ListItemSyncingData listItemSyncingData = new ListItemSyncingData();
    private ListItemViewHolder syncViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindings = DataBindingUtil.setContentView(this, R.layout.activity_bread);
        listViewAdapter = new TransactionListAdapter(bindings.txList);
        bindings.syncContainer.addView(getSyncView());
        bindings.mainContainer.getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);
        bindings.promptContainer.getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);
        bindings.promptContainer.getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);
        bindings.txList.addItemDecoration(new VerticalSpaceItemDecoration(4));
        SlideInDownAnimator slideInDownAnimator = new SlideInDownAnimator();
        slideInDownAnimator.setAddDuration(500);
        slideInDownAnimator.setChangeDuration(0);
        bindings.txList.setItemAnimator(slideInDownAnimator);
        unbinder = ButterKnife.bind(this);
        initializeViews();
        loadNextPromptItem();
    }

    private void initializeViews() {
        bindings.txList.setLayoutManager(new LinearLayoutManager(this));
        bindings.txList.setAdapter(listViewAdapter);
        bindings.primaryPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                t1Size);//make it the size it should be after animation to get the X
        bindings.secondaryPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                t2Size);//make it the size it should be after animation to get the X
        handler.post(() -> setPriceTags(BRSharedPrefs.getPreferredBTC(BreadActivity.this), false));
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bindings.primaryPrice.setText(formattedBTCAmount);
                    bindings.secondaryPrice.setText(String.format("%s", formattedCurAmount));
                }
            });
        });
    }

    private void showSearchTransactions() {
        ArrayList<ListItemTransactionData> transactionList = new ArrayList<>();
        if (bindings.searchBar != null && isSearching()) {
            boolean[] switches = bindings.searchBar.getFilterSwitches();
            String searchQuery = bindings.searchBar.getSearchQuery().toLowerCase().trim();

            int switchesON = 0;
            for (boolean i : switches) if (i) switchesON++;
            int transactionsCount = listViewAdapter.getTransactions().size();
            boolean matchesHash;
            boolean matchesAddress;
            boolean matchesMemo;
            boolean willAdd;
            for (int index = 0; index < transactionsCount; index++) {
                ListItemTransactionData transactionData = listViewAdapter.getTransactions().get(
                        index);
                TxItem item = transactionData.transactionItem;
                matchesHash =
                        item.getTxHashHexReversed() != null &&
                                item.getTxHashHexReversed().toLowerCase().contains(
                                        searchQuery.toLowerCase());
                matchesAddress = item.getFrom()[0].toLowerCase().contains(searchQuery.toLowerCase())
                        || item.getTo()[0].toLowerCase().contains(searchQuery.toLowerCase());
                matchesMemo = item.metaData != null && item.metaData.comment != null
                        && item.metaData.comment.toLowerCase().contains(searchQuery.toLowerCase());

                // Can we optimize this?
                if (matchesHash || matchesAddress || matchesMemo) {
                    willAdd = true;

                    if (switchesON > 0) {
                        // filter by sent and this is received
                        if (switches[0] && (item.getSent() - item.getReceived() <= 0)) {
                            willAdd = false;
                        }

                        // filter by received and this is sent
                        if (switches[1] && (item.getSent() - item.getReceived() > 0)) {
                            willAdd = false;
                        }

                        // complete
                        int confirms = item.getBlockHeight() == Integer.MAX_VALUE ? 0
                                : BRSharedPrefs.getLastBlockHeight(this) - item.getBlockHeight()
                                        + 1;
                        if (switches[2] && confirms >= 6) {
                            willAdd = false;
                        }

                        // pending
                        if (switches[3] && confirms < 6) {
                            willAdd = false;
                        }
                    }

                    if (willAdd) {
                        transactionList.add(transactionData);
                    }
                }
            }
        }
        listViewAdapter.showSearchResults(transactionList);
    }

    private void loadNextPromptItem() {
        PromptManager.PromptItem promptItem = PromptManager.getInstance().nextPrompt();
        if (null != promptItem) {
            showPrompt(new ListItemPromptData(promptItem));
        }
    }

    private View getSyncView() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(listItemSyncingData.resourceId, bindings.syncContainer,
                false);
        view.setVisibility(View.GONE);
        syncViewHolder = new ListItemSyncingViewHolder(view);
        return view;
    }

    private void showPrompt(ListItemPromptData listItemPromptData) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(listItemPromptData.resourceId, bindings.syncContainer,
                false);
        view.setOnClickListener(new PromptClickListener(listItemPromptData.promptItem));
        ListItemPromptViewHolder listItemPromptViewHolder = new ListItemPromptViewHolder(view,
                new PromptCloseClickListener(listItemPromptData.promptItem));
        bindings.promptContainer.addView(view);
        listItemPromptViewHolder.process(listItemPromptData);
    }

    private void removePrompt() {
        bindings.promptContainer.removeAllViews();
    }

    public void closeSearchBar() {
        bindings.toolBarFlipper.setDisplayedChild(0);
        handler.postDelayed(() -> {
            bindings.searchBar.toggleKeyboard(false);
            listViewAdapter.clearSearchResults();
        }, 250);
    }

    public void openSearchBar() {
        bindings.searchBar.toggleKeyboard(true);
        bindings.toolBarFlipper.setDisplayedChild(1);
    }

    private boolean isSearching() {
        return bindings.toolBarFlipper.getDisplayedChild() == 1;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////// List item click listeners
    /// ////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class PromptClickListener implements View.OnClickListener {

        private PromptManager.PromptItem promptItem;

        public PromptClickListener(PromptManager.PromptItem promptItem) {
            this.promptItem = promptItem;
        }

        @Override
        public void onClick(View view) {
            Intent intent;
            final Activity activity = BreadActivity.this;
            switch (promptItem) {
                case SYNCING:
                    break;
                case FINGER_PRINT:
                    intent = new Intent(activity, FingerprintActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.enter_from_right,
                            R.anim.exit_to_left);
                    break;
                case PAPER_KEY:
                    intent = new Intent(activity, WriteDownActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.enter_from_bottom,
                            R.anim.fade_down);
                    break;
                case UPGRADE_PIN:
                    intent = new Intent(activity, UpdatePinActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.enter_from_right,
                            R.anim.exit_to_left);
                    break;
                case RECOMMEND_RESCAN:
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(
                            () -> {
                                BRSharedPrefs.putScanRecommended(BreadActivity.this, false);
                                BRWalletManager.getInstance().walletFreeEverything();
                                BRPeerManager.getInstance().peerManagerFreeEverything();
                                recreate();
                            });
                    break;
                case NO_PASS_CODE:
                    break;
            }
        }
    }

    private class PromptCloseClickListener implements View.OnClickListener {

        private PromptManager.PromptItem promptItem;

        public PromptCloseClickListener(PromptManager.PromptItem promptItem) {
            this.promptItem = promptItem;
        }

        @Override
        public void onClick(View view) {
            removePrompt();
            BRSharedPrefs.putPromptDismissed(BreadActivity.this,
                    PromptManager.getInstance().getPromptName(promptItem));
            loadNextPromptItem();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////// Manager Listeners
    /// ////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onSyncManagerStarted() {
        bindings.syncContainer.getChildAt(0).setVisibility(View.VISIBLE);
        syncViewHolder.process(listItemSyncingData);
    }

    @Override
    public void onSyncManagerUpdate() {
        syncViewHolder.process(listItemSyncingData);
    }

    @Override
    public void onSyncManagerFinished() {
        bindings.syncContainer.getChildAt(0).setVisibility(View.GONE);
    }

    @Override
    public void onSyncFailed() {
        bindings.syncContainer.getChildAt(0).setVisibility(View.GONE);
    }

    @Override
    public void onTxManagerUpdate(TxItem[] newTxItems) {
        BRWalletManager.getInstance().refreshBalance(DigiByte.getContext());
        if (newTxItems == null || newTxItems.length == 0 || isSearching()) {
            return;
        }
        ArrayList<ListItemTransactionData> newTransactions = getNewTransactionsData(newTxItems);
        ArrayList<ListItemTransactionData> transactionsToAdd = removeAllExistingEntries(
                newTransactions);
        if (transactionsToAdd.size() > 0) {
            listViewAdapter.addTransactions(transactionsToAdd);
            bindings.txList.smoothScrollToPosition(0);
        } else {
            listViewAdapter.updateTransactions(newTransactions);
        }
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
            removeAll(listViewAdapter.getTransactions());
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////// UI OnClick Listeners
    /// /////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @OnClick(R.id.send_layout)
    void onButtonSend(View view) {
        if (BRAnimator.isClickAllowed()) {
            BRAnimator.showSendFragment(BreadActivity.this, null);
        }
    }

    @OnClick(R.id.receive_layout)
    void onButtonReceive(View view) {
        if (BRAnimator.isClickAllowed()) {
            BRAnimator.showReceiveFragment(BreadActivity.this, true);
        }
    }

    @OnClick(R.id.menu_layout)
    void onMenuButtonClick(View view) {
        if (BRAnimator.isClickAllowed()) {
            BRAnimator.showMenuFragment(BreadActivity.this);
        }
    }

    @OnClick(R.id.manage_text)
    void onManageTextClick(View view) {
        if (BRAnimator.isClickAllowed()) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(0, 0, 0, R.animator.plain_300);
            FragmentManage fragmentManage = new FragmentManage();
            transaction.add(android.R.id.content, fragmentManage, FragmentManage.class.getName());
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @OnClick(R.id.primary_price)
    void onPrimaryPriceClick(View view) {
        if (BRAnimator.isClickAllowed()) {
            boolean b = !BRSharedPrefs.getPreferredBTC(BreadActivity.this);
            setPriceTags(b, true);
            BRSharedPrefs.putPreferredBTC(BreadActivity.this, b);
        }
    }

    @OnClick(R.id.search_icon)
    void onSearchClick(View view) {
        if (BRAnimator.isClickAllowed()) {
            openSearchBar();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////// LEFT OVERS CLEANUP?
    /// //////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void setPriceTags(boolean btcPreferred, boolean animate) {
        bindings.secondaryPrice.setTextSize(!btcPreferred ? t1Size : t2Size);
        bindings.primaryPrice.setTextSize(!btcPreferred ? t2Size : t1Size);
        ConstraintSet set = new ConstraintSet();
        set.clone(bindings.breadToolbar);
        if (animate) {
            TransitionManager.beginDelayedTransition(bindings.breadToolbar);
        }
        int px4 = Utils.getPixelsFromDps(this, 4);
        int px16 = Utils.getPixelsFromDps(this, 16);
        set.connect(!btcPreferred ? R.id.secondary_price : R.id.primary_price, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.END, px16);
        set.connect(R.id.equals, ConstraintSet.START,
                !btcPreferred ? bindings.secondaryPrice.getId() : bindings.primaryPrice.getId(),
                ConstraintSet.END,
                px4);
        set.connect(!btcPreferred ? R.id.primary_price : R.id.secondary_price, ConstraintSet.START,
                bindings.equals.getId(), ConstraintSet.END, px4);
        set.applyTo(bindings.breadToolbar);

        new Handler().postDelayed(() ->
                        updateDigibyteDollarValues(),
                bindings.breadToolbar.getLayoutTransition().getDuration(LayoutTransition.CHANGING));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////// Activity overrides
    /// ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //leave it empty because the FragmentMenu is improperly designed
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri data = intent.getData();
        if (data != null) {
            String scheme = data.getScheme();
            if (scheme != null && (scheme.startsWith("digibyte") || scheme.startsWith("digiid"))) {
                BitcoinUrlHandler.processRequest(this, intent.getDataString());
            }
        }
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
        SyncService.scheduleBackgroundSync(this);
        TxManager.getInstance().updateTxList();
        BRApiManager.getInstance().asyncUpdateCurrencyData(this);
        BRApiManager.getInstance().asyncUpdateFeeData(this);
        bindings.searchBar.setOnUpdateListener(this);
        SyncManager.getInstance().startSyncingProgressThread(null);
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
        bindings.searchBar.setOnUpdateListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case BRConstants.CAMERA_REQUEST_ID: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    BRAnimator.openScanner(this, BRConstants.SCANNER_REQUEST);
                }
                break;
            }
        }
    }

    @Override
    public void onSearchBarFilterUpdate() {
        showSearchTransactions();
    }
}