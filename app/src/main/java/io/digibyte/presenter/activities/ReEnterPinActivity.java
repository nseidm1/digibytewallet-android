package io.digibyte.presenter.activities;

import android.os.Bundle;

import io.digibyte.R;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.animation.SpringAnimator;
import io.digibyte.tools.security.AuthManager;
import io.digibyte.tools.security.PostAuth;

public class ReEnterPinActivity extends BasePinActivity {

    private String firstPIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.setConfirmPin(true);
        setToolbarTitle(R.string.UpdatePin_createTitleConfirm);
        firstPIN = getIntent().getExtras().getString("pin");
    }

    @Override
    protected void onPinConfirmed() {
        if (firstPIN.equalsIgnoreCase(pin.toString())) {
            AuthManager.getInstance().authSuccess(this);
            AuthManager.getInstance().setPinCode(pin.toString(), this);
            if (getIntent().getBooleanExtra("noPin", false)) {
                BRAnimator.startBreadActivity(this, false);
            } else {
                BRAnimator.showBreadSignal(this, getString(R.string.Alerts_pinSet),
                        getString(R.string.UpdatePin_createInstruction),
                        R.drawable.signal_icon_graphic,
                        () -> PostAuth.getInstance().onCreateWalletAuth(ReEnterPinActivity.this,
                                false));
            }
        } else {
            AuthManager.getInstance().authFail(this);
            SpringAnimator.failShakeAnimation(this, binding.pinLayout);
            pin = new StringBuilder();
        }
    }
}