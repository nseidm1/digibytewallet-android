package io.digibyte.presenter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import io.digibyte.R;

public class SetPinActivity extends BasePinActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolbarTitle(R.string.UpdatePin_createTitle);
    }

    @Override
    protected void onPinConfirmed() {
        Intent intent = new Intent(SetPinActivity.this, ReEnterPinActivity.class);
        intent.putExtra("pin", pin.toString());
        intent.putExtra("noPin", getIntent().getBooleanExtra("noPin", false));
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        pin = new StringBuilder("");
    }
}