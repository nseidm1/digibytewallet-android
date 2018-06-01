package io.digibyte.presenter.activities.util;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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

    public static boolean isMainThread(){
        boolean isMain = Looper.myLooper() == Looper.getMainLooper();
        if(isMain){
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
            final BigDecimal btcAmount = BRExchange.getBitcoinForSatoshis(context,
                    amount);
            final String formattedBTCAmount = BRCurrency.getFormattedCurrencyString(
                    context, "DGB", btcAmount);

            //amount in currency units
            final BigDecimal curAmount = BRExchange.getAmountFromSatoshis(context,
                    iso, amount);
            final String formattedCurAmount = BRCurrency.getFormattedCurrencyString(
                    context, iso, curAmount);
            new Handler(Looper.getMainLooper()).post(() -> {
                primary.setText(formattedBTCAmount);
                secondary.setText(String.format("%s", formattedCurAmount));
            });
        });
    }

    public static class RootUtil {
        public static boolean isDeviceRooted() {
            return checkRootMethod1() || checkRootMethod2();
        }

        private static boolean checkRootMethod1() {
            String[] paths =
                    {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
                            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
            for (String path : paths) {
                if (new File(path).exists()) return true;
            }
            return false;
        }

        private static boolean checkRootMethod2() {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                if (in.readLine() != null) return true;
                return false;
            } catch (Throwable t) {
                return false;
            } finally {
                if (process != null) process.destroy();
            }
        }
    }

    public static void showJailbrokenDialog(AppCompatActivity context) {
        BRDialog.showCustomDialog(context, context.getString(R.string.JailbreakWarnings_title),
                context.getString(R.string.JailbreakWarnings_messageWithoutBalance),
                context.getString(R.string.JailbreakWarnings_close), null,
                brDialogView -> {
                    context.finishAffinity();
                }, null, null, 0);
    }
}
