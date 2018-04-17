package io.digibyte.presenter.activities;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import io.digibyte.DigiByte;
import io.digibyte.wallet.BRWalletManager;

/**
 * Created by nseidm1 on 4/15/18.
 */

public class RestartService extends Service {

    public static final String RESTART_ACTIVITY = "RestartService:RestartActivity";

    public static void restart(BRWalletManager.SmartInitType smartInitType) {
        Intent intent = new Intent(DigiByte.getContext(), RestartService.class);
        intent.putExtra(RESTART_ACTIVITY, smartInitType);
        DigiByte.getContext().startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getSerializableExtra(RESTART_ACTIVITY) == null) {
            return Service.START_NOT_STICKY;
        }
        SystemClock.sleep(500);
        switch ((BRWalletManager.SmartInitType) intent.getSerializableExtra(RESTART_ACTIVITY)) {
            case BreadActivity: {
                Intent newIntent = new Intent(this, BreadActivity.class);
                startActivity(newIntent);
                break;
            }
            case LoginActivity: {
                Intent newIntent = new Intent(this, LoginActivity.class);
                startActivity(newIntent);
                break;
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
