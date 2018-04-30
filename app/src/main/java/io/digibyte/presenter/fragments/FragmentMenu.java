package io.digibyte.presenter.fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.databinding.FragmentMenuBinding;
import io.digibyte.presenter.activities.settings.SecurityCenterActivity;
import io.digibyte.presenter.activities.settings.SettingsActivity;
import io.digibyte.presenter.entities.BRMenuItem;
import io.digibyte.presenter.fragments.interfaces.MenuDialogCallback;
import io.digibyte.presenter.fragments.interfaces.OnBackPressListener;
import io.digibyte.tools.animation.BRAnimator;

/**
 * BreadWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/29/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class FragmentMenu extends Fragment implements OnBackPressListener {
    private FragmentMenuBinding binding;
    private Handler handler = new Handler(Looper.getMainLooper());

    private MenuDialogCallback mMenuDialogCallback = () -> fadeOutRemove(null, false, true);

    public static void show(Activity activity) {
        FragmentMenu fragmentMenu = new FragmentMenu();
        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.from_bottom, R.animator.to_bottom,
                R.animator.from_bottom, R.animator.to_bottom);
        transaction.add(android.R.id.content, fragmentMenu,
                FragmentMenu.class.getName());
        transaction.addToBackStack(FragmentMenu.class.getName());
        transaction.commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentMenuBinding.inflate(inflater);
        binding.setCallback(mMenuDialogCallback);
        binding.menuListview.setAdapter(
                new MenuListAdapter(getContext(), R.layout.menu_list_item, populateMenuList()));
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ObjectAnimator colorFade = BRAnimator.animateBackgroundDim(binding.background, false, null);
        colorFade.setStartDelay(350);
        colorFade.setDuration(500);
        colorFade.start();
    }

    private List<BRMenuItem> populateMenuList() {
        List<BRMenuItem> itemList = new ArrayList<>();
        itemList.add(
                new BRMenuItem(getString(R.string.MenuButton_security), R.drawable.ic_shield, v -> {
                    fadeOutRemove(new Intent(getActivity(), SecurityCenterActivity.class), false, false);
                }));
        itemList.add(new BRMenuItem(getString(R.string.MenuButton_settings), R.drawable.ic_settings,
                v -> {
                    fadeOutRemove(new Intent(getActivity(), SettingsActivity.class), false, false);
                }));
        itemList.add(new BRMenuItem(getString(R.string.MenuButton_lock), R.drawable.ic_lock, v -> {
            fadeOutRemove(null, true, false);
        }));
        return itemList;
    }

    public class MenuListAdapter extends ArrayAdapter<BRMenuItem> {
        private Context mContext;
        private int defaultLayoutResource = R.layout.menu_list_item;

        public MenuListAdapter(@NonNull Context context, @LayoutRes int resource,
                @NonNull List<BRMenuItem> items) {
            super(context, resource, items);
            this.mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(defaultLayoutResource, parent, false);
            }
            TextView text = convertView.findViewById(R.id.item_text);
            ImageView icon = convertView.findViewById(R.id.item_icon);
            text.setText(Objects.requireNonNull(getItem(position)).text);
            icon.setImageResource(Objects.requireNonNull(getItem(position)).resId);
            convertView.setOnClickListener(Objects.requireNonNull(getItem(position)).listener);
            return convertView;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }
    }

    private void fadeOutRemove(Intent intent, boolean openLockScreen, boolean justClose) {
        ObjectAnimator colorFade = BRAnimator.animateBackgroundDim(binding.background, true,
                () -> {
                    remove();
                    if (justClose) {
                        return;
                    }
                    handler.postDelayed(() -> {
                        if (openLockScreen) {
                            BRAnimator.startBreadActivity(DigiByte.getContext(), true);
                        } else {
                            DigiByte.getContext().startActivity(intent);
                        }
                    }, 350);
                });
        colorFade.start();
    }

    private void remove() {
        if (getFragmentManager() == null) {
            return;
        }
        getFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressed() {
        fadeOutRemove(null, false, true);
    }
}