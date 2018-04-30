package io.digibyte.presenter.activities.settings;

import android.content.Intent;
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
            if (!BRAnimator.isClickAllowed()) return;
            Intent intent = new Intent(WipeActivity.this, InputWordsActivity.class);
            intent.putExtra("restore", true);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            if (!WipeActivity.this.isDestroyed()) finish();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }
}