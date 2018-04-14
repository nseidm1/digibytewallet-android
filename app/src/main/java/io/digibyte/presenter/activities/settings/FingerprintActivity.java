package io.digibyte.presenter.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.math.BigDecimal;

import io.digibyte.R;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.presenter.customviews.BRDialogView;
import io.digibyte.presenter.interfaces.BRAuthCompletion;
import io.digibyte.tools.animation.BRDialog;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.security.AuthManager;
import io.digibyte.tools.security.BRKeyStore;
import io.digibyte.tools.util.BRCurrency;
import io.digibyte.tools.util.BRExchange;
import io.digibyte.tools.util.Utils;


public class FingerprintActivity extends BRActivity {
    private static final String TAG = FingerprintActivity.class.getName();

    public RelativeLayout layout;
    private TextView limitExchange;
    private TextView limitInfo;

    private ToggleButton toggleButton;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);
        toggleButton = findViewById(R.id.toggleButton);
        limitExchange = findViewById(R.id.limit_exchange);
        limitInfo = findViewById(R.id.limit_info);
        toggleButton.setChecked(BRSharedPrefs.getUseFingerprint(this));
        limitExchange.setText(getLimitText());
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Activity app = FingerprintActivity.this;
            if (isChecked && !Utils.isFingerprintAvailable(app)) {
                Log.e(TAG, "onCheckedChanged: fingerprint not setup");
                BRDialog.showCustomDialog(app,
                        getString(R.string.TouchIdSettings_disabledWarning_title_android),
                        getString(R.string.TouchIdSettings_disabledWarning_body_android),
                        getString(R.string.Button_ok), null,
                        new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismissWithAnimation();
                            }
                        }, null, null, 0);
                buttonView.setChecked(false);
            } else {
                BRSharedPrefs.putUseFingerprint(app, isChecked);
            }

        });
        SpannableString ss = new SpannableString(
                getString(R.string.TouchIdSettings_customizeText_android));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                AuthManager.getInstance().authPrompt(FingerprintActivity.this, null,
                        getString(R.string.VerifyPin_continueBody), new BRAuthCompletion() {
                            @Override
                            public void onComplete() {
                                Intent intent = new Intent(FingerprintActivity.this,
                                        SpendLimitActivity.class);
                                overridePendingTransition(R.anim.enter_from_right,
                                        R.anim.exit_to_left);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        //start index of the last space (beginning of the last word)
        int indexOfSpace = limitInfo.getText().toString().lastIndexOf(" ");
        // make the whole text clickable if failed to select the last word
        ss.setSpan(clickableSpan, indexOfSpace == -1 ? 0 : indexOfSpace,
                limitInfo.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        limitInfo.setText(ss);
        limitInfo.setMovementMethod(LinkMovementMethod.getInstance());
        limitInfo.setHighlightColor(Color.TRANSPARENT);

    }

    private String getLimitText() {
        String iso = BRSharedPrefs.getIso(this);
        //amount in satoshis
        BigDecimal digibyte = new BigDecimal(BRKeyStore.getSpendLimit(this));
        BigDecimal curAmount = BRExchange.getAmountFromSatoshis(this, iso, digibyte);
        //formatted string for the label
        return String.format(getString(R.string.TouchIdSettings_spendingLimit),
                BRCurrency.getFormattedCurrencyString(this, "DGB", digibyte),
                BRCurrency.getFormattedCurrencyString(this, iso, curAmount));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}