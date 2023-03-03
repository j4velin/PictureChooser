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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class ImagesFragment extends Fragment {

    private GalleryAdapter adapter;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) adapter.shutdown();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gallery, null);

        Cursor cur = getActivity().getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.Media.DATA,
                                MediaStore.Images.Media.DISPLAY_NAME},
                        MediaStore.Images.Media.BUCKET_ID + " = ?",
                        new String[]{String.valueOf(getArguments().getInt("bucket"))},
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC");

        final List<GridItem> images = new ArrayList<GridItem>(cur.getCount());

        if (cur != null) {
            if (cur.moveToFirst()) {
                while (!cur.isAfterLast()) {
                    if (cur.getString(0) != null)
                        images.add(new GridItem(cur.getString(1), cur.getString(0)));
                    cur.moveToNext();
                }
            }
            cur.close();
        }

        GridView grid = (GridView) v.findViewById(R.id.grid);
        adapter = new GalleryAdapter(getActivity(), images);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((Main) getActivity()).imageSelected(images.get(position).path);
            }
        });
        return v;
    }

}
