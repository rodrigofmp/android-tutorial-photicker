package com.devmasterteam.photicker.utils;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.devmasterteam.photicker.R;
import com.devmasterteam.photicker.views.MainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SocialUtil {
    private static final String HASHTAG = "#photickerapp";

    public static void shareImageOnInsta(MainActivity mainActivity, RelativeLayout mRelativePhotoContent, View view) {
    }

    public static void shareImageOnFace(MainActivity mainActivity, RelativeLayout mRelativePhotoContent, View view) {
    }

    public static void shareImageTwitter(MainActivity mainActivity, RelativeLayout mRelativePhotoContent, View view) {
    }

    public static void shareImageWhats(MainActivity mainActivity, RelativeLayout mRelativePhotoContent, View view) {
        PackageManager pkManager = mainActivity.getPackageManager();

        try {
            pkManager.getPackageInfo("com.whatsapp", 0);

            String fileName = "temp_file" + System.currentTimeMillis() + ".jpg";

            try {
                mRelativePhotoContent.setDrawingCacheEnabled(true);
                mRelativePhotoContent.buildDrawingCache(true);

                File imageFile = new File(Environment.getExternalStorageDirectory(), fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(imageFile);

                mRelativePhotoContent.getDrawingCache(true).compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

                fileOutputStream.close();

                mRelativePhotoContent.setDrawingCacheEnabled(false);
                mRelativePhotoContent.destroyDrawingCache();

                try {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, HASHTAG);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:////sdcard/" + fileName));
                    sendIntent.setType("image/jpeg");
                    sendIntent.setPackage("com.whatsapp");

                    view.getContext().startActivity(Intent.createChooser(sendIntent, mainActivity.getString(R.string.share_image)));

                } catch (Exception e) {
                    Toast.makeText(mainActivity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
                }

            } catch (FileNotFoundException e) {
                Toast.makeText(mainActivity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(mainActivity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
            }

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(mainActivity, R.string.whatsapp_not_installed, Toast.LENGTH_SHORT).show();
        }
    }
}
