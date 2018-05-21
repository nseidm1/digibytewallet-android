package io.digibyte.tools.list.items;

import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Date;

import io.digibyte.R;
import io.digibyte.databinding.ListItemTransactionBinding;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.tools.database.Database;
import io.digibyte.tools.list.ListItemData;
import io.digibyte.tools.list.ListItemViewHolder;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.util.BRCurrency;
import io.digibyte.tools.util.BRDateUtil;
import io.digibyte.tools.util.BRExchange;

public class ListItemTransactionViewHolder extends ListItemViewHolder {
    public ListItemTransactionBinding binding;

    public ListItemTransactionViewHolder(ListItemTransactionBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void process(ListItemData aListItemData) {
        super.process(aListItemData);
        binding.setData((ListItemTransactionData) aListItemData);
    }

    @BindingAdapter("arrowIcon")
    public static void setArrowIcon(ImageView imageView,
            ListItemTransactionData listItemTransactionData) {
        TxItem item = listItemTransactionData.transactionItem;
        boolean received = item.getSent() == 0;
        imageView.setImageResource(received ? R.drawable.receive : R.drawable.send);
    }

    @BindingAdapter("amount")
    public static void setAmount(TextView textView,
            ListItemTransactionData listItemTransactionData) {
        TxItem item = listItemTransactionData.transactionItem;
        boolean isBTCPreferred = BRSharedPrefs.getPreferredBTC(textView.getContext());
        boolean received = item.getSent() == 0;
        String iso = isBTCPreferred ? "DGB" : BRSharedPrefs.getIso(textView.getContext());
        long satoshisAmount = received ? item.getReceived() : (item.getSent() - item.getReceived());
        textView.setTextColor(received ? Color.parseColor("#3fe77b") : Color.parseColor("#ff7416"));
        String transactionText;
        if (isBTCPreferred) {
            transactionText = BRCurrency.getFormattedCurrencyString(textView.getContext(), iso,
                    BRExchange.getAmountFromSatoshis(textView.getContext(), iso,
                            new BigDecimal(satoshisAmount)));
        } else {
            transactionText = BRCurrency.getFormattedCurrencyString(textView.getContext(), iso,
                    BRExchange.getAmountFromSatoshis(textView.getContext(), iso,
                            new BigDecimal(satoshisAmount)));
            //Save the transaction text in fiat mode, the first time the transaction is displayed in the app
            if (!Database.instance.containsTransaction(item.getTxHash())) {
                Database.instance.saveTransaction(item.getTxHash(), transactionText);
            }
        }
        textView.setText((received ? "+" : "-") + transactionText);
    }

    @BindingAdapter("timestamp")
    public static void setTimeStamp(TextView textView,
            ListItemTransactionData listItemTransactionData) {
        TxItem item = listItemTransactionData.transactionItem;
        long timeStamp =
                item.getTimeStamp() == 0 ? System.currentTimeMillis() : item.getTimeStamp() * 1000;
        CharSequence timeSpan = BRDateUtil.getCustomSpan(new Date(timeStamp));
        textView.setText(timeSpan);
    }
}
