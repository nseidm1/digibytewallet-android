package io.digibyte.presenter.fragments.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.platform.tools.KVStoreManager;

import java.math.BigDecimal;

import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.manager.TxManager;
import io.digibyte.tools.threads.BRExecutor;
import io.digibyte.tools.util.BRCurrency;
import io.digibyte.tools.util.BRExchange;

public class TransactionDetailsViewModel extends BaseObservable {

    private TxItem item;

    public TransactionDetailsViewModel(TxItem item) {
        this.item = item;
    }

    @Bindable
    public String getAmount() {
        boolean isBTCPreferred = BRSharedPrefs.getPreferredBTC(DigiByte.getContext());
        boolean received = item.getSent() == 0;
        String iso = isBTCPreferred ? "DGB" : BRSharedPrefs.getIso(DigiByte.getContext());
        long satoshisAmount = received ? item.getReceived() : (item.getSent() - item.getReceived());
        return BRCurrency.getFormattedCurrencyString(DigiByte.getContext(), iso,
                BRExchange.getAmountFromSatoshis(DigiByte.getContext(), iso,
                        new BigDecimal(satoshisAmount)));
    }

    @Bindable
    public String getToFrom() {
        return item.getReceived() - item.getSent() < 0 ? DigiByte.getContext().getString(R.string.sent) : DigiByte.getContext().getString(R.string.received);
    }

    @Bindable
    public boolean getSent() {
        return item.getReceived() - item.getSent() < 0;
    }

    @Bindable
    public String getAddress() {
        return item.getTo()[0];
    }

    @Bindable
    public int getSentReceivedIcon() {
        if (getSent()) {
            return R.drawable.transaction_details_sent;
        } else {
            return R.drawable.transaction_details_received;
        }
    }

    @Bindable
    public String getComment() {
        return item.metaData.comment;
    }

    public void setComment(String comment) {
        item.metaData.comment = comment;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                KVStoreManager.getInstance().putTxMetaData(DigiByte.getContext(), item.metaData, item.getTxHash());
                TxManager.getInstance().updateTxList();
            }
        });
    }

//    private void fillTexts() {
//        String iso = BRSharedPrefs.getPreferredBTC(getActivity()) ? "DGB" : BRSharedPrefs.getIso(getContext());
//
//        //get the tx amount
//        BigDecimal txAmount = new BigDecimal(item.getReceived() - item.getSent()).abs();
//        //see if it was sent
//        boolean sent = item.getReceived() - item.getSent() < 0;
//
//        //calculated and formatted amount for iso
//        String amountWithFee = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromSatoshis(getActivity(), iso, txAmount));
//        String amount = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromSatoshis(getActivity(), iso, item.getFee() == -1 ? txAmount : txAmount.subtract(new BigDecimal(item.getFee()))));
//        //calculated and formatted fee for iso
//        String fee = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromSatoshis(getActivity(), iso, new BigDecimal(item.getFee())));
//        //description (Sent $24.32 ....)
//        Spannable descriptionString = sent ? new SpannableString(String.format(getString(R.string.TransactionDetails_sent), amountWithFee)) : new SpannableString(String.format(getString(R.string.TransactionDetails_received), amount));
//
//        String startingBalance = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromSatoshis(getActivity(), iso, new BigDecimal(sent ? item.getBalanceAfterTx() + txAmount.longValue() : item.getBalanceAfterTx() - txAmount.longValue())));
//        String endingBalance = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromSatoshis(getActivity(), iso, new BigDecimal(item.getBalanceAfterTx())));
//        String commentString = item.metaData == null || item.metaData.comment == null ? "" : item.metaData.comment;
//        String sb = String.format(getString(R.string.Transaction_starting), startingBalance);
//        String eb = String.format(getString(R.string.Transaction_ending), endingBalance);
//        String amountString = String.format("%s %s\n\n%s\n%s", amount, item.getFee() == -1 ? "" : String.format(getString(R.string.Transaction_fee), fee), sb, eb);
//        if (sent) amountString = "-" + amountString;
//        String addr = item.getTo()[0];
//        String toFrom = sent ? String.format(getString(R.string.TransactionDetails_to), addr) : String.format(getString(R.string.TransactionDetails_from), addr);
//
//        mTxHash.setText(item.getTxHashHexReversed());
//
//
//        int level = getLevel(item);
//
//
//        boolean availableForSpend = false;
////        String sentReceived = !sent ? "Receiving" : "Sending";
////        sentReceived = ""; //make this empy for now
//        String percentage = "";
//        switch (level) {
//            case 0:
//                percentage = "0%";
//                break;
//            case 1:
//                percentage = "20%";
//                break;
//            case 2:
//                percentage = "40%";
//                availableForSpend = true;
//                break;
//            case 3:
//                percentage = "60%";
//                availableForSpend = true;
//                break;
//            case 4:
//                percentage = "80%";
//                availableForSpend = true;
//                break;
//            case 5:
//                percentage = "100%";
//                availableForSpend = true;
//                break;
//        }
//
//
//        mToFromBottom.setText(sent ? getString(R.string.TransactionDirection_to) : getString(R.string.TransactionDirection_address));
//        mDateText.setText(getFormattedDate(item.getTimeStamp()));
//        mDescriptionText.setText(TextUtils.concat(descriptionString));
//        mSubHeader.setText(toFrom);
//        mCommentText.setText(commentString);
//
//        mAmountText.setText(amountString);
//        mAddressText.setText(addr);
//    }
//
//    private int getLevel(TxItem item) {
//        int blockHeight = item.getBlockHeight();
//        int confirms = blockHeight == Integer.MAX_VALUE ? 0 : BRSharedPrefs.getLastBlockHeight(getContext()) - blockHeight + 1;
//        int level;
//        if (confirms <= 0) {
//            int relayCount = BRPeerManager.getRelayCount(item.getTxHash());
//            if (relayCount <= 0)
//                level = 0;
//            else if (relayCount == 1)
//                level = 1;
//            else
//                level = 2;
//        } else {
//            if (confirms == 1)
//                level = 3;
//            else if (confirms == 2)
//                level = 4;
//            else if (confirms == 3)
//                level = 5;
//            else
//                level = 6;
//        }
//        return level;
//    }

//    private String getFormattedDate(long timeStamp) {
//
//        Date currentLocalTime = new Date(timeStamp == 0 ? System.currentTimeMillis() : timeStamp * 1000);
//
//        SimpleDateFormat date1 = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
//        SimpleDateFormat date2 = new SimpleDateFormat("HH:mm a", Locale.getDefault());
//
//        String str1 = date1.format(currentLocalTime);
//        String str2 = date2.format(currentLocalTime);
//
//        return str1 + " " + String.format(getString(R.string.TransactionDetails_from), str2);
//    }
//
//    private String getShortAddress(String addr) {
//        String p1 = addr.substring(0, 5);
//        String p2 = addr.substring(addr.length() - 5, addr.length());
//        return p1 + "..." + p2;
//    }
}
