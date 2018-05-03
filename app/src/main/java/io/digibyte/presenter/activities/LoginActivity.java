package io.digibyte.presenter.activities;

import static io.digibyte.R.color.white;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.platform.tools.BRBitId;

import io.digibyte.R;
import io.digibyte.databinding.ActivityPinBinding;
import io.digibyte.presenter.activities.camera.ScanQRActivity;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.presenter.interfaces.BRAuthCompletion;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.animation.SpringAnimator;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.security.AuthManager;
import io.digibyte.tools.security.BRKeyStore;
import io.digibyte.tools.security.BitcoinUrlHandler;
import io.digibyte.tools.util.Utils;
import io.digibyte.wallet.BRWalletManager;

public class LoginActivity extends BRActivity {
    private static final String TAG = LoginActivity.class.getName();
    ActivityPinBinding binding;
    private StringBuilder pin = new StringBuilder();
    private int pinLimit = 6;
    private boolean inputAllowed = true;

    private View.OnClickListener fingerprintClickListener =
            v -> AuthManager.getInstance().authPrompt(LoginActivity.this, "", "",
                    new BRAuthCompletion() {
                        @Override
                        public void onComplete() {
                            unlockWallet();
                        }

                        @Override
                        public void onCancel() {

                        }
                    });

    private View.OnClickListener leftButtonClickListener = v -> BRAnimator.showReceiveFragment(
            LoginActivity.this, false);

    private View.OnClickListener rightButtonClickListener =
            v -> ScanQRActivity.openScanner(LoginActivity.this);

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pin);
        binding.setLeftButtonClickListener(leftButtonClickListener);
        binding.setRightButtonClickListener(rightButtonClickListener);
        setupPinLimits();
        setupKeyboard();
        setUpOfflineButtons();
        boolean useFingerprint = setupFingerprintIcon();
        if (!processDeepLink(getIntent()) && useFingerprint) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                binding.fingerprintIcon.performClick();
            }, 500);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processDeepLink(intent);
    }

    private final boolean processDeepLink(@Nullable final Intent intent) {
        Uri data = intent.getData();
        if (data != null && BRBitId.isBitId(data.toString())) {
            BRBitId.signAndRespond(this, data.toString(), true);
            return true;
        } else if (data != null && BitcoinUrlHandler.isBitcoinUrl(data.toString())) {
            BRAnimator.showSendFragment(this, data.toString());
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDots();

        inputAllowed = true;
        BRWalletManager.getInstance().smartInit(this, BRWalletManager.SmartInitType.LoginActivity);
    }

    private final void handleClick(String key) {
        if (!inputAllowed) {
            Log.e(TAG, "handleClick: input not allowed");
            return;
        }
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


    private final void handleDigitClick(Integer dig) {
        if (pin.length() < pinLimit) {
            pin.append(dig);
        }
        updateDots();
    }

    private final void handleDeleteClick() {
        if (pin.length() > 0) {
            pin.deleteCharAt(pin.length() - 1);
        }
        updateDots();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        } else {
            finishAffinity();
        }
    }

    private final void unlockWallet() {
        BRAnimator.startBreadActivity(this, false);
    }

    private final void showFailedToUnlock() {
        SpringAnimator.failShakeAnimation(LoginActivity.this, binding.pinLayout);
        pin = new StringBuilder("");
        new Handler().postDelayed(() -> {
            inputAllowed = true;
            updateDots();
        }, 1000);
    }

    private final void updateDots() {
        AuthManager.getInstance().updateDots(this, pinLimit, pin.toString(), binding.dot1,
                binding.dot2, binding.dot3, binding.dot4,
                binding.dot5, binding.dot6, R.drawable.ic_pin_dot_white, () -> {
                    inputAllowed = false;
                    if (AuthManager.getInstance().checkAuth(pin.toString(),
                            LoginActivity.this)) {
                        AuthManager.getInstance().authSuccess(LoginActivity.this);
                        unlockWallet();
                    } else {
                        AuthManager.getInstance().authFail(LoginActivity.this);
                        showFailedToUnlock();
                    }
                });
    }

    private final void setUpOfflineButtons() {
        int activeColor = getColor(white);
        GradientDrawable leftDrawable =
                (GradientDrawable) binding.leftButton.getBackground().getCurrent();
        GradientDrawable rightDrawable =
                (GradientDrawable) binding.rightButton.getBackground().getCurrent();

        int rad = Utils.getPixelsFromDps(this,
                (int) getResources().getDimension(R.dimen.radius) / 2);
        int stoke = 2;

        leftDrawable.setCornerRadii(new float[]{rad, rad, 0, 0, 0, 0, rad, rad});
        rightDrawable.setCornerRadii(new float[]{0, 0, rad, rad, rad, rad, 0, 0});

        leftDrawable.setStroke(stoke, activeColor, 0, 0);
        rightDrawable.setStroke(stoke, activeColor, 0, 0);
        binding.leftButton.setTextColor(activeColor);
        binding.rightButton.setTextColor(activeColor);
    }

    private final boolean setupFingerprintIcon() {
        final boolean useFingerprint = AuthManager.isFingerPrintAvailableAndSetup(this)
                && BRSharedPrefs.getUseFingerprint(this);
        binding.fingerprintIcon.setVisibility(useFingerprint ? View.VISIBLE : View.GONE);
        binding.setFingerprintCallback(fingerprintClickListener);
        return useFingerprint;
    }

    private final void setupPinLimits() {
        String pin = BRKeyStore.getPinCode(this);
        if (pin.isEmpty() || (pin.length() != 6 && pin.length() != 4)) {
            Intent intent = new Intent(this, SetPinActivity.class);
            intent.putExtra("noPin", true);
            startActivity(intent);
            if (!LoginActivity.this.isDestroyed()) finish();
            return;
        }
        if (BRKeyStore.getPinCode(this).length() == 4) pinLimit = 4;
    }

    private final void setupKeyboard() {
        binding.brkeyboard.addOnInsertListener(key -> handleClick(key));
        binding.brkeyboard.setBRButtonBackgroundResId(R.drawable.keyboard_trans_button);
        binding.brkeyboard.setBRButtonTextColor(R.color.white);
        binding.brkeyboard.setShowDot(false);
        binding.brkeyboard.setBreadground(getDrawable(R.drawable.bread_gradient));
        binding.brkeyboard.setCustomButtonBackgroundColor(10,
                getColor(android.R.color.transparent));
        binding.brkeyboard.setDeleteImage(getDrawable(R.drawable.ic_delete_white));
    }
}