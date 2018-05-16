package io.digibyte.presenter.activities.settings;

import android.os.Bundle;
import android.widget.Button;

import io.digibyte.R;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.animation.BRDialog;
import io.digibyte.wallet.BRWalletManager;


public class SyncBlockchainActivity extends BRActivity {
    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_blockchain);

        scanButton = findViewById(R.id.button_scan);
        scanButton.setOnClickListener(v -> {
            if (!BRAnimator.isClickAllowed()) return;
            BRDialog.showCustomDialog(SyncBlockchainActivity.this,
                    getString(R.string.ReScan_alertTitle),
                    getString(R.string.ReScan_footer), getString(R.string.ReScan_alertAction),
                    getString(R.string.Button_cancel),
                    brDialogView -> {
                        scanButton.setEnabled(false);
                        brDialogView.dismissWithAnimation();
                        BRWalletManager.getInstance().wipeBlockAndTrans(SyncBlockchainActivity.this,
                                () -> BRAnimator.startBreadActivity(SyncBlockchainActivity.this,
                                        false));
                    }, brDialogView -> brDialogView.dismissWithAnimation(), null, 0);
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}