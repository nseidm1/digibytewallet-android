package io.digibyte.presenter.activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import io.digibyte.DigiByte;
import io.digibyte.wallet.BRWalletManager;

/**
 * Created by nseidm1 on 4/15/18.
 */

public class RestartService extends JobIntentService {

    public static final String RESTART_ACTIVITY = "RestartService:RestartActivity";
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void restart(BRWalletManager.SmartInitType smartInitType) {
        Intent intent = new Intent(DigiByte.getContext(), RestartService.class);
        intent.putExtra(RESTART_ACTIVITY, smartInitType);
        DigiByte.getContext().startService(intent);
    }
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent == null || intent.getSerializableExtra(RESTART_ACTIVITY) == null) {
            return;
        }
        SystemClock.sleep(250);
        switch((BRWalletManager.SmartInitType) intent.getSerializableExtra(RESTART_ACTIVITY)) {
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
        stopSelf();
    }
}
