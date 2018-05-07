package io.digibyte.presenter.fragments;

import static io.digibyte.tools.animation.BRAnimator.animateBackgroundDim;
import static io.digibyte.tools.animation.BRAnimator.animateSignalSlide;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.databinding.FragmentReceiveBinding;
import io.digibyte.presenter.customviews.BRButton;
import io.digibyte.presenter.customviews.BRKeyboard;
import io.digibyte.presenter.customviews.BRLinearLayoutWithCaret;
import io.digibyte.presenter.fragments.interfaces.FragmentReceiveCallbacks;
import io.digibyte.presenter.fragments.interfaces.OnBackPressListener;
import io.digibyte.presenter.fragments.models.ReceiveFragmentModel;
import io.digibyte.presenter.interfaces.BRAuthCompletion;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.animation.SlideDetector;
import io.digibyte.tools.manager.BRClipboardManager;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.qrcode.QRUtils;
import io.digibyte.tools.security.AuthManager;
import io.digibyte.tools.threads.BRExecutor;
import io.digibyte.tools.util.Utils;
import io.digibyte.wallet.BRWalletManager;

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

public class FragmentReceive extends Fragment implements OnBackPressListener {

    private static final String IS_RECEIVE = "FragmentReceive:IsReceive";
    private String receiveAddress;
    private boolean shareButtonsShown = false;
    private Handler copyCloseHandler = new Handler(Looper.getMainLooper());
    private FragmentReceiveBinding fragmentReceiveBinding;

    private ReceiveFragmentModel receiveFragmentModel = new ReceiveFragmentModel();

    private FragmentReceiveCallbacks callbacks = new FragmentReceiveCallbacks() {

        @Override
        public void shareEmailClick() {
            String bitcoinUri = Utils.createBitcoinUrl(receiveAddress, 0, null, null, null);
            Uri qrImageUri = QRUtils.getQRImageUri(getContext(), bitcoinUri);
            QRUtils.share("mailto:", getActivity(), qrImageUri, null, null);
        }

        @Override
        public void shareTextClick() {
            try {
                QRUtils.share("sms:", getActivity(), null, receiveAddress, "");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void shareButtonClick() {
            shareButtonsShown = !shareButtonsShown;
            showShareButtons(shareButtonsShown);
        }

        @Override
        public void addressClick() {
            copyText();
        }

        @Override
        public void requestButtonClick() {
            Activity app = getActivity();
            app.onBackPressed();
            BRAnimator.showRequestFragment(app, receiveAddress);
        }

        @Override
        public void backgroundClick() {
            fadeOutRemove();

        }

        @Override
        public void qrImageClick() {
            copyText();
        }

        @Override
        public void closeClick() {
            fadeOutRemove();
        }
    };

    public static void show(Activity activity, boolean isReceive) {
        FragmentReceive fragmentReceive = new FragmentReceive();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_RECEIVE, isReceive);
        fragmentReceive.setArguments(bundle);
        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.from_bottom, R.animator.to_bottom,
                R.animator.from_bottom, R.animator.to_bottom);
        transaction.add(android.R.id.content, fragmentReceive,
                FragmentReceive.class.getName());
        transaction.addToBackStack(FragmentReceive.class.getName());
        transaction.commitAllowingStateLoss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentReceiveBinding = FragmentReceiveBinding.inflate(inflater);
        fragmentReceiveBinding.setCallback(callbacks);
        fragmentReceiveBinding.setData(receiveFragmentModel);
        fragmentReceiveBinding.mainContainer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        fragmentReceiveBinding.amountLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        BRWalletManager.getInstance().addBalanceChangedListener(balance -> updateQr());
        return fragmentReceiveBinding.getRoot();
    }

    private boolean getIsReceive() {
        return getArguments().getBoolean(IS_RECEIVE, false);
    }

    private void showShareButtons(boolean show) {
        if (!show) {
            receiveFragmentModel.setShareVisibility(false);
            receiveFragmentModel.setShareButtonType(2);
        } else {
            receiveFragmentModel.setShareVisibility(true);
            receiveFragmentModel.setShareButtonType(3);
            showCopiedLayout(false);
        }
    }

    private void showCopiedLayout(boolean show) {
        if (!show) {
            receiveFragmentModel.setCopiedButtonLayoutVisibility(false);
            copyCloseHandler.removeCallbacksAndMessages(null);
        } else {
            if (!receiveFragmentModel.isCopiedButtonLayoutVisibility()) {
                receiveFragmentModel.setCopiedButtonLayoutVisibility(true);
                showShareButtons(false);
                shareButtonsShown = false;
                copyCloseHandler.postDelayed(() -> showCopiedLayout(false), 2000);
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle extras = getArguments();
        if (!getIsReceive()) {
            receiveFragmentModel.setSeparatorVisibility(false);
            receiveFragmentModel.setRequestButtonVisibility(false);
            receiveFragmentModel.setTitle(getString(R.string.UnlockScreen_myAddress));
        }
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(() -> updateQr());
        ObjectAnimator colorFade = BRAnimator.animateBackgroundDim(fragmentReceiveBinding.backgroundLayout, false, null);
        colorFade.setStartDelay(350);
        colorFade.setDuration(500);
        colorFade.start();
    }

    private void updateQr() {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(() -> {
            if(getContext() == null) {
                return;
            }
            BRWalletManager.refreshAddress(getContext());
            copyCloseHandler.post(() -> {
                receiveAddress = BRSharedPrefs.getReceiveAddress(getContext());
                receiveFragmentModel.setAddress(receiveAddress);
                QRUtils.generateQR(getContext(), "digibyte:" + receiveAddress, fragmentReceiveBinding.qrImage);
            });
        });
    }

    private void copyText() {
        BRClipboardManager.putClipboard(getContext(),receiveAddress);
        showCopiedLayout(true);
    }

    private void fadeOutRemove() {
        ObjectAnimator colorFade = BRAnimator.animateBackgroundDim(fragmentReceiveBinding.backgroundLayout, true,
                () -> {
                    remove();
                });
        colorFade.start();
    }

    private void remove() {
        if (getFragmentManager() == null) {
            return;
        }
        try { getFragmentManager().popBackStack(); }
        catch(IllegalStateException e) { e.printStackTrace(); }
    }

    @Override
    public void onBackPressed() {
        fadeOutRemove();
    }
}