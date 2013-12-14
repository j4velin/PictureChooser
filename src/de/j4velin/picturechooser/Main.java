package de.j4velin.picturechooser;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

public class Main extends Activity {
	@Override
	protected void onCreate(final Bundle b) {
		super.onCreate(b);

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
