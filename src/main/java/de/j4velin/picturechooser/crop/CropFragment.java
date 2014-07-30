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
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import de.j4velin.picturechooser.Main;
import de.j4velin.picturechooser.R;
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
                new SaveTask((Main) getActivity(), cv, imagePosition)
                        .execute(getArguments().getString("imgPath"));
            }
        });
        return v;
    }
}
