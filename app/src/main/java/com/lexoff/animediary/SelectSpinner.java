package com.lexoff.animediary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

//https://stackoverflow.com/a/24679564
public class SelectSpinner extends Spinner {

    public interface OnSelectSpinnerListener {

        public void onOpenSelectMenu();

        public void onItemSelectByUser(AdapterView<?> parent, View view,
                                       int position, long id);
    }

    private boolean isUserOpen = false;

    private List<OnSelectSpinnerListener> onSelectSpinnerListeners;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;

    public SelectSpinner(Context context) {
        super(context);
        registerEvents();
    }

    public SelectSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        registerEvents();
    }

    public SelectSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        registerEvents();
    }

    /**
     * Register default events
     * */
    private void registerEvents() {
        // overrides the default event for android OnItemSelectedListener
        super.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (onItemSelectedListener != null) {
                    // call onItemSelected default event in android
                    onItemSelectedListener.onItemSelected(parent, view,
                            position, id);
                }
                // checks if the spinner selection is opened by the user and has event listener
                if (isUserOpen && onSelectSpinnerListeners != null) {
                    for (OnSelectSpinnerListener listener : onSelectSpinnerListeners) {
                        listener.onItemSelectByUser(parent, view, position, id);
                    }
                    isUserOpen = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (onItemSelectedListener != null) {
                    // call onNothingSelected default event in android
                    onItemSelectedListener.onNothingSelected(parent);
                }
            }
        });
    }

    public void addOnSelectSpinnerListener(
            OnSelectSpinnerListener onSelectSpinnerListener) {
        if (this.onSelectSpinnerListeners == null) {
            this.onSelectSpinnerListeners = new ArrayList<OnSelectSpinnerListener>();
        }
        this.onSelectSpinnerListeners.add(onSelectSpinnerListener);
    }

    @Override
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    @Override
    public boolean performClick() {
        // isUserOpen variable set to true, to indicate that there was user interaction
        isUserOpen = true;
        if (this.onSelectSpinnerListeners != null) {
            for (OnSelectSpinnerListener listener : this.onSelectSpinnerListeners) {
                // call onOpenSelectMenu event
                listener.onOpenSelectMenu();
            }
        }
        return super.performClick();
    }
}
