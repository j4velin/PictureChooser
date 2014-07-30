/*
 * Copyright 2014 Thomas Hoffmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.j4velin.picturechooser.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import de.j4velin.picturechooser.Logger;
import de.j4velin.picturechooser.Main;
import de.j4velin.picturechooser.R;
import de.j4velin.picturechooser.util.ExternalDirWrapper;
import de.j4velin.picturechooser.util.ImageLoader;

public class CropFragment extends Fragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.crop, null);

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(metrics);

        int availableHeight = metrics.heightPixels;
        int availableWidth = metrics.widthPixels;

        TypedValue tv = new TypedValue();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                availableHeight -= TypedValue
                        .complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) availableHeight -= getResources().getDimensionPixelSize(resourceId);

        final float[] imgDetails = new float[3];

        ImageView iv = (ImageView) v.findViewById(R.id.image);
        iv.setImageBitmap(ImageLoader
                .decode(getArguments().getString("imgPath"), availableWidth, availableHeight,
                        imgDetails));

        float imageViewWidth = imgDetails[0];
        float imageViewHeight = imgDetails[1];

        while (imageViewWidth > availableWidth || imageViewHeight > availableHeight) {
            imageViewWidth *= 0.99f;
            imageViewHeight *= 0.99f;
        }

        float spareWidth = availableWidth - imageViewWidth;
        float spareHeight = availableHeight - imageViewHeight;

        final CropView cv = (CropView) v.findViewById(R.id.crop);
        final RectF imagePosition = new RectF();
        imagePosition.left = spareWidth / 2;
        imagePosition.top = spareHeight / 2;
        imagePosition.right = imagePosition.left + imageViewWidth;
        imagePosition.bottom = imagePosition.top + imageViewHeight;
        cv.setImagePosition(imagePosition);
        cv.setScale(imgDetails[0] / imagePosition.width());

        v.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                save(calculateSize(cv.getCropArea(), imagePosition, cv.getScale()));
            }
        });
        return v;
    }

    /**
     * Calculates the resulting size of the cropped image
     *
     * @param cropArea the selected cropping area
     * @param image the area of the imageView
     * @param scale the scale between original image size and imageview size
     * @return the size of the cropped image
     */
    private static Rect calculateSize(final RectF cropArea, final RectF image, float scale) {
        return new Rect((int) ((cropArea.left - image.left) * scale),
                (int) ((cropArea.top - image.top) * scale), (int) (cropArea.width() * scale),
                (int) (cropArea.height() * scale));
    }

    /**
     * Saves the cropped image.
     *
     * @param crop the calculated size of the cropped image
     */
    private void save(final Rect crop) {
        final String destination = createNewCroppingFile();
        if (destination == null) return;
        FileOutputStream out = null;
        try {
            // might be an issue if an image is more than 6000px --> ignore that for now
            final Bitmap original =
                    ImageLoader.decode(getArguments().getString("imgPath"), 3000, 3000, null);
            out = new FileOutputStream(destination);
            Bitmap.createBitmap(original, crop.left, crop.top, crop.right, crop.bottom, null, true)
                    .compress(Bitmap.CompressFormat.JPEG, 100, out);
            ((Main) getActivity()).cropped(destination);
        } catch (Throwable e) {
            Logger.log(e);
            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                out.close();
            } catch (Throwable ignore) {
            }
        }
    }

    /**
     * Creates the file which will contain the cropped image
     *
     * @return the absolute path to the created file or NULL if there was an error
     */
    private String createNewCroppingFile() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File tmpFile;
            int test = 0;
            String path;
            try {
                path = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO ?
                        ExternalDirWrapper.getExternalFilesDir(getActivity()).getAbsolutePath() +
                                "/image_" :
                        Environment.getExternalStorageDirectory().getAbsolutePath() +
                                "/Android/data/" + getActivity().getPackageName() + "/files/image_";
            } catch (NullPointerException e) { // should not happen when media
                // is mounted, but seems to
                // happen anyway
                return null;
            }
            do {
                test++;
                tmpFile = new File(path + test + ".jpg");
            } while (tmpFile.length() > 0);
            try {
                File parent = tmpFile.getParentFile();
                if (parent == null || !parent.exists()) {
                    tmpFile.mkdirs();
                }
                tmpFile.createNewFile();
                return tmpFile.getAbsolutePath();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "Error: External storage not available",
                    Toast.LENGTH_LONG).show();

        }
        return null;
    }
}
