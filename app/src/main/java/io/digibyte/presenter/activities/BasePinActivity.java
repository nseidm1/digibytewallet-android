package io.digibyte.presenter.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import io.digibyte.R;
import io.digibyte.databinding.ActivityPinTemplateBinding;
import io.digibyte.presenter.activities.models.PinActivityModel;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.tools.security.AuthManager;

public abstract class BasePinActivity extends BRActivity {

    private static final String PIN_STATE = "ReEnterPinActivity:PinState";
    protected StringBuilder pin = new StringBuilder();
    protected ActivityPinTemplateBinding binding;
    protected abstract void onPinConfirmed();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pin_template);
        setupToolbar();
        binding.brkeyboard.addOnInsertListener(key -> handleClick(key));
        binding.brkeyboard.setShowDot(false);
        binding.setData(new PinActivityModel());
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
        if (key == null) {
            return;
        }
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

    private void updateDots() {
        AuthManager.getInstance().updateDots(pin.toString(), binding.dot1, binding.dot2, binding.dot3, binding.dot4, binding.dot5, binding.dot6, () -> new Handler().postDelayed(() -> {
            onPinConfirmed();
        }, 100));
    }
}
