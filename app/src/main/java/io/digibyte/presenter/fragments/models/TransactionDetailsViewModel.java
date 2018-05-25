package io.digibyte.presenter.fragments.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.text.TextUtils;

import com.platform.entities.TxMetaData;
import com.platform.tools.KVStoreManager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.tools.database.Database;
import io.digibyte.tools.database.DigiTransaction;
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
    public String getCryptoAmount() {
        if (!BRSharedPrefs.getPreferredBTC(DigiByte.getContext()) && currentFiatAmountEqualsOriginalFiatAmount()) {
            return getRawFiatAmount(item);
        } else {
            boolean received = item.getSent() == 0;
            long satoshisAmount = received ? item.getReceived() : (item.getSent() - item.getReceived());
            return BRCurrency.getFormattedCurrencyString(DigiByte.getContext(), "DGB",
                    BRExchange.getAmountFromSatoshis(DigiByte.getContext(), "DGB",
                            new BigDecimal(satoshisAmount)));
        }
    }

    public static String getRawFiatAmount(TxItem txItem) {
        boolean received = txItem.getSent() == 0;
        long satoshisAmount = received ? txItem.getReceived() : (txItem.getSent() - txItem.getReceived());
        return BRCurrency.getFormattedCurrencyString(DigiByte.getContext(),
                BRSharedPrefs.getIso(DigiByte.getContext()),
                BRExchange.getAmountFromSatoshis(DigiByte.getContext(),
                        BRSharedPrefs.getIso(DigiByte.getContext()),
                        new BigDecimal(satoshisAmount)));
    }

    @Bindable
    public String getFiatAmount() {
        return String.format(DigiByte.getContext().getString(R.string.current_amount), getRawFiatAmount(item));
    }

    public String getOriginalFiatAmount() {
        DigiTransaction transaction = Database.instance.findTransaction(item.getTxHash());
        return String.format(DigiByte.getContext().getString(R.string.original_amount), transaction.getTxAmount());
    }

    public boolean currentFiatAmountEqualsOriginalFiatAmount() {
        DigiTransaction transaction = Database.instance.findTransaction(item.getTxHash());
        boolean received = item.getSent() == 0;
        long satoshisAmount = received ? item.getReceived() : (item.getSent() - item.getReceived());
        return transaction.getTxAmount().equals(BRCurrency.getFormattedCurrencyString(DigiByte.getContext(),
                BRSharedPrefs.getIso(DigiByte.getContext()),
                BRExchange.getAmountFromSatoshis(DigiByte.getContext(),
                        BRSharedPrefs.getIso(DigiByte.getContext()),
                        new BigDecimal(satoshisAmount))));
    }

    @Bindable
    public String getToFrom() {
        return item.getReceived() - item.getSent() < 0 ? DigiByte.getContext().getString(
                R.string.sent) : DigiByte.getContext().getString(R.string.received);
    }

    @Bindable
    public boolean getSent() {
        return item.getReceived() - item.getSent() < 0;
    }

    @Bindable
    public String getAddress() {
        return getSent() ? item.getTo()[0] : item.getFrom()[0];
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
    public String getDate() {
        return getFormattedDate(item.getTimeStamp());
    }

    @Bindable
    public String getTime() {
        return getFormattedTime(item.getTimeStamp());
    }

    @Bindable
    public boolean getCompleted() {
        return BRSharedPrefs.getLastBlockHeight(DigiByte.getContext()) - item.getBlockHeight() + 1
                >= 6;
    }

    @Bindable
    public String getFee() {
        if (item.getSent() == 0) {
            return "";
        }
        String approximateFee = BRCurrency.getFormattedCurrencyString(DigiByte.getContext(), "DGB",
                BRExchange.getAmountFromSatoshis(DigiByte.getContext(), "DGB",
                        new BigDecimal(item.getFee())));
        if (TextUtils.isEmpty(approximateFee)) {
            approximateFee = "";
        }
        return DigiByte.getContext().getString(R.string.Send_fee).replace("%1$s", approximateFee);
    }

    @Bindable
    public String getMemo() {
        return item.metaData != null ? item.metaData.comment : "";
    }

    public void setMemo(String memo) {
        TxMetaData metaData = item.metaData;
        if (metaData == null) {
            item.metaData = new TxMetaData();
        }
        item.metaData.comment = memo;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(() -> {
            KVStoreManager.getInstance().putTxMetaData(DigiByte.getContext(), item.metaData, item.getTxHash());
            TxManager.getInstance().updateTxList();
        });
    }

    private String getFormattedDate(long timeStamp) {
        Date currentLocalTime = new Date(
                timeStamp == 0 ? System.currentTimeMillis() : timeStamp * 1000);
        SimpleDateFormat date1 = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        return date1.format(currentLocalTime);
    }

    private String getFormattedTime(long timeStamp) {
        Date currentLocalTime = new Date(
                timeStamp == 0 ? System.currentTimeMillis() : timeStamp * 1000);
        SimpleDateFormat date2 = new SimpleDateFormat("HH:mm a", Locale.getDefault());
        return date2.format(currentLocalTime);
    }


}
