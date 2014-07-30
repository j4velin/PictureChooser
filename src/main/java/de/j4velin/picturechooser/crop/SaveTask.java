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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.j4velin.picturechooser.Main;
import de.j4velin.picturechooser.R;
import de.j4velin.picturechooser.util.ExternalDirWrapper;
import de.j4velin.picturechooser.util.ImageLoader;

class SaveTask extends AsyncTask<String, Void, String> {

    private final ProgressDialog pg;
    private final CropView cv;
    private final RectF imagePosition;
    private final Main main;

    SaveTask(final Main main, final CropView cv, final RectF imagePosition) {
        pg = new ProgressDialog(main);
        this.cv = cv;
        this.imagePosition = imagePosition;
        this.main = main;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pg.setMessage(main.getString(R.string.saving));
        pg.setCancelable(false);
        pg.show();
    }

    @Override
    protected void onPostExecute(final String error) {
        super.onPostExecute(error);
        pg.dismiss();
        if (error != null) {
            new AlertDialog.Builder(main).setMessage(error).setTitle(R.string.error).create().show();
        }
    }

    @Override
    protected String doInBackground(final String... original) {
        return save(original[0], calculateSize(cv.getCropArea(), imagePosition, cv.getScale()));
    }

    /**
     * Saves the cropped image.
     *
     * @param source the source image
     * @param crop   the calculated size of the cropped image
     * @return null or a string containing information about an error
     */
    private String save(final String source, final Rect crop) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return main.getString(R.string.external_not_available);
        }
        final String destination;
        try {
            destination = createNewCroppingFile();
        } catch (IOException e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        FileOutputStream out = null;
        try {
            // might be an issue if an image is more than 6000px --> ignore that for now
            final Bitmap original = ImageLoader.decode(source, 3000, 3000, null);
            out = new FileOutputStream(destination);
            Bitmap.createBitmap(original, crop.left, crop.top, crop.right, crop.bottom, null, true)
                    .compress(Bitmap.CompressFormat.JPEG, 100, out);
            main.cropped(destination);
        } catch (Throwable e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        } finally {
            try {
                out.close();
            } catch (Throwable ignore) {
            }
        }
        return null;
    }

    /**
     * Calculates the resulting size of the cropped image
     *
     * @param cropArea the selected cropping area
     * @param image    the area of the imageView
     * @param scale    the scale between original image size and imageview size
     * @return the size of the cropped image
     */

    private static Rect calculateSize(final RectF cropArea, final RectF image, float scale) {
        return new Rect((int) ((cropArea.left - image.left) * scale),
                (int) ((cropArea.top - image.top) * scale), (int) (cropArea.width() * scale),
                (int) (cropArea.height() * scale));
    }

    /**
     * Creates the file which will contain the cropped image
     *
     * @return the absolute path to the created file or NULL if there was an error
     */
    private String createNewCroppingFile() throws IOException {
        File tmpFile;
        int test = 0;
        String path;
        try {
            path = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO ?
                    ExternalDirWrapper.getExternalFilesDir(main).getAbsolutePath() + "/image_" :
                    Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/Android/data/" + main.getPackageName() + "/files/image_";
        } catch (NullPointerException e) { // should not happen when media
            // is mounted, but seems to
            // happen anyway
            return null;
        }
        do {
            test++;
            tmpFile = new File(path + test + ".jpg");
        } while (tmpFile.length() > 0);
        File parent = tmpFile.getParentFile();
        if (parent == null || !parent.exists()) {
            tmpFile.mkdirs();
        }
        tmpFile.createNewFile();
        return tmpFile.getAbsolutePath();
    }
}
