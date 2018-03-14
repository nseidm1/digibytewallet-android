package io.digibyte;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.buglife.sdk.Buglife;
import com.buglife.sdk.InvocationMethod;

import java.util.ArrayList;

import io.digibyte.presenter.activities.DisabledActivity;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.manager.BREventManager;
import io.digibyte.tools.security.BRKeyStore;


/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 7/22/15.
 * Copyright (c) 2016 breadwallet LLC
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

public class DigiByte extends Application implements Application.ActivityLifecycleCallbacks
{
    private static final String TAG = DigiByte.class.getName();

    public static final String HOST = "digibyte.io";
    public static final String LocalBroadcastOnEnterForeground = "OnEnterForeground";
    public static final String LocalBroadcastOnEnterBackground = "OnEnterBackground";

    private static DigiByte application;
    public static DigiByte getContext() { return application; }

    private long suspendedTime;
    private boolean isSuspendedFlag;
    private ArrayList<Activity> activityList;
    public boolean isSuspended() { return isSuspendedFlag; }

    // TODO: Replace this with an atomic integer counter, we should avoid keeping references of activities
    private Activity activeActivity;
    public Activity getActivity() { return activeActivity; }

    @Override
    public void onCreate()
    {
        application = this;

        activeActivity = null;
        suspendedTime = 0;
        isSuspendedFlag = false;
        activityList = new ArrayList<>();

        registerActivityLifecycleCallbacks(this);

        super.onCreate();

        // Fill in API key here
        Buglife.initWithApiKey(this, "");
        Buglife.setInvocationMethod(InvocationMethod.SHAKE);

        // Register receivers
        LocalBroadcastManager.getInstance(this).registerReceiver(onApplicationEnterForeground, new IntentFilter(LocalBroadcastOnEnterForeground));
        LocalBroadcastManager.getInstance(this).registerReceiver(onApplicationEnterBackground, new IntentFilter(LocalBroadcastOnEnterBackground));
    }

    //////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// Broadcast Receivers //////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////
    private BroadcastReceiver onApplicationEnterForeground = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, "onApplicationEnterForeground");

            if(null != activeActivity && !(activeActivity instanceof DisabledActivity))
            {
                // lock wallet if 3 minutes passed
                if (suspendedTime != 0 && (System.currentTimeMillis() - suspendedTime >= 180 * 1000))
                {
                    if (!BRKeyStore.getPinCode(activeActivity).isEmpty())
                    {
                        BRAnimator.startBreadActivity(activeActivity, true);
                    }
                }
            }
            suspendedTime = 0;
        }
    };

    private BroadcastReceiver onApplicationEnterBackground = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, "onApplicationEnterBackground");

            BREventManager.getInstance().onEnterBackground();

            suspendedTime = System.currentTimeMillis();
        }
    };

    //////////////////////////////////////////////////////////////////////////////////
    //////////// Implementation of ActivityLifecycleCallbacks interface //////////////
    //////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Activity anActivity, Bundle aBundle)
    {
        addActivity(anActivity);
    }

    @Override
    public void onActivityStarted(Activity anActivity)
    {
        addActivity(anActivity);
    }

    @Override
    public void onActivityResumed(Activity anActivity)
    {
        activeActivity = anActivity;
    }

    @Override
    public void onActivityStopped(Activity anActivity)
    {
        removeActivity(anActivity);
    }

    @Override
    public void onActivityDestroyed(Activity anActivity)
    {
        removeActivity(anActivity);
    }

    @Override public void onActivityPaused(Activity anActivity) {}
    @Override public void onActivitySaveInstanceState(Activity anActivity, Bundle aBundle) {}

    private void addActivity(Activity anActivity)
    {
        if(!activityList.contains(anActivity))
        {
            activityList.add(anActivity);
        }

        if(activityList.size() > 0 && isSuspendedFlag)
        {
            isSuspendedFlag = false;
            activeActivity = anActivity;
            LocalBroadcastManager.getInstance(anActivity).sendBroadcast(new Intent(LocalBroadcastOnEnterForeground));
        }
    }

    private void removeActivity(Activity anActivity)
    {
        activityList.remove(anActivity);
        if(activityList.size() == 0 && !isSuspendedFlag)
        {
            activeActivity = null;
            isSuspendedFlag = true;
            LocalBroadcastManager.getInstance(anActivity).sendBroadcast(new Intent(LocalBroadcastOnEnterBackground));
        }
    }
}