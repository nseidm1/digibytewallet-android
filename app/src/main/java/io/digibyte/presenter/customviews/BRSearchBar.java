package io.digibyte.presenter.customviews;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import io.digibyte.R;
import io.digibyte.presenter.activities.BreadActivity;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 5/8/17.
 * Copyright (c) 2017 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class BRSearchBar extends android.support.v7.widget.Toolbar
{
    public interface OnSearchUpdateListener
    {
        void onSearchBarFilterUpdate();
    }

    private OnSearchUpdateListener listener;
    public void setOnUpdateListener(OnSearchUpdateListener aListener) { listener = aListener; }

    private EditText searchEdit;
    private BRButton sentFilter;
    private BRButton receivedFilter;
    private BRButton pendingFilter;
    private BRButton completedFilter;
    private BRButton cancelButton;
    private BreadActivity breadActivity;

    private boolean[] filterSwitches = new boolean[4];
    public boolean[] getFilterSwitches() { return filterSwitches; }
    public String getSearchQuery() { return searchEdit.getText().toString(); }

    public BRSearchBar(Context context)
    {
        super(context);
        init();
    }

    public BRSearchBar(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public BRSearchBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        inflate(getContext(), R.layout.search_bar, this);

        breadActivity = (BreadActivity) getContext();
        searchEdit = findViewById(R.id.search_edit);
        sentFilter = findViewById(R.id.sent_filter);
        receivedFilter = findViewById(R.id.received_filter);
        pendingFilter = findViewById(R.id.pending_filter);
        completedFilter = findViewById(R.id.complete_filter);
        cancelButton = findViewById(R.id.cancel_button);

        clearSwitches();

        setListeners();

        searchEdit.requestFocus();
        searchEdit.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                InputMethodManager keyboard = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(null != keyboard)
                {
                    keyboard.showSoftInput(searchEdit, 0);
                }
            }
        }, 200); //use 300 to make it run when coming back from lock screen
    }

    private void updateFilterButtonsUI()
    {
        sentFilter.setType(filterSwitches[0] ? 3 : 2);
        receivedFilter.setType(filterSwitches[1] ? 3 : 2);
        pendingFilter.setType(filterSwitches[2] ? 3 : 2);
        completedFilter.setType(filterSwitches[3] ? 3 : 2);

        if(null != listener)
        {
            listener.onSearchBarFilterUpdate();
        }
    }

    private void setListeners()
    {
        searchEdit.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    if (breadActivity.barFlipper != null)
                    {
                        breadActivity.barFlipper.setDisplayedChild(0);
                        clearSwitches();
                    }
                }
            }
        });

        cancelButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchEdit.setText("");
                breadActivity.barFlipper.setDisplayedChild(0);
                clearSwitches();
                if(null != listener)
                {
                    listener.onSearchBarFilterUpdate();
                }
                onShow(false);
            }
        });

        searchEdit.addTextChangedListener(new TextWatcher()
        {
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(null != listener)
                {
                    listener.onSearchBarFilterUpdate();
                }
            }
        });

        searchEdit.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    onShow(false);
                    return true;
                }
                return false;
            }
        });

        sentFilter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                filterSwitches[0] = !filterSwitches[0];
                filterSwitches[1] = false;
                updateFilterButtonsUI();

            }
        });

        receivedFilter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                filterSwitches[1] = !filterSwitches[1];
                filterSwitches[0] = false;
                updateFilterButtonsUI();
            }
        });

        pendingFilter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                filterSwitches[2] = !filterSwitches[2];
                filterSwitches[3] = false;
                updateFilterButtonsUI();
            }
        });

        completedFilter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                filterSwitches[3] = !filterSwitches[3];
                filterSwitches[2] = false;
                updateFilterButtonsUI();
            }
        });
    }

    public void clearSwitches()
    {
        filterSwitches[0] = false;
        filterSwitches[1] = false;
        filterSwitches[2] = false;
        filterSwitches[3] = false;
    }

    public void onShow(boolean b)
    {
        final InputMethodManager keyboard = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (b)
        {
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    searchEdit.requestFocus();
                    if(null != keyboard)
                    {
                        keyboard.showSoftInput(searchEdit, 0);
                    }
                }
            }, 400);
        }
        else
        {
            if(null != keyboard)
            {
                keyboard.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
            }
        }

        clearSwitches();
        updateFilterButtonsUI();
    }
}