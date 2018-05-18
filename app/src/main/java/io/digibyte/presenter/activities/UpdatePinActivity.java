package io.digibyte.presenter.activities;

import android.os.Bundle;

import io.digibyte.R;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.animation.SpringAnimator;
import io.digibyte.tools.security.AuthManager;

public class UpdatePinActivity extends BasePinActivity {
    int mode = ENTER_PIN;
    public static final int ENTER_PIN = 1;
    public static final int ENTER_NEW_PIN = 2;
    public static final int RE_ENTER_NEW_PIN = 3;
    private String curNewPin = "";

    @Override
    protected void onPinConfirmed() {
        switch (mode) {
            case ENTER_PIN:
                if (AuthManager.getInstance().checkAuth(pin.toString(), this)) {
                    setMode(ENTER_NEW_PIN);
                } else {
                    SpringAnimator.failShakeAnimation(this, binding.pinLayout);
                }
                clearDots();
                pin = new StringBuilder("");
                clearDots();
                break;
            case ENTER_NEW_PIN:
                setMode(RE_ENTER_NEW_PIN);
                curNewPin = pin.toString();
                clearDots();
                break;

            case RE_ENTER_NEW_PIN:
                if (curNewPin.equalsIgnoreCase(pin.toString())) {
                    AuthManager.getInstance().setPinCode(pin.toString(), this);
                    BRAnimator.showBreadSignal(this, getString(R.string.Alerts_pinSet), getString(R.string.UpdatePin_caption), R.drawable.signal_icon_graphic, () -> BRAnimator.startBreadActivity(UpdatePinActivity.this, false));
                } else {
                    SpringAnimator.failShakeAnimation(this, binding.pinLayout);
                    setMode(ENTER_NEW_PIN);
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMode(ENTER_PIN);
        setToolbarTitle(R.string.UpdatePin_updateTitle);
    }

    private void setMode(int mode) {
        String text = "";
        this.mode = mode;
        switch (mode) {
            case ENTER_PIN:
                text = getString(R.string.UpdatePin_enterCurrent);
                break;
            case ENTER_NEW_PIN:
                text = getString(R.string.UpdatePin_enterNew);
                break;
            case RE_ENTER_NEW_PIN:
                text = getString(R.string.UpdatePin_reEnterNew);
                break;
        }
        binding.description.setText(text);
        SpringAnimator.springView(binding.description);
    }
}