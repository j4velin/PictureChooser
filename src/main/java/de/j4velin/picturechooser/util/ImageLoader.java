/*
 * from http://www.androidhive.info/2012/07/android-loading-image-from-url-http/
 */
package de.j4velin.picturechooser.util;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.j4velin.picturechooser.BuildConfig;
import de.j4velin.picturechooser.Logger;
import de.j4velin.picturechooser.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.widget.ImageView;

public class ImageLoader {

    private final static int THUMBNAIL_SIZE_PX = 300;

	private static MemoryCache memoryCache = new MemoryCache();
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	private ExecutorService executorService;

	public ImageLoader(Context context) {
		executorService = Executors.newCachedThreadPool();
	}

	private static final int stub_id = R.drawable.ic_menu_gallery;

	public void DisplayImage(final String pfad, final ImageView imageView) {
		imageViews.put(imageView, pfad);
		Bitmap bitmap = memoryCache.get(pfad);
		if (bitmap != null)
			imageView.setImageBitmap(bitmap);
		else {
			queuePhoto(pfad, imageView);
			imageView.setImageResource(stub_id);
		}
	}

	private void queuePhoto(final String pfad, final ImageView imageView) {
		executorService.submit(new PhotosLoader(new PhotoToLoad(pfad, imageView)));
	}

	// decodes image and scales it to reduce memory consumption
	static Bitmap decode(final String pfad, int width, int height) {
		Options options = new Options();
		options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
		BitmapFactory.decodeFile(pfad, options);
		int imgWidth = options.outWidth;
        int imgHeight = options.outHeight;

        while (imgWidth > width * options.inSampleSize && imgHeight > height * options.inSampleSize)
            options.inSampleSize *= 2;

		options.inJustDecodeBounds = false;
		try {
			return BitmapFactory.decodeFile(pfad, options);
		} catch (OutOfMemoryError oom) {
			if (BuildConfig.DEBUG) {
				Logger.log("OOM when loading file " + pfad);
				Logger.log(oom);
			}
			return null;
		}
	}

	private boolean imageViewReused(final PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.pfad))
			return true;
		return false;
	}

	private class PhotosLoader implements Runnable {
		private final PhotoToLoad photoToLoad;

		PhotosLoader(final PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = decode(photoToLoad.pfad, THUMBNAIL_SIZE_PX, THUMBNAIL_SIZE_PX);
			memoryCache.put(photoToLoad.pfad, bmp);
			if (imageViewReused(photoToLoad))
				return;
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
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null) {
				photoToLoad.imageView.setImageBitmap(bitmap);
			} else {
				photoToLoad.imageView.setImageResource(stub_id);
			}
		}
	}

}
