/*
 * from http://www.androidhive.info/2012/07/android-loading-image-from-url-http/
 */
package de.j4velin.picturechooser.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.os.Build;
import android.widget.ImageView;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.j4velin.picturechooser.BuildConfig;
import de.j4velin.picturechooser.Logger;
import de.j4velin.picturechooser.R;

public class ImageLoader {

    private final static int THUMBNAIL_SIZE_PX = 300;

    private static final MemoryCache memoryCache = new MemoryCache();
    private final Map<ImageView, String> imageViews =
            Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private final ExecutorService executorService;

    public ImageLoader() {
        executorService = Executors.newCachedThreadPool();
    }

    private static final int stub_id = R.drawable.ic_menu_gallery;

    public void displayImage(final String pfad, final ImageView imageView) {
        imageViews.put(imageView, pfad);
        Bitmap bitmap = memoryCache.get(pfad);
        if (bitmap != null) imageView.setImageBitmap(bitmap);
        else {
            queuePhoto(pfad, imageView);
            imageView.setImageResource(stub_id);
        }
    }

    private void queuePhoto(final String pfad, final ImageView imageView) {
        executorService.submit(new PhotosLoader(new PhotoToLoad(pfad, imageView)));
    }

    private static Bitmap decode(final String pfad) {
        return decode(pfad, THUMBNAIL_SIZE_PX, THUMBNAIL_SIZE_PX, null);
    }

    // decodes image and scales it to reduce memory consumption
    public static Bitmap decode(final String pfad, int width, int height, final float[] imgDetails) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(pfad, options);
        final int imgWidth = options.outWidth;
        final int imgHeight = options.outHeight;

        while (imgWidth > width * options.inSampleSize && imgHeight > height * options.inSampleSize)
            options.inSampleSize *= 2;

        if (imgDetails != null) {
            imgDetails[0] = imgWidth;
            imgDetails[1] = imgHeight;
            imgDetails[2] = options.inSampleSize;
        }

        options.inJustDecodeBounds = false;
        try {
            int rotation = Build.VERSION.SDK_INT >= 5 ? API5Wrapper.getOrientation(pfad) : 0;
            if (rotation > 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                if (rotation != 180 && imgDetails != null) {
                    imgDetails[0] = imgHeight;
                    imgDetails[1] = imgWidth;
                }
                if (BuildConfig.DEBUG) Logger.log("rotate image by " + rotation);
                Bitmap image = BitmapFactory.decodeFile(pfad, options);
                try {
                    return Bitmap.createBitmap(image, 0, 0, imgWidth / options.inSampleSize,
                            imgHeight / options.inSampleSize, matrix, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return BitmapFactory.decodeFile(pfad, options);
            }
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
            if (BuildConfig.DEBUG) {
                Logger.log("OOM when loading file " + pfad);
                Logger.log(oom);
            }
            return null;
        }
    }

    private boolean imageViewReused(final PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        return tag == null || !tag.equals(photoToLoad.pfad);
    }

    private class PhotosLoader implements Runnable {
        private final PhotoToLoad photoToLoad;

        PhotosLoader(final PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            if (imageViewReused(photoToLoad)) return;
            Bitmap bmp = decode(photoToLoad.pfad);
            memoryCache.put(photoToLoad.pfad, bmp);
            if (imageViewReused(photoToLoad)) return;
            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
            Activity a = (Activity) photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }

    private class PhotoToLoad {
        private final String pfad;
        private final ImageView imageView;

        PhotoToLoad(final String u, final ImageView i) {
            pfad = u;
            imageView = i;
        }
    }

    // Used to display bitmap in the UI thread
    private class BitmapDisplayer implements Runnable {
        private final Bitmap bitmap;
        private final PhotoToLoad photoToLoad;

        BitmapDisplayer(final Bitmap b, final PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad)) return;
            if (bitmap != null) {
                photoToLoad.imageView.setImageBitmap(bitmap);
            } else {
                photoToLoad.imageView.setImageResource(stub_id);
            }
        }
    }
}
