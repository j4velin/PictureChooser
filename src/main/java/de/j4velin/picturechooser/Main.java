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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.PermissionChecker;

import de.j4velin.picturechooser.crop.CropFragment;

public class Main extends FragmentActivity {

    private final static int REQUEST_READ_STORAGE_PERMISSION = 1;

    @Override
    protected void onCreate(final Bundle b) {
        super.onCreate(b);

        setResult(RESULT_CANCELED);

        if (Build.VERSION.SDK_INT >= 23 && PermissionChecker
                .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PermissionChecker.PERMISSION_GRANTED && PermissionChecker
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE_PERMISSION);
        } else {
            showBuckets();
        }
    }

    private void showBuckets() {
        // Create new fragment and transaction
        Fragment newFragment = new BucketsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this
        // fragment,
        // and add the transaction to the back stack
        transaction.replace(android.R.id.content, newFragment);

        // Commit the transaction
        try {
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    void showBucket(final int bucketId) {
        Bundle b = new Bundle();
        b.putInt("bucket", bucketId);
        Fragment f = new ImagesFragment();
        f.setArguments(b);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f)
                .addToBackStack(null).commit();
    }

    void imageSelected(final String imgPath) {
        if (getIntent().getBooleanExtra("crop", false)) {
            Bundle b = new Bundle();
            b.putString("imgPath", imgPath);
            b.putFloat("aspect", getIntent().getIntExtra("aspectX", 0) /
                    (float) getIntent().getIntExtra("aspectY", 1));
            Fragment f = new CropFragment();
            f.setArguments(b);
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f)
                    .addToBackStack(null).commit();
        } else {
            returnResult(imgPath);
        }
    }

    public void cropped(final String imgPath) {
        returnResult(imgPath);
    }

    private void returnResult(final String imgPath) {
        Intent result = new Intent();
        result.putExtra("imgPath", imgPath);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_READ_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showBuckets();
            } else {
                finish();
            }
        }
    }
}
