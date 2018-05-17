package io.digibyte.presenter.fragments.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.text.TextUtils;

import java.math.BigDecimal;

import io.digibyte.BR;
import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.util.BRCurrency;
import io.digibyte.tools.util.BRExchange;
import io.digibyte.wallet.BRWalletManager;

public class SendFragmentModel extends BaseObservable {

    private StringBuilder amountBuilder = new StringBuilder("");
    private String selectedIso = BRSharedPrefs.getPreferredBTC(DigiByte.getContext()) ? "DGB"
            : BRSharedPrefs.getIso(DigiByte.getContext());
    private String enteredAddress = "";
    private FeeType feeType = FeeType.REGULAR;
    private String memo = "";
    private boolean showSendWaiting = false;

    enum FeeType {
        REGULAR, ECONOMY
    }

    @Bindable
    public int getBRKeyboardColor() {
        return R.color.keyboard_text_color;
    }

    @Bindable
    public String getIsoText() {
        return BRCurrency.getSymbolByIso(DigiByte.getContext(), getSelectedIso());
    }

    @Bindable
    public String getIsoButtonText() {
        return String.format("%s(%s)",
                BRCurrency.getCurrencyName(DigiByte.getContext(), getSelectedIso()),
                BRCurrency.getSymbolByIso(DigiByte.getContext(), getSelectedIso()));
    }

    @Bindable
    public String getAddress() {
        return enteredAddress;
    }

    public void setAddress(String address) {
        this.enteredAddress = address;
        notifyPropertyChanged(BR.address);
    }

    @Bindable
    public int getBalanceTextColor() {
        if (validAmount() && new BigDecimal(getAmount()).doubleValue()
                > getBalanceForISO().doubleValue()) {
            return DigiByte.getContext().getColor(R.color.warning_color);
        } else {
            return DigiByte.getContext().getColor(R.color.white);
        }
    }

    @Bindable
    public int getFeeTextColor() {
        if (validAmount() && new BigDecimal(getAmount()).doubleValue()
                > getBalanceForISO().doubleValue()) {
            return DigiByte.getContext().getColor(R.color.warning_color);
        } else {
            return DigiByte.getContext().getColor(R.color.white);
        }
    }

    @Bindable
    public int getAmountEditTextColor() {
        if (validAmount() && new BigDecimal(getAmount()).doubleValue()
                > getBalanceForISO().doubleValue()) {
            return DigiByte.getContext().getColor(R.color.warning_color);
        } else {
            return DigiByte.getContext().getColor(R.color.white);
        }
    }

    @Bindable
    public int getISOTextColor() {
        if (validAmount() && new BigDecimal(getAmount()).doubleValue()
                > getBalanceForISO().doubleValue()) {
            return DigiByte.getContext().getColor(R.color.warning_color);
        } else {
            return DigiByte.getContext().getColor(R.color.white);
        }
    }

    @Bindable
    public String getBalanceText() {
        return String.format("%s", getFormattedBalance());
    }

    @Bindable
    public String getFeeText() {
        String approximateFee = getApproximateFee();
        if (TextUtils.isEmpty(approximateFee)) {
            return "";
        }
        return String.format(DigiByte.getContext().getString(R.string.Send_fee), approximateFee);
    }

    @Bindable
    public String getAmount() {
        return amountBuilder.toString();
    }

    @Bindable
    public String getMemo() {
        return memo;
    }

    @Bindable
    public int getShowSendWaiting() {
        return showSendWaiting ? 1 : 0;
    }

    @Bindable
    public boolean getMaxSendVisibility() {
        return BRSharedPrefs.getGenericSettingsSwitch(DigiByte.getContext(), "max_send_enabled");
    }

    public void showSendWaiting(boolean show) {
        showSendWaiting = show;
        notifyPropertyChanged(BR.showSendWaiting);
    }

    public void setMemo(String memo) {
        this.memo = memo;
        notifyPropertyChanged(BR.memo);
    }

    public void setSelectedIso(String iso) {
        selectedIso = iso;
    }

    public String getSelectedIso() {
        return selectedIso;
    }

    private boolean validAmount() {
        try {
            new BigDecimal(getAmount());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void appendAmount(Integer append) {
        amountBuilder.append(append);
        notifyPropertyChanged(BR.amountEditTextColor);
    }

    public void appendAmount(String append) {
        amountBuilder.append(append);
        notifyPropertyChanged(BR.amountEditTextColor);
    }

    public void setAmount(String amount) {
        amountBuilder = new StringBuilder(amount);
        notifyPropertyChanged(BR.amountEditTextColor);
    }

    public void handleDeleteClick() {
        if (!validAmount()) {
            return;
        }
        amountBuilder.deleteCharAt(getAmount().length() - 1);
    }

    private long getFee() {
        if (getSatoshis() == 0 || TextUtils.isEmpty(getAddress())) {
            return 0;
        }
        if (TextUtils.isEmpty(getAddress()) || !BRWalletManager.validateAddress(getAddress())) {
            return BRWalletManager.getInstance().feeForTransactionAmount(getSatoshis());
        } else {
            return BRWalletManager.getInstance().feeForTransaction(getAddress(), getSatoshis());
        }
    }

    private BigDecimal getFeeForISO() {
        return BRExchange.getAmountFromSatoshis(DigiByte.getContext(), getSelectedIso(),
                new BigDecimal(getFee()));
    }

    private String getApproximateFee() {
        return BRCurrency.getFormattedCurrencyString(DigiByte.getContext(), getSelectedIso(),
                getFeeForISO());
    }

    private long getSatoshis() {
        if (!validAmount()) {
            return 0;
        }
        long satoshis = selectedIso.equalsIgnoreCase("dgb") ? BRExchange.getSatoshisForBitcoin(
                DigiByte.getContext(), new BigDecimal(getAmount())).longValue()
                : BRExchange.getSatoshisFromAmount(DigiByte.getContext(), getSelectedIso(),
                        new BigDecimal(getAmount())).longValue();
        return satoshis;
    }

    public void populateMaxAmount() {
        setSelectedIso("dgb");
        setAmount(new BigDecimal(
                BRWalletManager.getInstance().getBalance(DigiByte.getContext())).divide(
                new BigDecimal(100000000)).toString());
        notifyPropertyChanged(BR.amount);
    }

    private BigDecimal getBalanceForISO() {
        return BRExchange.getAmountFromSatoshis(DigiByte.getContext(), getSelectedIso(),
                new BigDecimal(BRWalletManager.getInstance().getBalance(DigiByte.getContext())));
    }

    private String getFormattedBalance() {
        return BRCurrency.getFormattedCurrencyString(DigiByte.getContext(), getSelectedIso(),
                getBalanceForISO());
    }

    public void updateText() {
        notifyPropertyChanged(BR.isoButtonText);
        notifyPropertyChanged(BR.amount);
        notifyPropertyChanged(BR.isoText);
        notifyPropertyChanged(BR.balanceText);
        notifyPropertyChanged(BR.feeText);
        notifyPropertyChanged(BR.balanceTextColor);
        notifyPropertyChanged(BR.feeTextColor);
        notifyPropertyChanged(BR.amountEditTextColor);
        notifyPropertyChanged(BR.iSOTextColor);
    }
}