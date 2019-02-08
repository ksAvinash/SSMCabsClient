package com.labs.ssmcabs.client.fonts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class DecurionOutlineFont extends TextView {

    public DecurionOutlineFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public DecurionOutlineFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DecurionOutlineFont(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/Decurion Outline.otf" );
        setTypeface(tf);
    }

}
