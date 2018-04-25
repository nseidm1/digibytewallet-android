package io.digibyte.presenter.fragments.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.text.TextUtils;

import io.digibyte.BR;
import io.digibyte.DigiByte;
import io.digibyte.R;

public class PinFragmentViewModel extends BaseObservable {

    private String title;
    private String message;

    public void setTitle(String title) {
        this.title = title;
        if (TextUtils.isEmpty(this.title)) {
            this.title = DigiByte.getContext().getString(R.string.VerifyPin_title);
        }
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setMessage(String message) {
        this.message = message;
        if (TextUtils.isEmpty(this.message)) {
            this.message = DigiByte.getContext().getString(R.string.VerifyPin_authorize);
        }
        notifyPropertyChanged(BR.message);
    }

    @Bindable
    public String getMessage() {
        return message;
    }
}
