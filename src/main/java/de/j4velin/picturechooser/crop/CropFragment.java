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
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;

import de.j4velin.picturechooser.Logger;
import de.j4velin.picturechooser.Main;
import de.j4velin.picturechooser.R;
import de.j4velin.picturechooser.util.API16Wrapper;
import de.j4velin.picturechooser.util.API17Wrapper;
import de.j4velin.picturechooser.util.ImageLoader;

public class CropFragment extends Fragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.crop, null);

        DisplayMetrics metrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            API17Wrapper.getRealMetrics(
                    ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE))
                            .getDefaultDisplay(), metrics);
        } else {
            ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getMetrics(metrics);
        }

        int availableHeight = metrics.heightPixels;
        int availableWidth = metrics.widthPixels;

        if (Main.DEBUG) Logger.log("available space: " + availableWidth + "x" + availableHeight);

        final ImageView iv = (ImageView) v.findViewById(R.id.image);
        final float[] imageData = new float[3];

        iv.setImageBitmap(ImageLoader
                .decode(getArguments().getString(Main.IMAGE_PATH), availableWidth, availableHeight,
                        imageData));

        final CropView cv = (CropView) v.findViewById(R.id.crop);
        final RectF imagePosition = new RectF();

        iv.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= 16) {
                            API16Wrapper.removeTreeObserver(iv.getViewTreeObserver(), this);
                        } else {
                            //noinspection deprecation
                            iv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                        /**
                         * based on chteuchteu's solution on
                         * http://stackoverflow.com/questions/16193282/how-to-get-the-position-of-a-picture-inside-an-imageview
                         */
                        float[] f = new float[9];
                        iv.getImageMatrix().getValues(f);

                        int imageWidth = (int) (imageData[0] / imageData[2] * f[Matrix.MSCALE_X]);
                        int imageHeight = (int) (imageData[1] / imageData[2] * f[Matrix.MSCALE_Y]);

                        if (Main.DEBUG) {
                            Logger.log("imageData: " + imageData[0] + "," + imageData[1] + "," +
                                    imageData[2]);
                            Logger.log("Scaled image: " + imageWidth + "x" + imageHeight);
                            Logger.log("ImageView: " + iv.getWidth() + "x" + iv.getHeight());
                        }

                        imagePosition.left = (iv.getWidth() - imageWidth) / 2;
                        imagePosition.top = (iv.getHeight() - imageHeight) / 2;
                        imagePosition.right = imagePosition.left + imageWidth;
                        imagePosition.bottom = imagePosition.top + imageHeight;

                        if (Main.DEBUG) Logger.log("Image position: " + imagePosition.toString());

                        cv.setImagePosition(imagePosition);
                        cv.setScale(imageData[0] / imagePosition.width());
                        cv.setAspect(getArguments().getFloat("aspect", 0));
                    }
                });

        v.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new SaveTask((Main) getActivity(), cv, imagePosition)
                        .execute(getArguments().getString(Main.IMAGE_PATH));
            }
        });
        return v;
    }
}
