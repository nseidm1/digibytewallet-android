package io.digibyte.presenter.activities.settings;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import io.digibyte.R;
import io.digibyte.databinding.ActivityCreditsBinding;
import io.digibyte.presenter.activities.util.BRActivity;

public class CreditsActivity extends BRActivity {

    private ActivityCreditsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_credits);
        setupToolbar();
        setToolbarTitle(R.string.Settings_credits);

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String versionName = pInfo != null ? pInfo.versionName : "";
        binding.setVersion(versionName);
    }
}
