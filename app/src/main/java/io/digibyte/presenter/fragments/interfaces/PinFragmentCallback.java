package io.digibyte.presenter.fragments.interfaces;

import io.digibyte.presenter.customviews.BRKeyboard;

public interface PinFragmentCallback extends BRKeyboard.OnInsertListener {
    void onCancelClick();
}
