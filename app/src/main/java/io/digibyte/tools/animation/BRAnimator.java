package io.digibyte.tools.animation;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;

import io.digibyte.R;
import io.digibyte.presenter.activities.BreadActivity;
import io.digibyte.presenter.activities.LoginActivity;
import io.digibyte.presenter.activities.camera.ScanQRActivity;
import io.digibyte.presenter.customviews.BRDialogView;
import io.digibyte.presenter.fragments.FragmentMenu;
import io.digibyte.presenter.fragments.FragmentReceive;
import io.digibyte.presenter.fragments.FragmentRequestAmount;
import io.digibyte.presenter.fragments.FragmentSend;
import io.digibyte.presenter.fragments.FragmentSignal;
import io.digibyte.presenter.fragments.FragmentTransactionDetails;
import io.digibyte.presenter.interfaces.BROnSignalCompletion;
import io.digibyte.tools.list.items.ListItemTransactionData;
import io.digibyte.tools.threads.BRExecutor;
import io.digibyte.tools.util.BRConstants;


/**
 * BreadWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 7/13/15.
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

public class BRAnimator {
    private static final String TAG = BRAnimator.class.getName();
    private static boolean clickAllowed = true;
    public static int SLIDE_ANIMATION_DURATION = 300;

    public static void showBreadSignal(AppCompatActivity activity, String title, String iconDescription,
                                       int drawableId, BROnSignalCompletion completion) {
        FragmentSignal fragmentSignal = new FragmentSignal();
        Bundle bundle = new Bundle();
        bundle.putString(FragmentSignal.TITLE, title);
        bundle.putString(FragmentSignal.ICON_DESCRIPTION, iconDescription);
        fragmentSignal.setCompletion(completion);
        bundle.putInt(FragmentSignal.RES_ID, drawableId);
        fragmentSignal.setArguments(bundle);
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.from_bottom, R.animator.to_bottom,
                R.animator.from_bottom, R.animator.to_bottom);
        transaction.add(android.R.id.content, fragmentSignal, FragmentSignal.class.getName());
        transaction.addToBackStack(FragmentSignal.class.getName());
        transaction.commitAllowingStateLoss();
    }

    public static void showOrUpdateSendFragment(AppCompatActivity app, final String bitcoinUrl) {
        FragmentSend fragmentSend = (FragmentSend) app.getSupportFragmentManager().findFragmentByTag(FragmentSend.class.getName());
        if (fragmentSend != null) {
            fragmentSend.setUrl(bitcoinUrl);
            return;
        }
        FragmentSend.show(app, bitcoinUrl);
    }

    public static void showTransactionPager(AppCompatActivity app, ArrayList<ListItemTransactionData> items,
            int position) {
        FragmentTransactionDetails.show(app, items, position);
    }

    public static void openScanner(Activity app, int requestID) {
        try {
            if (app == null) return;

            // Check if the camera permission is granted
            if (ContextCompat.checkSelfPermission(app,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(app,
                        Manifest.permission.CAMERA)) {
                    BRDialog.showCustomDialog(app,
                            app.getString(R.string.Send_cameraUnavailabeTitle_android),
                            app.getString(R.string.Send_cameraUnavailabeMessage_android),
                            app.getString(R.string.AccessibilityLabels_close), null,
                            new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismiss();
                                }
                            }, null, null, 0);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(app,
                            new String[]{Manifest.permission.CAMERA},
                            BRConstants.CAMERA_REQUEST_ID);
                }
            } else {
                // Permission is granted, open camera
                Intent intent = new Intent(app, ScanQRActivity.class);
                app.startActivityForResult(intent, requestID);
                app.overridePendingTransition(R.anim.fade_up, R.anim.fade_down);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static LayoutTransition getDefaultTransition() {
        LayoutTransition itemLayoutTransition = new LayoutTransition();
        itemLayoutTransition.setStartDelay(LayoutTransition.APPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.CHANGING, 0);
        itemLayoutTransition.setDuration(100);
        itemLayoutTransition.setInterpolator(LayoutTransition.CHANGING,
                new OvershootInterpolator(2f));
        Animator scaleUp = ObjectAnimator.ofPropertyValuesHolder((Object) null,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0, 1));
        scaleUp.setDuration(50);
        scaleUp.setStartDelay(50);
        Animator scaleDown = ObjectAnimator.ofPropertyValuesHolder((Object) null,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0));
        scaleDown.setDuration(2);
        itemLayoutTransition.setAnimator(LayoutTransition.APPEARING, scaleUp);
        itemLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, null);
        itemLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        return itemLayoutTransition;
    }

    public static void showRequestFragment(AppCompatActivity app) {
        FragmentRequestAmount.show(app);
    }

    //isReceive tells the Animator that the Receive fragment is requested, not My Address
    public static void showReceiveFragment(AppCompatActivity app, boolean isReceive) {
        FragmentReceive.show(app, isReceive);
    }

    public static void showMenuFragment(AppCompatActivity app) {
        if (app == null) {
            Log.e(TAG, "showReceiveFragment: app is null");
            return;
        }
        FragmentMenu.show(app);
    }

    public static boolean isClickAllowed() {
        if (clickAllowed) {
            clickAllowed = false;
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    clickAllowed = true;
                }
            });
            return true;
        } else {
            return false;
        }
    }

    public static void startBreadActivity(Context from, boolean auth) {
        if (from == null) return;
        Log.e(TAG, "startBreadActivity: " + from.getClass().getName());
        Class toStart = auth ? LoginActivity.class : BreadActivity.class;
        Intent intent = new Intent(from, toStart);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        from.startActivity(intent);
    }

    public static void openBreadActivity(Context from, boolean auth) {
        if (from == null) return;
        Log.e(TAG, "startBreadActivity: " + from.getClass().getName());
        Class toStart = auth ? LoginActivity.class : BreadActivity.class;
        Intent intent = new Intent(from, toStart);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        from.startActivity(intent);
    }

    public static void animateSignalSlide(ViewGroup signalLayout, final boolean reverse,
            final OnSlideAnimationEnd listener) {
        float translationY = signalLayout.getTranslationY();
        float signalHeight = signalLayout.getHeight();
        signalLayout.setTranslationY(reverse ? translationY : translationY + signalHeight);


        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        signalLayout.animate().translationY(reverse ? screenHeight : translationY).setDuration(
                SLIDE_ANIMATION_DURATION)
                .setInterpolator(
                        reverse ? new DecelerateInterpolator() : new OvershootInterpolator(0.7f))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (listener != null) {
                            listener.onAnimationEnd();
                        }
                    }
                });
    }

    @Deprecated
    public static void animateBackgroundDim(final ViewGroup backgroundLayout, boolean reverse) {
        int transColor = reverse ? R.color.black_trans : android.R.color.transparent;
        int blackTransColor = reverse ? android.R.color.transparent : R.color.black_trans;

        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(transColor, blackTransColor);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                backgroundLayout.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        anim.setDuration(SLIDE_ANIMATION_DURATION);
        anim.start();
    }

    public static ObjectAnimator animateBackgroundDim(final ViewGroup backgroundLayout,
            boolean reverse, OnAnimationEndListener onAnimationEndListener) {
        ObjectAnimator colorFade =
                ObjectAnimator.ofObject(backgroundLayout, "backgroundColor", new ArgbEvaluator(),
                        reverse ? Color.argb(200, 0, 0, 0) : Color.argb(0, 0, 0, 0),
                        reverse ? Color.argb(0, 0, 0, 0) : Color.argb(200, 0, 0, 0));
        colorFade.setDuration(SLIDE_ANIMATION_DURATION);
        colorFade.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEndListener != null) {
                    onAnimationEndListener.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return colorFade;
    }

    public interface OnAnimationEndListener {
        void onAnimationEnd();
    }

    public interface OnSlideAnimationEnd {
        void onAnimationEnd();
    }
}