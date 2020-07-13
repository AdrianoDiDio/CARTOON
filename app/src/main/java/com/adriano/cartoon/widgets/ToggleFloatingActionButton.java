package com.adriano.cartoon.widgets;

import android.content.Context;
import android.graphics.Outline;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Checkable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/*
 * Adriano Di Dio 2020
 * An extension to the default FloatingActionButton that implements the Checkable interface.
 * It also handles state changes by saving/restoring the checked value on configuration changes.
 * Part of this code is taken from the sample found on github:
 * https://github.com/googlearchive/android-FloatingActionButtonBasic
 *
 * */
public class ToggleFloatingActionButton extends FloatingActionButton implements Checkable {

    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };
    private static final String TOGGLE_FLOATING_ACTION_BUTTON_SUPER_STATE_BUNDLE_KEY = "SuperState";
    private static final String TOGGLE_FLOATING_ACTION_BUTTON_CHECKED_BUNDLE_KEY = "Checked";
    private boolean checked;
    private OnCheckedChangeListener onCheckedChangeListener;

    public ToggleFloatingActionButton(Context context) {
        super(context);
        init();
    }

    public ToggleFloatingActionButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public ToggleFloatingActionButton(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        init();
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.checked == checked) {
            return;
        }
        this.checked = checked;
        refreshDrawableState();
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChange(this, checked);
        }
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(TOGGLE_FLOATING_ACTION_BUTTON_SUPER_STATE_BUNDLE_KEY, super.onSaveInstanceState());
        bundle.putBoolean(TOGGLE_FLOATING_ACTION_BUTTON_CHECKED_BUNDLE_KEY, checked);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable(TOGGLE_FLOATING_ACTION_BUTTON_SUPER_STATE_BUNDLE_KEY);
            setChecked(bundle.getBoolean(TOGGLE_FLOATING_ACTION_BUTTON_CHECKED_BUNDLE_KEY));
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        invalidateOutline();
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    private void init() {
        setClickable(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, getWidth(), getHeight());
            }
        });
        setClipToOutline(true);
    }
}
