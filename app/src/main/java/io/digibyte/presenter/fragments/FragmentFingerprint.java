package io.digibyte.presenter.fragments;/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.databinding.FingerprintDialogContainerBinding;
import io.digibyte.presenter.fragments.interfaces.FingerprintFragmentCallback;
import io.digibyte.presenter.fragments.models.FingerprintFragmentViewModel;
import io.digibyte.presenter.interfaces.BRAuthCompletion;
import io.digibyte.tools.security.AuthManager;
import io.digibyte.tools.security.FingerprintUiHelper;


/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
public class FragmentFingerprint extends Fragment implements FingerprintUiHelper.Callback {
    public static final String TAG = FragmentFingerprint.class.getName();
    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintUiHelper.FingerprintUiHelperBuilder mFingerprintUiHelperBuilder;
    private FingerprintUiHelper mFingerprintUiHelper;
    private FingerprintFragmentViewModel viewModel;
    private BRAuthCompletion completion;

    private final FingerprintManager mFingerprintManager =
            (FingerprintManager) DigiByte.getContext().getSystemService(
                    Activity.FINGERPRINT_SERVICE);

    FingerprintDialogContainerBinding binding;

    private FingerprintFragmentCallback callback = new FingerprintFragmentCallback() {
        @Override
        public void onCancelClick() {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.remove(FragmentFingerprint.this).commitAllowingStateLoss();
        }

        @Override
        public void onSecondButtonClick() {
            goToBackup();
        }
    };

    public static FragmentFingerprint newInstance(String title, String message) {
        FragmentFingerprint fingerprintFragment = new FragmentFingerprint();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        fingerprintFragment.setArguments(args);
        return fingerprintFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FingerprintDialogContainerBinding.inflate(inflater);
         viewModel = new FingerprintFragmentViewModel();
        binding.setData(viewModel);
        binding.setCallback(callback);
        binding.executePendingBindings();
        viewModel.setTitle(getArguments().getString("title"));
        viewModel.setMessage(getArguments().getString("message"));
        viewModel.setCancelButtonLabel(getString(R.string.Button_cancel));
        viewModel.setSecondaryButtonLabel(getString(R.string.Prompts_TouchId_usePin_android));
        mFingerprintUiHelperBuilder = new FingerprintUiHelper.FingerprintUiHelperBuilder(
                mFingerprintManager);
        mFingerprintUiHelper = mFingerprintUiHelperBuilder.build(binding.fingerprintIcon,
                binding.fingerprintStatus, this, getContext());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFingerprintUiHelper.startListening(mCryptoObject);
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }

    /**
     * Switches to backup (password) screen. This either can happen when fingerprint is not
     * available or the user chooses to use the password authentication method by pressing the
     * button. This can also happen when the user had too many fingerprint attempts.
     */
    private void goToBackup() {
        AuthManager.getInstance().authPromptWithFingerprint(getContext(), getArguments().getString("title"),
                getArguments().getString("message"), false, completion);
    }

    @Override
    public void onAuthenticated() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.remove(FragmentFingerprint.this).commitAllowingStateLoss();
        if (completion != null) completion.onComplete();
    }

    public void setCompletion(BRAuthCompletion completion) {
        this.completion = completion;
    }

    @Override
    public void onError() {
        goToBackup();
    }
}