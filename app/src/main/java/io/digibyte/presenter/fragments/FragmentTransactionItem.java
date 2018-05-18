package io.digibyte.presenter.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.digibyte.R;
import io.digibyte.databinding.TransactionDetailsItemBinding;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.presenter.fragments.interfaces.TransactionDetailsCallback;
import io.digibyte.presenter.fragments.models.TransactionDetailsViewModel;
import io.digibyte.tools.animation.BRDialog;
import io.digibyte.tools.manager.BRClipboardManager;
import io.digibyte.tools.manager.BRSharedPrefs;

/**
 * BreadWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/29/15.
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

public class FragmentTransactionItem extends Fragment {
    private static final String TRANSACTION_ITEM = "FragmentTransactionItem:TransactionItem";

    private TransactionDetailsViewModel viewModel;

    private TransactionDetailsCallback callback = new TransactionDetailsCallback() {
        @Override
        public void onBackgroundClick() {
            FragmentTransactionDetails fragment =
                    (FragmentTransactionDetails) getActivity().getSupportFragmentManager()
                            .findFragmentByTag(
                                    FragmentTransactionDetails.class.getName());
            fragment.onBackPressed();
        }

        @Override
        public void onAddressClick() {
            BRClipboardManager.putClipboard(getContext(), viewModel.getAddress());
            Toast.makeText(getContext(), R.string.Receive_copied, Toast.LENGTH_SHORT).show();
        }
    };

    public static FragmentTransactionItem newInstance(TxItem item) {
        FragmentTransactionItem fragmentTransactionItem = new FragmentTransactionItem();
        Bundle bundle = new Bundle();
        bundle.putParcelable(TRANSACTION_ITEM, item);
        fragmentTransactionItem.setArguments(bundle);
        return fragmentTransactionItem;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        viewModel = new TransactionDetailsViewModel(getArguments().getParcelable(TRANSACTION_ITEM));
        TransactionDetailsItemBinding binding = TransactionDetailsItemBinding.inflate(inflater);
        binding.setData(viewModel);
        binding.setCallback(callback);
        if (BRSharedPrefs.getPreferredBTC(getContext())) {
            binding.amountText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else {
            binding.amountText.setCompoundDrawablesWithIntrinsicBounds(null, null, getContext().getDrawable(R.drawable.info), null);
            binding.amountText.setOnClickListener(
                    v -> BRDialog.showCustomDialog(getContext(), getContext().getString(R.string.Alert_info),
                            getContext().getString(R.string.Alert_Amount_info),
                            getContext().getString(R.string.AccessibilityLabels_close), null,
                            brDialogView -> brDialogView.dismissWithAnimation(), null, null, 0));
        }
        return binding.getRoot();
    }
}