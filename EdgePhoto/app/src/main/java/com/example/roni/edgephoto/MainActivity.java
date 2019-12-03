package com.example.roni.edgephoto;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


class MyApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}


public class MainActivity extends AppCompatActivity {

    private SeekBar mBar;
    private TextView mTextView;
    private ImageView mPhotoCapturedImageView;
    private static final int ACTIVITY_START_CAMERA_APP = 0;
    private String mImageFileLocation = "";
    private int luku = 3;
    private int STORAGE_PERMISSIONS_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBar = findViewById(R.id.seekBar);
        mBar.setMax(20);


        mTextView = findViewById(R.id.TextView);
        mTextView.setTextSize(20);
        mTextView.setText("" + luku);

        mBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                luku = i;
                mTextView.setText("" + luku);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        LayoutInflater controlInflater = LayoutInflater.from(getBaseContext());
//        View viewControl = controlInflater.inflate(R.layout.activity_main, null);
//        mPhotoCapturedImageView = viewControl.findViewById(R.id.capturePhotoImageView);

        mPhotoCapturedImageView = findViewById(R.id.capturePhotoImageView);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void takePhoto(View view) {
        Intent callCameraApplicationIntent = new Intent();
        callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri temp = android.net.Uri.parse(photoFile.toURI().toString());

        callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, temp);

        startActivityForResult(callCameraApplicationIntent, ACTIVITY_START_CAMERA_APP);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
            Bitmap myBitmap = setReducedImageSize();

            try {
                ExifInterface exif = new ExifInterface(mImageFileLocation);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
            } catch (Exception e) {
            }
            Bitmap suodatettu = suodata(myBitmap);
            mPhotoCapturedImageView.setImageBitmap(suodatettu);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "IMAGE_" + timeStamp + "_";
//            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                saveImage(suodatettu, imageFileName);
//            } else {//               requestStoragePermission();
//            }


            //Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(mImageFileLocation);
            //mPhotoCapturedImageView.setImageBitmap(photoCapturedBitmap);
        }
    }
    public void savePhoto(View view) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Need permission to save the altered image!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_CODE);
        }
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Need permission to save the altered image!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSIONS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";


        File outputDir = getApplicationContext().getExternalCacheDir(); // context being the Activity pointer
        File outputFile = File.createTempFile(imageFileName, ".jpg", outputDir);
        mImageFileLocation = outputFile.getAbsolutePath();
        return outputFile;
    }

    Bitmap setReducedImageSize() {
        int targetImageViewWidth = mPhotoCapturedImageView.getWidth();
        int targetImageViewHeight = mPhotoCapturedImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageFileLocation, bmOptions);
        int cameraImageWidth = bmOptions.outWidth;
        int cameraImageHeight = bmOptions.outHeight;

        int scaleFactor = Math.min(cameraImageWidth / targetImageViewWidth, cameraImageHeight / targetImageViewHeight);
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inJustDecodeBounds = false;
        Bitmap photoReducedSizeBitmap = BitmapFactory.decodeFile(mImageFileLocation, bmOptions);
        return photoReducedSizeBitmap;
    }


    Bitmap suodata(Bitmap kuva) {
        Bitmap suodatettu = Bitmap.createBitmap(kuva.getWidth(), kuva.getHeight(), kuva.getConfig());

        byte[][][] rgb = new byte[kuva.getWidth()][kuva.getHeight()][4];

        int[] vaaka = {-1, 0, 1, -1, 0, 1, -1, 0, 1};
        int[] pysty = {1, 1, 1, 0, 0, 0, -1, -1, -1};
        Vektori vaakav = new Vektori(vaaka);
        Vektori pystyv = new Vektori(pysty);

        int x, y = 1;
        while (y < kuva.getHeight() - 1) {
            x = 1;
            while (x < kuva.getWidth() - 1) {

                rgb[x][y] = ByteBuffer.allocate(4).putInt(kuva.getPixel(x, y)).array();
                x++;

            }
            y++;
        }

        y = 1;
        while (y < kuva.getHeight() - 1) {

            x = 1;
            while (x < kuva.getWidth() - 1) {


                int[] r = {rgb[x - 1][y - 1][1],
                        rgb[x][y - 1][1],
                        rgb[x + 1][y - 1][1],
                        rgb[x - 1][y][1],
                        rgb[x][y][1],
                        rgb[x + 1][y][1],
                        rgb[x - 1][y + 1][1],
                        rgb[x][y + 1][1],
                        rgb[x + 1][y + 1][1]};

                int[] g = {rgb[x - 1][y - 1][2],
                        rgb[x][y - 1][2],
                        rgb[x + 1][y - 1][2],
                        rgb[x - 1][y][2],
                        rgb[x][y][2],
                        rgb[x + 1][y][2],
                        rgb[x - 1][y + 1][2],
                        rgb[x][y + 1][2],
                        rgb[x + 1][y + 1][2]};

                int[] b = {rgb[x - 1][y - 1][3],
                        rgb[x][y - 1][3],
                        rgb[x + 1][y - 1][3],
                        rgb[x - 1][y][3],
                        rgb[x][y][3],
                        rgb[x + 1][y][3],
                        rgb[x - 1][y + 1][3],
                        rgb[x][y + 1][3],
                        rgb[x + 1][y + 1][3]};

                Vektori rv = new Vektori(r);
                Vektori gv = new Vektori(g);
                Vektori bv = new Vektori(b);


                int temp = Math.abs((rv.sisatulo(vaakav)) + (rv.sisatulo(pystyv)) +
                        (gv.sisatulo(vaakav)) + (gv.sisatulo(pystyv)) +
                        (bv.sisatulo(vaakav)) + (bv.sisatulo(pystyv))) / 18;
                if (temp > luku) {
                    suodatettu.setPixel(x, y, 0xff000000);
                }

                x++;
            }
            y++;
        }
        return suodatettu;
    }

    private void saveImage(Bitmap finalBitmap, String image_name) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = image_name+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
