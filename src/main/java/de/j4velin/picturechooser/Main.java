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
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.PermissionChecker;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.j4velin.picturechooser.crop.CropFragment;

public class Main extends FragmentActivity {

    public final static String EXTRA_CROP = "crop";

    public final static String ASPECT_X = "aspectX";
    public final static String ASPECT_Y = "aspectY";

    public final static String IMAGE_PATH = "imgPath";

    public final static boolean DEBUG = false;

    private final static int REQUEST_STORAGE_PERMISSION = 1;
    private final static int REQUEST_IMAGE = 2;

    @Override
    protected void onCreate(final Bundle b) {
        super.onCreate(b);
        setResult(RESULT_CANCELED);
        checkPermission(true);
    }

    private void checkPermission(boolean askForPermission) {
        if (Build.VERSION.SDK_INT >= 23 && PermissionChecker
                .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PermissionChecker.PERMISSION_GRANTED && PermissionChecker
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PermissionChecker.PERMISSION_GRANTED) {
            if (askForPermission) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat
                        .shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                } else {
                    new AlertDialog.Builder(this).setMessage(R.string.permission)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface,
                                                            int i) {
                                            Intent intent = new Intent(
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getPackageName(),
                                                    null);
                                            intent.setData(uri);
                                            startActivityForResult(intent,
                                                    REQUEST_STORAGE_PERMISSION);
                                            dialogInterface.dismiss();
                                        }
                                    }).setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            finish();
                        }
                    }).create().show();
                }
            } else {
                finish();
            }
        } else {
            start();
        }
    }

    private void start() {
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
                        REQUEST_IMAGE);
            } catch (ActivityNotFoundException e) {
                showInternalPictureChooser();
            }
        } else {
            showInternalPictureChooser();
        }
    }

    private void showInternalPictureChooser() {
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
        if (getIntent().getBooleanExtra(EXTRA_CROP, false)) {
            Bundle b = new Bundle();
            b.putString(IMAGE_PATH, imgPath);
            b.putFloat("aspect", getIntent().getIntExtra(ASPECT_X, 0) /
                    (float) getIntent().getIntExtra(ASPECT_Y, 1));
            Fragment f = new CropFragment();
            f.setArguments(b);
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f)
                    .addToBackStack(null).commitAllowingStateLoss();
        } else {
            returnResult(imgPath);
        }
    }

    public void cropped(final String imgPath) {
        if (Main.DEBUG) Logger.log("Cropped file created: " + imgPath);
        returnResult(imgPath);
    }

    private void returnResult(final String imgPath) {
        Intent result = new Intent();
        result.setData(Uri.fromFile(new File((imgPath))));
        result.putExtra(IMAGE_PATH, imgPath);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            checkPermission(false);
        } else if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                if (Main.DEBUG) Logger.log("onActivityResult data=" + uri);
                File f = new File(uri.getPath());
                if (f.exists() && f.canRead()) {
                    // locally available file, not need to copy
                    imageSelected(f.getAbsolutePath());
                    return;
                }
                InputStream input = null;
                OutputStream output = null;
                try {
                    String imageName = uri.getLastPathSegment();
                    if (imageName == null) {
                        imageName = "image";
                    } else if (imageName.contains("/")) {
                        imageName = imageName.substring(imageName.lastIndexOf("/"));
                    }
                    input = getContentResolver().openInputStream(uri);
                    String extension = MimeTypeMap.getSingleton()
                            .getExtensionFromMimeType(getContentResolver().getType(uri));
                    if (extension == null) extension = "jpg";
                    int pos = 0;
                    do {
                        f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                .getAbsolutePath(), imageName + "_" + pos + "." + extension);
                        pos++;
                    } while (f.exists());

                    try {
                        if (!f.createNewFile())
                            throw new IOException(f.getAbsolutePath() + " can not be created");
                    } catch (Exception e) {
                        pos = 0;
                        do {
                            f = new File(getFilesDir(),
                                    uri.getLastPathSegment() + "_" + pos + "." + extension);
                            pos++;
                        } while (f.exists());
                        if (!f.createNewFile())
                            throw new IOException(f.getAbsolutePath() + " can not be created");
                    }

                    output = new FileOutputStream(f);

                    byte[] buffer = new byte[4096];
                    int read;

                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();

                    if (Main.DEBUG) Logger.log("File created: " + f.getAbsolutePath());

                    imageSelected(f.getPath());

                } catch (Exception e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    finish();
                } finally {
                    try {
                        input.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        output.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT >= 19) {
            start();
        }
    }
}
