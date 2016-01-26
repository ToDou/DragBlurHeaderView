package com.loopeer.android.librarys.dragblurheader;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

public class DefaultHeader extends TextView implements Header {

    public DefaultHeader(Context context) {
        this(context, null);
    }

    public DefaultHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setGravity(Gravity.CENTER);
        setPadding(0, 40, 0, 40);
        setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_bright));
    }


    @Override
    public void onMoveStart(int currentPosY, int startSwitchOffset, CharSequence string) {
        setText(string);
    }

    @Override
    public void onCanStartSwitch(int currentPosY, int startSwitchOffset, CharSequence string) {
        setText(string);
    }
}
