package de.j4velin.picturechooser.util;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public abstract class API12Wrapper {

    public static int getByteCount(final Bitmap b) {
        return b.getByteCount();
    }
}
