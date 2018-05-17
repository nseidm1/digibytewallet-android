package io.digibyte.presenter.fragments.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class FragmentSignalViewModel extends BaseObservable {

    private String title;
    private String description;
    private int icon;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    @Bindable
    public int getIcon() {
        return icon;
    }
}
