package de.j4velin.picturechooser;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class ImagesFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.gallery, null);

		Cursor cur = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME },
				MediaStore.Images.Media.BUCKET_ID + " = ?", new String[] { String.valueOf(getArguments().getInt("bucket")) },
				null);

		final List<GridItem> images = new ArrayList<GridItem>(cur.getCount());

		if (cur != null) {
			if (cur.moveToFirst()) {
				while (!cur.isAfterLast()) {
					images.add(new GridItem(cur.getString(1), cur.getString(0)));
					cur.moveToNext();
				}
			}
			cur.close();
		}

		GridView grid = (GridView) v.findViewById(R.id.grid);
		grid.setAdapter(new GalleryAdapter(getActivity(), images));
		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((Main) getActivity()).imageSelected(images.get(position).path);
			}
		});
		return v;
	}

}
