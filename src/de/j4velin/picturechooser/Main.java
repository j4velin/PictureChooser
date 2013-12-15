package de.j4velin.picturechooser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

public class Main extends Activity {
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(final Bundle b) {
		super.onCreate(b);

		setResult(RESULT_CANCELED);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			setTheme(android.R.style.Theme_Holo_NoActionBar_TranslucentDecor);
		} else {
			setTheme(android.R.style.Theme_Holo_NoActionBar);
		}

		// Create new fragment and transaction
		Fragment newFragment = new BucketsFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();

		// Replace whatever is in the fragment_container view with this
		// fragment,
		// and add the transaction to the back stack
		transaction.replace(android.R.id.content, newFragment);

		// Commit the transaction
		transaction.commit();
	}

	void showBucket(final int bucketId) {
		Bundle b = new Bundle();
		b.putInt("bucket", bucketId);
		Fragment f = new ImagesFragment();
		f.setArguments(b);
		getFragmentManager().beginTransaction().replace(android.R.id.content, f).addToBackStack(null).commit();
	}

	void imageSelected(final String imgPath) {
		Intent result = new Intent();
		result.putExtra("imgPath", imgPath);
		setResult(RESULT_OK, result);
		finish();
	}
}
