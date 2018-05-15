package io.digibyte.presenter.activities.intro;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import io.digibyte.R;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.presenter.interfaces.BRAuthCompletion;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.security.AuthManager;
import io.digibyte.tools.security.PostAuth;

public class WriteDownActivity extends BRActivity {
    private static final String TAG = WriteDownActivity.class.getName();
    private Button writeButton;
    private ImageButton close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_down);
        writeButton = findViewById(R.id.button_write_down);
        close = findViewById(R.id.close_button);
        close.setOnClickListener(v -> close());
        writeButton.setOnClickListener(v -> {
            if (!BRAnimator.isClickAllowed()) return;
            AuthManager.getInstance().authPrompt(WriteDownActivity.this, null,
                    getString(R.string.VerifyPin_continueBody), new BRAuthCompletion() {
                        @Override
                        public void onComplete() {
                            PostAuth.getInstance().onPhraseCheckAuth(WriteDownActivity.this,
                                    false);
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
        });
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            close();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    private void close() {
        Log.e(TAG, "close: ");
        BRAnimator.startBreadActivity(this, false);
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
        if (!isDestroyed()) finish();
    }
}