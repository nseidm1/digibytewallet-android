package io.digibyte.presenter.activities.settings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import io.digibyte.R;
import io.digibyte.presenter.activities.InputWordsActivity;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.tools.animation.BRAnimator;


public class WipeActivity extends BRActivity {
    private Button nextButton;
    private ImageButton close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);

        nextButton = findViewById(R.id.send_button);
        close = findViewById(R.id.close_button);

        nextButton.setOnClickListener(v -> {
            InputWordsActivity.open(WipeActivity.this, InputWordsActivity.Type.WIPE);
        });
        close.setOnClickListener(v -> {
            if (!BRAnimator.isClickAllowed()) return;
            onBackPressed();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
    }
}