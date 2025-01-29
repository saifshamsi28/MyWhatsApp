package com.saif.mywhatsapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

public class ClickablePhotoView extends PhotoView {

    public ClickablePhotoView(Context context) {
        super(context);
    }

    public ClickablePhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickablePhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        super.performClick();
//        Toast.makeText(getContext(), "touching", Toast.LENGTH_SHORT).show();
        return true;
    }
}
