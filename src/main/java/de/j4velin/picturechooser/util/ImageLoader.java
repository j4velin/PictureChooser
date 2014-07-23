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

	MemoryCache memoryCache = new MemoryCache();
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	private ExecutorService executorService;

	public ImageLoader(Context context) {
		executorService = Executors.newCachedThreadPool();
	}

	final int stub_id = R.drawable.ic_menu_gallery;

	public void DisplayImage(String pfad, ImageView imageView) {
		imageViews.put(imageView, pfad);
		Bitmap bitmap = memoryCache.get(pfad);
		if (bitmap != null)
			imageView.setImageBitmap(bitmap);
		else {
			queuePhoto(pfad, imageView);
			imageView.setImageResource(stub_id);
		}
	}

	private void queuePhoto(String pfad, ImageView imageView) {
		executorService.submit(new PhotosLoader(new PhotoToLoad(pfad, imageView)));
	}

	// decodes image and scales it to reduce memory consumption
	Bitmap decode(String pfad) {
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pfad, options);
		int width = options.outWidth;
		if (width < 300) {
			options.inSampleSize = 1;
		} else if (width < 600) {
			options.inSampleSize = 2;
		} else if (width < 1200) {
			options.inSampleSize = 4;
		} else if (width < 2400) {
			options.inSampleSize = 8;
		} else {
			options.inSampleSize = (int) java.lang.Math.floor(width / (float) 300);
		}
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

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.pfad))
			return true;
		return false;
	}

	public void clearCache() {
		memoryCache.clear();
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = decode(photoToLoad.pfad);
			memoryCache.put(photoToLoad.pfad, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	private class PhotoToLoad {
		String pfad;
		ImageView imageView;

		PhotoToLoad(String u, ImageView i) {
			pfad = u;
			imageView = i;
		}
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		BitmapDisplayer(Bitmap b, PhotoToLoad p) {
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
