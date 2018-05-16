package io.digibyte.presenter.activities.intro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import io.digibyte.R;
import io.digibyte.presenter.activities.InputWordsActivity;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.tools.animation.BRAnimator;

public class RecoverActivity extends BRActivity {
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_recover);
        nextButton = findViewById(R.id.send_button);
        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(RecoverActivity.this, InputWordsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}
