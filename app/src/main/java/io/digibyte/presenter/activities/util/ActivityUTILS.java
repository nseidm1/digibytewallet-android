package io.digibyte.presenter.activities.util;

import static android.content.Context.ACTIVITY_SERVICE;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

import io.digibyte.R;
import io.digibyte.presenter.activities.BasePinActivity;
import io.digibyte.presenter.activities.DisabledActivity;
import io.digibyte.presenter.activities.InputWordsActivity;
import io.digibyte.tools.animation.BRDialog;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.threads.BRExecutor;
import io.digibyte.tools.util.BRCurrency;
import io.digibyte.tools.util.BRExchange;


/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 4/27/17.
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
public class ActivityUTILS {

    private static final String TAG = ActivityUTILS.class.getName();

    //return true if the app does need to show the disabled wallet screen
    public static boolean isAppSafe(Activity app) {
        return app instanceof BasePinActivity || app instanceof InputWordsActivity;
    }

    public static void showWalletDisabled(Activity app) {
        Intent intent = new Intent(app, DisabledActivity.class);
        app.startActivity(intent);
        app.overridePendingTransition(R.anim.fade_up, R.anim.fade_down);
        Log.e(TAG, "showWalletDisabled: " + app.getClass().getName());

    }

    public static boolean isLast(Activity app) {
        ActivityManager mngr = (ActivityManager) app.getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);

        if (taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(app.getClass().getName())) {
            return true;
        }
        return false;
    }

    public static boolean isMainThread() {
        boolean isMain = Looper.myLooper() == Looper.getMainLooper();
        if (isMain) {
            Log.e(TAG, "IS MAIN UI THREAD!");
        }
        return isMain;
    }

    public static void updateDigibyteDollarValues(Context context, TextView primary,
            TextView secondary) {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(() -> {
            final String iso = BRSharedPrefs.getIso(context);

            //current amount in satoshis
            final BigDecimal amount = new BigDecimal(
                    BRSharedPrefs.getCatchedBalance(context));

            //amount in BTC units
            final BigDecimal btcAmount = BRExchange.getBitcoinForSatoshis(context, amount);
            float currentBtc = getCurrentAmount(primary).floatValue();
            if (isSameValue(btcAmount, primary)) {
                return;
            }
            primary.setTag(btcAmount);

            //amount in currency units
            final BigDecimal curAmount = BRExchange.getAmountFromSatoshis(context, iso, amount);
            float currentAmount = getCurrentAmount(secondary).floatValue();
            if (isSameValue(curAmount, secondary)) {
                return;
            }
            secondary.setTag(curAmount);

            new Handler(Looper.getMainLooper()).post(() -> {

                float[] btcIntervals = getIntervals(currentBtc, btcAmount.floatValue());
                ValueAnimator btcAnimator = ValueAnimator.ofFloat(btcIntervals);
                btcAnimator.addUpdateListener(animation -> {
                    float value = (float) animation.getAnimatedValue();
                    primary.setText(BRCurrency.getFormattedCurrencyString(
                            context, "DGB", value));
                });
                btcAnimator.setDuration(1500);
                btcAnimator.setInterpolator(new DecelerateInterpolator());
                btcAnimator.start();


                float[] fiatIntervals = getIntervals(currentAmount, curAmount.floatValue());
                ValueAnimator fiatAnimator = ValueAnimator.ofFloat(fiatIntervals);
                fiatAnimator.addUpdateListener(animation -> {
                    float value = (float) animation.getAnimatedValue();
                    secondary.setText(BRCurrency.getFormattedCurrencyString(
                            context, iso, value));
                });
                fiatAnimator.setDuration(1500);
                fiatAnimator.setInterpolator(new DecelerateInterpolator());
                fiatAnimator.start();
            });

        });
    }

    private static boolean isSameValue(BigDecimal current, View view) {
        return view.getTag() != null && current.equals(view.getTag());
    }

    private static float[] getIntervals(float start, float finish) {
        if (finish < start) {
            return new float[]{start, finish};
        }
        float[] intervals = new float[40];
        for (int i = 1; i <= 40; i++) {
            intervals[i - 1] = start + (finish - start) * ((float) i * .025f);
        }
        return intervals;
    }

    private static BigDecimal getCurrentAmount(TextView amount) {
        return amount.getTag() != null ? ((BigDecimal) amount.getTag()) : BigDecimal.ZERO;
    }

    public static void showJailbrokenDialog(AppCompatActivity context) {
        BRDialog.showCustomDialog(context, context.getString(R.string.JailbreakWarnings_title),
                context.getString(R.string.JailbreakWarnings_messageWithoutBalance),
                context.getString(R.string.JailbreakWarnings_close), null,
                brDialogView -> {
                    context.finishAffinity();
                }, null, brDialogView -> {
                    context.finishAffinity();
                }, 0);
    }

    public static boolean isvm() {

        StringBuilder deviceInfo = new StringBuilder();
        deviceInfo.append("Build.PRODUCT " + Build.PRODUCT + "\n");
        deviceInfo.append("Build.FINGERPRINT " + Build.FINGERPRINT + "\n");
        deviceInfo.append("Build.MANUFACTURER " + Build.MANUFACTURER + "\n");
        deviceInfo.append("Build.MODEL " + Build.MODEL + "\n");
        deviceInfo.append("Build.BRAND " + Build.BRAND + "\n");
        deviceInfo.append("Build.DEVICE " + Build.DEVICE + "\n");
        String info = deviceInfo.toString();

        Log.i("LOB", info);

        Boolean isvm = false;
        if (
                "google_sdk".equals(Build.PRODUCT) ||
                        "sdk_google_phone_x86".equals(Build.PRODUCT) ||
                        "sdk".equals(Build.PRODUCT) ||
                        "sdk_x86".equals(Build.PRODUCT) ||
                        "vbox86p".equals(Build.PRODUCT) ||
                        Build.FINGERPRINT.contains("generic") ||
                        Build.MANUFACTURER.contains("Genymotion") ||
                        Build.MODEL.contains("Emulator") ||
                        Build.MODEL.contains("Android SDK built for x86")
                ) {
            isvm = true;
        }

        if (Build.BRAND.contains("generic") && Build.DEVICE.contains("generic")) {
            isvm = true;
        }

        return isvm;
    }
}