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

public class BucketsFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.gallery, null);

		String[] projection = new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
				MediaStore.Images.Media.BUCKET_ID };

		Cursor cur = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
				null, MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

		final List<GridItem> buckets = new ArrayList<GridItem>();
		BucketItem lastBucket = null;

		if (cur != null) {
			if (cur.moveToFirst()) {
				while (!cur.isAfterLast()) {
					if (lastBucket == null || !lastBucket.name.equals(cur.getString(1))) {
						lastBucket = new BucketItem(cur.getString(1), cur.getString(0), cur.getInt(2));
						buckets.add(lastBucket);
					} else {
						lastBucket.images++;
					}
					cur.moveToNext();
				}
			}
			cur.close();
		}

		GridView grid = (GridView) v.findViewById(R.id.grid);
		grid.setAdapter(new GalleryAdapter(getActivity(), buckets));
		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((Main) getActivity()).showBucket(((BucketItem)buckets.get(position)).id);
			}
		});
		return v;
	}

}
