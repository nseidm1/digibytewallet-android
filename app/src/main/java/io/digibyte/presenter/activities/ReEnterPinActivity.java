package io.digibyte.presenter.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import io.digibyte.R;
import io.digibyte.databinding.ActivityPinTemplateBinding;
import io.digibyte.presenter.activities.models.PinActivityModel;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.animation.SpringAnimator;
import io.digibyte.tools.security.AuthManager;
import io.digibyte.tools.security.PostAuth;

public class ReEnterPinActivity extends BRActivity {
    private static final String PIN_STATE = "ReEnterPinActivity:PinState";
    private StringBuilder pin = new StringBuilder();
    private String firstPIN;
    private ActivityPinTemplateBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pin_template);
        binding.setData(new PinActivityModel());
        binding.title.setText(getString(R.string.UpdatePin_createTitleConfirm));
        firstPIN = getIntent().getExtras().getString("pin");
        binding.brkeyboard.addOnInsertListener(key -> handleClick(key));
        binding.brkeyboard.setShowDot(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDots();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(PIN_STATE, pin);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getSerializable(PIN_STATE) != null) {
            pin = (StringBuilder) savedInstanceState.getSerializable(PIN_STATE);
        }
    }

    private void handleClick(String key) {
        if (key.isEmpty()) {
            handleDeleteClick();
        } else if (Character.isDigit(key.charAt(0))) {
            handleDigitClick(Integer.parseInt(key.substring(0, 1)));
        }
    }

    private void handleDigitClick(Integer dig) {
        if (pin.length() < 6)
            pin.append(dig);
        updateDots();
    }

    private void handleDeleteClick() {
        if (pin.length() > 0)
            pin.deleteCharAt(pin.length() - 1);
        updateDots();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    private void updateDots() {
        AuthManager.getInstance().updateDots(pin.toString(), binding.dot1, binding.dot2, binding.dot3, binding.dot4, binding.dot5, binding.dot6, () -> new Handler().postDelayed(() -> {
            verifyPin();
        }, 100));
    }

    private void verifyPin() {
        if (firstPIN.equalsIgnoreCase(pin.toString())) {
            AuthManager.getInstance().authSuccess(this);
            AuthManager.getInstance().setPinCode(pin.toString(), this);
            if (getIntent().getBooleanExtra("noPin", false)) {
                BRAnimator.startBreadActivity(this, false);
            } else {
                BRAnimator.showBreadSignal(this, getString(R.string.Alerts_pinSet), getString(R.string.UpdatePin_createInstruction), R.drawable.signal_icon_graphic, () -> PostAuth.getInstance().onCreateWalletAuth(ReEnterPinActivity.this, false));
            }

        } else {
            AuthManager.getInstance().authFail(this);
            SpringAnimator.failShakeAnimation(this, binding.pinLayout);
            pin = new StringBuilder();
        }
    }
}
