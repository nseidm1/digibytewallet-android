package io.digibyte.presenter.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import io.digibyte.R;
import io.digibyte.databinding.ActivityPinTemplateBinding;
import io.digibyte.presenter.activities.models.PinActivityModel;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.security.AuthManager;

public class SetPinActivity extends BRActivity {
    private static final String TAG = SetPinActivity.class.getName();
    ActivityPinTemplateBinding binding;
    private StringBuilder pin = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pin_template);
        binding.brkeyboard.addOnInsertListener(key -> handleClick(key));
        binding.brkeyboard.setShowDot(false);
        binding.setData(new PinActivityModel());
        BRSharedPrefs.putGreetingsShown(this, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDots();
    }

    private void handleClick(String key) {
        if (key == null) {
            Log.e(TAG, "handleClick: key is null! ");
            return;
        }
        if (key.isEmpty()) {
            handleDeleteClick();
        } else if (Character.isDigit(key.charAt(0))) {
            handleDigitClick(Integer.parseInt(key.substring(0, 1)));
        } else {
            Log.e(TAG, "handleClick: oops: " + key);
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
            Intent intent = new Intent(SetPinActivity.this, ReEnterPinActivity.class);
            intent.putExtra("pin", pin.toString());
            intent.putExtra("noPin", getIntent().getBooleanExtra("noPin", false));
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            pin = new StringBuilder("");
        }, 100));
    }
}