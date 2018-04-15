package io.digibyte.presenter.activities;

import static io.digibyte.R.color.white;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.platform.tools.BRBitId;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
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
import io.digibyte.tools.util.Utils;
import io.digibyte.wallet.BRWalletManager;

public class LoginActivity extends BRActivity {
    private static final String TAG = LoginActivity.class.getName();
    ActivityPinBinding binding;
    private StringBuilder pin = new StringBuilder();
    private int pinLimit = 6;
    private boolean inputAllowed = true;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pin);
        unbinder = ButterKnife.bind(this);
        String pin = BRKeyStore.getPinCode(this);
        if (pin.isEmpty() || (pin.length() != 6 && pin.length() != 4)) {
            Intent intent = new Intent(this, SetPinActivity.class);
            intent.putExtra("noPin", true);
            startActivity(intent);
            if (!LoginActivity.this.isDestroyed()) finish();
            return;
        }
        if (BRKeyStore.getPinCode(this).length() == 4) pinLimit = 4;
        binding.brkeyboard.addOnInsertListener(key -> handleClick(key));
        binding.brkeyboard.setBRButtonBackgroundResId(R.drawable.keyboard_trans_button);
        binding.brkeyboard.setBRButtonTextColor(R.color.white);
        binding.brkeyboard.setShowDot(false);
        binding.brkeyboard.setBreadground(getDrawable(R.drawable.bread_gradient));
        binding.brkeyboard.setCustomButtonBackgroundColor(10,
                getColor(android.R.color.transparent));
        binding.brkeyboard.setDeleteImage(getDrawable(R.drawable.ic_delete_white));
        setUpOfflineButtons();
        final boolean useFingerprint = AuthManager.isFingerPrintAvailableAndSetup(this)
                && BRSharedPrefs.getUseFingerprint(this);
        binding.fingerprintIcon.setVisibility(useFingerprint ? View.VISIBLE : View.GONE);
        binding.fingerprintIcon.setOnClickListener(v -> AuthManager.getInstance().
                authPrompt(LoginActivity.this, "", "", new BRAuthCompletion() {
                    @Override
                    public void onComplete() {
                        unlockWallet();
                    }

                    @Override
                    public void onCancel() {

                    }
                }));
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null && BRBitId.isBitId(data.toString())) {
            BRBitId.signAndRespond(this, data.toString(), true);
        } else {
            if (binding.fingerprintIcon != null && useFingerprint) {
                new Handler().postDelayed(() -> {
                    if (binding.fingerprintIcon != null && useFingerprint) {
                        binding.fingerprintIcon.performClick();
                    }
                }, 500);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri data = intent.getData();
        if (data != null && BRBitId.isBitId(data.toString())) {
            BRBitId.signAndRespond(this, data.toString(), true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.left_button)
    public void leftButtonClick(View view) {
        if (!BRAnimator.isClickAllowed()) return;
        BRAnimator.showReceiveFragment(LoginActivity.this, false);
    }

    @OnClick(R.id.right_button)
    public void rightButtonClick(View view) {
        if (!BRAnimator.isClickAllowed()) return;
        ScanQRActivity.openScanner(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDots();

        inputAllowed = true;
        BRWalletManager.getInstance().smartInit(null);
    }

    private void handleClick(String key) {
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


    private void handleDigitClick(Integer dig) {
        if (pin.length() < pinLimit) {
            pin.append(dig);
        }
        updateDots();
    }

    private void handleDeleteClick() {
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

    private void unlockWallet() {
        Intent intent = new Intent(LoginActivity.this, BreadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        startActivity(intent);
    }

    private void showFailedToUnlock() {
        SpringAnimator.failShakeAnimation(LoginActivity.this, binding.pinLayout);
        pin = new StringBuilder("");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                inputAllowed = true;
                updateDots();
            }
        }, 1000);
    }

    private void updateDots() {
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

    private void setUpOfflineButtons() {
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }
}