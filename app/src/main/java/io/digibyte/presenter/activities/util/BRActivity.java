package io.digibyte.presenter.activities.util;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.platform.tools.BRBitId;

import java.util.concurrent.CopyOnWriteArrayList;

import io.digibyte.presenter.fragments.interfaces.OnBackPressListener;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.security.AuthManager;
import io.digibyte.tools.security.BitcoinUrlHandler;
import io.digibyte.tools.security.PostAuth;
import io.digibyte.tools.threads.BRExecutor;
import io.digibyte.tools.util.BRConstants;
import io.digibyte.wallet.BRWalletManager;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 5/23/17.
 * Copyright (c) 2017 breadwallet LLC
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
public class BRActivity extends Activity implements FragmentManager.OnBackStackChangedListener {
    private final String TAG = this.getClass().getName();
    private CopyOnWriteArrayList<OnBackPressListener> backClickListeners = new CopyOnWriteArrayList<>();

    static {
        System.loadLibrary(BRConstants.NATIVE_LIB_NAME);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (!ActivityUTILS.isAppSafe(this)) {
            if (AuthManager.getInstance().isWalletDisabled(this)) {
                AuthManager.getInstance().setWalletDisabled(this);
            }
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        // 123 is the qrCode result
        switch (requestCode) {

            case BRConstants.PAY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(
                            () -> PostAuth.getInstance().onPublishTxAuth(BRActivity.this, true));
                }
                break;
            case BRConstants.PAYMENT_PROTOCOL_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onPaymentProtocolRequest(this, true);
                }
                break;

            case BRConstants.CANARY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onCanaryCheck(this, true);
                } else {
                    finish();
                }
                break;

            case BRConstants.SHOW_PHRASE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onPhraseCheckAuth(this, true);
                }
                break;
            case BRConstants.PROVE_PHRASE_REQUEST:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onPhraseProveAuth(this, true);
                }
                break;
            case BRConstants.PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onRecoverWalletAuth(this, true);
                } else {
                    finish();
                }
                break;

            case BRConstants.SCANNER_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String result = data.getStringExtra("result");
                            if (BitcoinUrlHandler.isBitcoinUrl(result)) {
                                BRAnimator.showSendFragment(BRActivity.this, result);
                            } else if (BRBitId.isBitId(result)) {
                                BRBitId.signAndRespond(BRActivity.this, result, false);
                            } else {
                                Log.e(TAG, "onActivityResult: not bitcoin address NOR bitID");
                            }
                        }
                    }, 500);
                }
                break;
            case BRConstants.SCANNER_BCH_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String result = data.getStringExtra("result");
                            PostAuth.getInstance().onSendBch(BRActivity.this, true, result);
                        }
                    }, 500);
                }
                break;

            case BRConstants.PUT_PHRASE_NEW_WALLET_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onCreateWalletAuth(this, true);
                } else {
                    Log.e(TAG, "WARNING: resultCode != RESULT_OK");
                    BRWalletManager m = BRWalletManager.getInstance();
                    m.wipeWalletButKeystore(this);
                    finish();
                }
                break;
        }
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
    public void onBackPressed() {
        if (backClickListeners.size() == 0) {
            super.onBackPressed();
        } else {
            for (OnBackPressListener onBackPressListener : backClickListeners) {
                onBackPressListener.onBackPressed();
            }
        }
    }

    @Override
    public void onBackStackChanged() {
        //Add back press listeners
        backClickListeners.clear();
        for (int i = 0; i < getFragmentManager().getBackStackEntryCount(); i++) {
            FragmentManager.BackStackEntry backStackEntry = getFragmentManager().getBackStackEntryAt(i);
            Fragment fragment = getFragmentManager().findFragmentByTag(backStackEntry.getName());
            if (fragment instanceof OnBackPressListener) {
                OnBackPressListener onBackPressListener = (OnBackPressListener) fragment;
                if (!backClickListeners.contains(onBackPressListener)) {
                    backClickListeners.add(onBackPressListener);
                }
            }
        }
    }
}