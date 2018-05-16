package io.digibyte.presenter.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import io.digibyte.R;
import io.digibyte.databinding.ActivitySettingsBinding;
import io.digibyte.presenter.activities.SettingsCallback;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.presenter.entities.BRSettingsItem;
import io.digibyte.presenter.interfaces.BRAuthCompletion;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.security.AuthManager;

import static io.digibyte.R.layout.settings_list_item;
import static io.digibyte.R.layout.settings_list_section;
import static io.digibyte.R.layout.settings_switch;

public class SettingsActivity extends BRActivity {
    public List<BRSettingsItem> items = new LinkedList();
    private SettingsListAdapter adapter;

    private SettingsCallback callback = new SettingsCallback() {
        private int count = 0;

        @Override
        public void onTitleClick() {
            count++;
            if (count == 5) {
                items.add(new BRSettingsItem(getString(R.string.Settings_advancedTitle), "", v -> {
                    Intent intent = new Intent(SettingsActivity.this, AdvancedActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                }, BRSettingsItem.Type.ITEM));
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        binding.setCallback(callback);
        populateItems();
        adapter = new SettingsListAdapter(this, R.layout.settings_list_item, items);
        binding.setAdapter(adapter);
    }

    public class SettingsListAdapter extends ArrayAdapter<String> {

        private List<BRSettingsItem> items;
        private Context mContext;

        public SettingsListAdapter(@NonNull Context context, @LayoutRes int resource,
                                   @NonNull List<BRSettingsItem> items) {
            super(context, resource);
            this.items = items;
            this.mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            BRSettingsItem item = items.get(position);
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            switch(item.type) {
                default:
                case ITEM: {
                    View v = inflater.inflate(settings_list_item, parent, false);
                    TextView addon = v.findViewById(R.id.item_addon);
                    addon.setText(item.addonText);
                    v.setOnClickListener(item.listener);
                    ((TextView) v.findViewById(R.id.item_title)).setText(item.title);
                    return v;
                }
                case SECTION: {
                    View v = inflater.inflate(settings_list_section, parent, false);
                    ((TextView) v.findViewById(R.id.item_title)).setText(item.title);
                    return v;
                }
                case SWITCH:
                    View v = inflater.inflate(settings_switch, parent, false);
                    ((TextView) v.findViewById(R.id.item_title)).setText(item.title);
                    ((SwitchCompat) v.findViewById(R.id.settings_switch)).setChecked(BRSharedPrefs.getGenericSettingsSwitch(SettingsActivity.this, item.switchKey));
                    ((SwitchCompat) v.findViewById(R.id.settings_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            BRSharedPrefs.setGenericSettingsSwitch(SettingsActivity.this, item.switchKey, isChecked);
                        }
                    });
                    return v;
            }

        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
    }

    private void populateItems() {

        items.add(new BRSettingsItem(getString(R.string.Settings_wallet), "", null, BRSettingsItem.Type.SECTION));

        //No support currently in BRActivity for the concept of import wallet from QR
        /*items.add(new BRSettingsItem(getString(R.string.Settings_importTitle), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, ImportActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);

        }, false));*/

        items.add(new BRSettingsItem(getString(R.string.Settings_wipe), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, WipeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
        }, BRSettingsItem.Type.ITEM));

        items.add(new BRSettingsItem(getString(R.string.Settings_manage), "", null, BRSettingsItem.Type.SECTION));

        if (AuthManager.isFingerPrintAvailableAndSetup(this)) {
            items.add(new BRSettingsItem(getString(R.string.Settings_touchIdLimit_android), "",
                    v -> AuthManager.getInstance().authPrompt(SettingsActivity.this, null,
                            getString(R.string.VerifyPin_continueBody),
                            new BRAuthCompletion() {
                                @Override
                                public void onComplete() {
                                    Intent intent = new Intent(SettingsActivity.this,
                                            SpendLimitActivity.class);
                                    overridePendingTransition(R.anim.enter_from_right,
                                            R.anim.exit_to_left);
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancel() {

                                }
                            }), BRSettingsItem.Type.ITEM));
        }

        items.add(new BRSettingsItem(getString(R.string.max_send_enabled),"max_send_enabled", BRSettingsItem.Type.SWITCH));

        items.add(new BRSettingsItem(getString(R.string.Settings_currency),
                BRSharedPrefs.getIso(this), v -> {
            Intent intent = new Intent(SettingsActivity.this, DisplayCurrencyActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, BRSettingsItem.Type.ITEM));

        items.add(new BRSettingsItem(getString(R.string.Settings_sync), "", v -> {
            Intent intent = new Intent(SettingsActivity.this,
                    SyncBlockchainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, BRSettingsItem.Type.ITEM));

        items.add(new BRSettingsItem(getString(R.string.Settings_about), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, BRSettingsItem.Type.ITEM));
    }
}