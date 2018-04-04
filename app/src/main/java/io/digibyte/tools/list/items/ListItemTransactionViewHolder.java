package io.digibyte.tools.list.items;

import android.databinding.BindingAdapter;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Date;

import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.databinding.ListItemTransactionBinding;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.tools.list.ListItemData;
import io.digibyte.tools.list.ListItemViewHolder;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.util.BRCurrency;
import io.digibyte.tools.util.BRDateUtil;
import io.digibyte.tools.util.BRExchange;
import io.digibyte.wallet.BRPeerManager;

public class ListItemTransactionViewHolder extends ListItemViewHolder {
    private ListItemTransactionBinding binding;

    public ListItemTransactionViewHolder(ListItemTransactionBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void process(ListItemData aListItemData) {
        super.process(aListItemData);
        binding.setData((ListItemTransactionData) aListItemData);
    }

    @BindingAdapter("transactionComment")
    public static void setTransactionComment(TextView textView,
            ListItemTransactionData listItemTransactionData) {
        TxItem item = listItemTransactionData.transactionItem;
        String commentString = (item.metaData == null || item.metaData.comment == null) ? ""
                : item.metaData.comment;

        textView.setText(commentString);
        if (commentString.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }
    }

    @BindingAdapter("account")
    public static void setAccount(TextView textView,
            ListItemTransactionData listItemTransactionData) {
        TxItem item = listItemTransactionData.transactionItem;
        textView.setText(item.getTo()[0]);
    }

    @BindingAdapter("itemViewBackground")
    public static void setItemViewBackground(View view,
            ListItemTransactionData listItemTransactionData) {
        view.setBackgroundResource(getResourceByPos(listItemTransactionData.transactionIndex,
                listItemTransactionData.transactionsCount));
    }

    private static int getResourceByPos(int aPosition, int aTotal) {
        if (aTotal == 1) {
            return R.drawable.tx_rounded;
        } else if (aPosition == 0) {
            return R.drawable.tx_rounded_up;
        } else if (aPosition == aTotal - 1) {
            return R.drawable.tx_rounded_down;
        }
        return R.drawable.tx_not_rounded;
    }

    @BindingAdapter("arrowIcon")
    public static void setArrowIcon(ImageView imageView,
            ListItemTransactionData listItemTransactionData) {
        TxItem item = listItemTransactionData.transactionItem;
        boolean received = item.getSent() == 0;
        imageView.setImageResource(
                received ? R.drawable.arrow_down_bold_circle : R.drawable.arrow_up_bold_circle);
    }

    @BindingAdapter("sentReceived")
    public static void setSentReceived(TextView textView,
            ListItemTransactionData listItemTransactionData) {
        TxItem item = listItemTransactionData.transactionItem;
        boolean received = item.getSent() == 0;
        textView.setText(
                received ? textView.getContext().getString(R.string.TransactionDetails_received, "")
                        : textView.getContext().getString(R.string.TransactionDetails_sent, ""));
    }

    @BindingAdapter("toFrom")
    public static void setToFrom(TextView textView,
            ListItemTransactionData listItemTransactionData) {
        TxItem item = listItemTransactionData.transactionItem;
        boolean received = item.getSent() == 0;
        textView.setText(
                received ? String.format(
                        textView.getContext().getString(R.string.TransactionDetails_from), "")
                        : String.format(
                                textView.getContext().getString(R.string.TransactionDetails_to),
                                ""));
    }

    private static int getLevel(TxItem item) {
        int blockHeight = item.getBlockHeight();
        int confirms = blockHeight == Integer.MAX_VALUE ? 0 : BRSharedPrefs.getLastBlockHeight(
                DigiByte.getContext()) - blockHeight + 1;

        int level;
        if (confirms <= 0) {
            int relayCount = BRPeerManager.getRelayCount(item.getTxHash());
            if (relayCount <= 0) {
                level = 0;
            } else if (relayCount == 1) {
                level = 1;
            } else {
                level = 2;
            }
        } else {
            if (confirms == 1) {
                level = 3;
            } else if (confirms == 2) {
                level = 4;
            } else if (confirms == 3) {
                level = 5;
            } else if (confirms == 4) {
                level = 6;
            } else if (confirms == 5) {
                level = 7;
            } else if (confirms == 6) {
                level = 8;
            } else {
                level = 9;
            }
        }
        return level;
    }

    private static Pair<Boolean, String> getAvailableForSpendAndPercentageText(int level) {
        boolean availableForSpend = false;
        String percentage = "";
        switch (level) {
            case 0:
            case 1:
            case 2:
                percentage = "0%";
                break;
            case 3: //1 confirm
                percentage = "20%";
                availableForSpend = true;
                break;
            case 4: //2 confirm
                percentage = "40%";
                availableForSpend = true;
                break;
            case 5: //3 confirm
                percentage = "60%";
                availableForSpend = true;
                break;
            case 6: //4 confirm
                percentage = "60%";
                availableForSpend = true;
                break;
            case 7: //5 confirm
                percentage = "80%";
                availableForSpend = true;
                break;
            case 8: //6 confirm
                percentage = "100%";
                availableForSpend = true;
                break;
        }
        return new Pair<>(availableForSpend, percentage);
    }

    @BindingAdapter("status2")
    public static void setStatus2(TextView textView,
            ListItemTransactionData listItemTransactionData) {
        TxItem item = listItemTransactionData.transactionItem;
        boolean received = item.getSent() == 0;
        Pair<Boolean, String> availableForSpend = getAvailableForSpendAndPercentageText(
                getLevel(item));
        if (availableForSpend.first && received) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(textView.getContext().getString(R.string.Transaction_available));
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("status")
    public static void setStatus(TextView textView,
            ListItemTransactionData listItemTransactionData) {
        TxItem item = listItemTransactionData.transactionItem;
        boolean received = item.getSent() == 0;
        String sentReceived = received ? "Receiving" : "Sending";
        Pair<Boolean, String> percentage = getAvailableForSpendAndPercentageText(getLevel(item));
        int level = getLevel(item);
        if (level >= 9) {
            textView.setText(textView.getContext().getString(R.string.Transaction_complete));
        } else {
            textView.setText(String.format("%s - %s", sentReceived, percentage.second));
        }
        if (!item.isValid()) {
            textView.setText(textView.getContext().getString(R.string.Transaction_invalid));
        }
    }

    @BindingAdapter("amount")
    public static void setAmount(TextView textView,
            ListItemTransactionData listItemTransactionData) {
        TxItem item = listItemTransactionData.transactionItem;
        boolean isBTCPreferred = BRSharedPrefs.getPreferredBTC(textView.getContext());
        boolean received = item.getSent() == 0;
        String iso = isBTCPreferred ? "DGB" : BRSharedPrefs.getIso(textView.getContext());
        long satoshisAmount = received ? item.getReceived() : (item.getSent() - item.getReceived());
        textView.setText(BRCurrency.getFormattedCurrencyString(textView.getContext(), iso,
                BRExchange.getAmountFromSatoshis(textView.getContext(), iso,
                        new BigDecimal(satoshisAmount))));
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
