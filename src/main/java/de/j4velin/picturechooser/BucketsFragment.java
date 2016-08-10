/*
 * Copyright 2013 Thomas Hoffmann
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
package de.j4velin.picturechooser;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BucketsFragment extends Fragment {

    private GalleryAdapter adapter;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) adapter.shutdown();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gallery, null);

        String[] projection = new String[]{MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};

        final List<GridItem> buckets = new ArrayList<GridItem>();
        BucketItem lastBucket = null;

        Cursor cur = getActivity().getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC, " +
                                MediaStore.Images.Media.DATE_MODIFIED + " DESC");

        if (cur != null) {
            if (cur.moveToFirst()) {
                while (!cur.isAfterLast()) {
                    if (cur.getString(1) != null) {
                        if (lastBucket == null || lastBucket.id != cur.getInt(2)) {
                            try {
                                lastBucket = new BucketItem(cur.getString(1), cur.getString(0),
                                        cur.getInt(2));
                                buckets.add(lastBucket);
                            } catch (IllegalArgumentException iae) {
                                iae.printStackTrace();
                            }
                        } else {
                            lastBucket.images++;
                        }
                    }
                    cur.moveToNext();
                }
            }
            cur.close();
        }

        cur = getActivity().getContentResolver()
                .query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, projection, null, null,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC, " +
                                MediaStore.Images.Media.DATE_MODIFIED + " DESC");

        if (cur != null) {
            if (cur.moveToFirst()) {
                while (!cur.isAfterLast()) {
                    if (cur.getString(1) != null) {
                        if (lastBucket == null || !lastBucket.name.equals(cur.getString(1))) {
                            try {
                                lastBucket = new BucketItem(cur.getString(1), cur.getString(0),
                                        cur.getInt(2));
                                buckets.add(lastBucket);
                            } catch (IllegalArgumentException iae) {
                                iae.printStackTrace();
                            }
                        } else {
                            lastBucket.images++;
                        }
                    }
                    cur.moveToNext();
                }
            }
            cur.close();
        }

        if (buckets.isEmpty()) {
            Toast.makeText(getActivity(), R.string.no_images, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        } else {
            GridView grid = (GridView) v.findViewById(R.id.grid);
            adapter = new GalleryAdapter(getActivity(), buckets);
            grid.setAdapter(adapter);
            grid.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ((Main) getActivity()).showBucket(((BucketItem) buckets.get(position)).id);
                }
            });
        }
        return v;
    }

}
