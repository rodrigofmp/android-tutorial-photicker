package com.devmasterteam.photicker.utils;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.devmasterteam.photicker.R;
import com.devmasterteam.photicker.views.MainActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageUtil {
    public static List<Integer> getImagesList() {
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.st_facial_0);
        images.add(R.drawable.st_facial_1);
        images.add(R.drawable.st_facial_3);
        images.add(R.drawable.st_facial_4);
        images.add(R.drawable.st_facial_5);
        images.add(R.drawable.st_facial_6);
        images.add(R.drawable.st_facial_7);
        images.add(R.drawable.st_facial_8);
        images.add(R.drawable.st_facial_10);
        images.add(R.drawable.st_facial_11);
        images.add(R.drawable.st_facial_12);
        images.add(R.drawable.st_facial_13);
        images.add(R.drawable.st_facial_14);
        images.add(R.drawable.st_comida_1);
        images.add(R.drawable.st_comida_2);
        images.add(R.drawable.st_comida_3);
        images.add(R.drawable.st_comida_5);
        images.add(R.drawable.st_comida_6);
        images.add(R.drawable.st_coracao_1);
        images.add(R.drawable.st_coracao_2);
        images.add(R.drawable.st_coracao_3);
        images.add(R.drawable.st_coracao_4);
        images.add(R.drawable.st_coracao_5);
        images.add(R.drawable.st_coracao_6);
        images.add(R.drawable.st_drink_1);
        images.add(R.drawable.st_drink_2);
        images.add(R.drawable.st_drink_3);
        images.add(R.drawable.st_drink_4);
        images.add(R.drawable.st_drink_5);
        images.add(R.drawable.st_drink_6);
        images.add(R.drawable.st_drink_7);
        images.add(R.drawable.st_drink_8);
        images.add(R.drawable.st_drink_9);
        images.add(R.drawable.st_drink_10);
        images.add(R.drawable.st_tatto_1);
        images.add(R.drawable.st_tatto_2);
        images.add(R.drawable.st_tatto_3);
        images.add(R.drawable.st_tatto_4);
        images.add(R.drawable.st_tatto_5);
        images.add(R.drawable.st_tatto_6);
        images.add(R.drawable.st_sticker_2);
        images.add(R.drawable.st_sticker_5);
        images.add(R.drawable.st_sticker_6);
        images.add(R.drawable.st_sticker_7);
        images.add(R.drawable.st_sticker_8);
        images.add(R.drawable.st_sticker_9);
        images.add(R.drawable.st_sticker_11);
        return images;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static void handleZoomIn(ImageView mImageSelected) {
        if (mImageSelected.getWidth() > 800)
            return;

        ViewGroup.LayoutParams params = mImageSelected.getLayoutParams();
        params.width = (int) (mImageSelected.getWidth() + (mImageSelected.getWidth() * 0.1));
        params.height = (int) (mImageSelected.getHeight() + (mImageSelected.getHeight() * 0.1));
        mImageSelected.setLayoutParams(params);
    }

    public static void handleZoomOut(ImageView mImageSelected) {
        if (mImageSelected.getWidth() < 50)
            return;

        ViewGroup.LayoutParams params = mImageSelected.getLayoutParams();
        params.width = (int) (mImageSelected.getWidth() - (mImageSelected.getWidth() * 0.1));
        params.height = (int) (mImageSelected.getHeight() - (mImageSelected.getHeight() * 0.1));
        mImageSelected.setLayoutParams(params);
    }

    public static void handleRotateLeft(ImageView mImageSelected) {
        mImageSelected.setRotation(mImageSelected.getRotation() - 5);
    }

    public static void handleRotateRight(ImageView mImageSelected) {
        mImageSelected.setRotation(mImageSelected.getRotation() + 5);
    }

    public static File createImageFile(Context context) throws IOException {
        String imageFileName = "photicker";
        File storeDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storeDir);
        return image;
    }

    public static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) {
        ExifInterface exifInterface;

        try {
            exifInterface = new ExifInterface(selectedImage.getPath());

            int orientation  = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(img, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(img, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(img, 270);
                default:
                    return img;
            }
        }
        catch(IOException ex) {
            return img;
        }

    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, false);

        img.recycle();

        return rotatedImg;
    }

    public static Bitmap drawBitmap(RelativeLayout mRelativePhotoContent) {
        mRelativePhotoContent.setDrawingCacheEnabled(true);
        return mRelativePhotoContent.getDrawingCache();
    }

}
