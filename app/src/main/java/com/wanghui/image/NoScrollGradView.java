package com.wanghui.image;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by wanghui on 16-7-7.
 */
public class NoScrollGradView extends GridView {
    public NoScrollGradView(Context cxt, AttributeSet attris) {
        super(cxt, attris);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
