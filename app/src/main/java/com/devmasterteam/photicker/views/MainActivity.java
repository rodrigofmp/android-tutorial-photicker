package com.devmasterteam.photicker.views;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.devmasterteam.photicker.R;
import com.devmasterteam.photicker.utils.ImageUtil;
import com.devmasterteam.photicker.utils.LongEventType;
import com.devmasterteam.photicker.utils.PermissionUtil;
import com.devmasterteam.photicker.utils.SocialUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final String AUTHORITY = "com.devmasterteam.photicker.fileprovider";
    private File mFileForPhoto;

    private final ViewHolder mViewHolder = new ViewHolder();
    private ImageView mImageSelected;
    private boolean mAutoIncrement;
    private LongEventType mLongEventType;
    private Handler mRepeatUpdateHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        List<Integer> mListImages = ImageUtil.getImagesList();


        this.mViewHolder.mRelativePhotoContent = (RelativeLayout) this.findViewById(R.id.relative_photo_content_draw);
        final LinearLayout content = (LinearLayout) this.findViewById(R.id.linear_horizontal_scroll_content);

        for (Integer imageId : mListImages) {
            ImageView image = new ImageView(this);
            image.setImageBitmap(ImageUtil.decodeSampledBitmapFromResource(getResources(), imageId, 70, 70));
            image.setPadding(20, 10, 20, 10);

            BitmapFactory.Options dimensions = new BitmapFactory.Options();
            dimensions.inJustDecodeBounds = true; // Não usa alocação de memória para essa imagem
            BitmapFactory.decodeResource(getResources(), imageId, dimensions);

            final int width = dimensions.outWidth;
            final int height = dimensions.outHeight;

            image.setOnClickListener(onClickImageOption(this.mViewHolder.mRelativePhotoContent, imageId, width, height));

            content.addView(image);
        }

        this.mViewHolder.mLinearControlPanel = (LinearLayout) this.findViewById(R.id.linear_control_panel);
        this.mViewHolder.mLinearSharePanel = (LinearLayout) this.findViewById(R.id.linear_share_panel);
        this.mViewHolder.mButtonZoomIn = (ImageView) this.findViewById(R.id.image_zoom_in);
        this.mViewHolder.mButtonZoomOut = (ImageView) this.findViewById(R.id.image_zoom_out);
        this.mViewHolder.mButtonRotateLeft = (ImageView) this.findViewById(R.id.image_rotate_left);
        this.mViewHolder.mButtonRotateRight = (ImageView) this.findViewById(R.id.image_rotate_right);
        this.mViewHolder.mButtonFinish = (ImageView) this.findViewById(R.id.image_finish);
        this.mViewHolder.mButtonRemove = (ImageView) this.findViewById(R.id.image_remove);

        this.mViewHolder.mButtonTakePhoto = (ImageView) this.findViewById(R.id.image_take_photo);
        this.mViewHolder.mImagePhoto = (ImageView) this.findViewById(R.id.image_photo);
        this.mViewHolder.mImageInstagram = (ImageView) this.findViewById(R.id.image_instagram);
        this.mViewHolder.mImageFacebook = (ImageView) this.findViewById(R.id.image_facebook);
        this.mViewHolder.mImageWhatsapp = (ImageView) this.findViewById(R.id.image_whatsapp);
        this.mViewHolder.mImageTwitter = (ImageView) this.findViewById(R.id.image_twitter);

        this.setListeners();
    }

    private void setListeners() {
        mViewHolder.mButtonZoomIn.setOnClickListener(this);
        mViewHolder.mButtonZoomOut.setOnClickListener(this);
        mViewHolder.mButtonRotateLeft.setOnClickListener(this);
        mViewHolder.mButtonRotateRight.setOnClickListener(this);
        mViewHolder.mButtonFinish.setOnClickListener(this);
        mViewHolder.mButtonRemove.setOnClickListener(this);

        mViewHolder.mButtonZoomIn.setOnLongClickListener(this);
        mViewHolder.mButtonZoomOut.setOnLongClickListener(this);
        mViewHolder.mButtonRotateLeft.setOnLongClickListener(this);
        mViewHolder.mButtonRotateRight.setOnLongClickListener(this);

        mViewHolder.mButtonZoomIn.setOnTouchListener(this);
        mViewHolder.mButtonZoomOut.setOnTouchListener(this);
        mViewHolder.mButtonRotateLeft.setOnTouchListener(this);
        mViewHolder.mButtonRotateRight.setOnTouchListener(this);

        mViewHolder.mButtonTakePhoto.setOnClickListener(this);

        mViewHolder.mImageInstagram.setOnClickListener(this);
        mViewHolder.mImageFacebook.setOnClickListener(this);
        mViewHolder.mImageWhatsapp.setOnClickListener(this);
        mViewHolder.mImageTwitter.setOnClickListener(this);
    }

    private View.OnClickListener onClickImageOption(final RelativeLayout relativeLayout, final Integer imageId, int width, int height) {
        return new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View view) {
                final ImageView imageView = new ImageView(MainActivity.this);
                imageView.setBackgroundResource(imageId);
                relativeLayout.addView(imageView);

                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

                mImageSelected = imageView;

                toggleControlPanel(true);

                imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        float x, y;
                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                mImageSelected = imageView;
                                toggleControlPanel(true);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                int coords[] = {0, 0};
                                relativeLayout.getLocationOnScreen(coords);

                                x = (motionEvent.getRawX() - (imageView.getWidth() / 2));
                                y = motionEvent.getRawY() - ((coords[1] + 100) + (imageView.getHeight() / 2));

                                imageView.setX(x);
                                imageView.setY(y);
                                break;
                            case MotionEvent.ACTION_UP:
                                break;
                        }

                        return true;
                    }
                });
            }
        };
    }

    private void toggleControlPanel(boolean showControls) {
        if (showControls) {
            mViewHolder.mLinearControlPanel.setVisibility(View.VISIBLE);
            mViewHolder.mLinearSharePanel.setVisibility(View.GONE);
        } else {
            mViewHolder.mLinearControlPanel.setVisibility(View.GONE);
            mViewHolder.mLinearSharePanel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_take_photo:
                if (!PermissionUtil.hasCameraPermission(this)) {
                    PermissionUtil.askCameraPermission(this);
                } else {
                    dispatchTakePictureIntent();
                }
                break;

            case R.id.image_zoom_in:
                ImageUtil.handleZoomIn(this.mImageSelected);
                break;

            case R.id.image_zoom_out:
                ImageUtil.handleZoomOut(this.mImageSelected);
                break;

            case R.id.image_rotate_left:
                ImageUtil.handleRotateLeft(this.mImageSelected);
                break;

            case R.id.image_rotate_right:
                ImageUtil.handleRotateRight(this.mImageSelected);
                break;

            case R.id.image_finish:
                toggleControlPanel(false);
                break;

            case R.id.image_remove:
                this.mViewHolder.mRelativePhotoContent.removeView(this.mImageSelected);
                break;

            case R.id.image_instagram:
                SocialUtil.shareImageOnInsta(this, this.mViewHolder.mRelativePhotoContent, view);
                break;

            case R.id.image_facebook:
                SocialUtil.shareImageOnFace(this, this.mViewHolder.mRelativePhotoContent, view);
                break;

            case R.id.image_twitter:
                SocialUtil.shareImageTwitter(this, this.mViewHolder.mRelativePhotoContent, view);
                break;

            case R.id.image_whatsapp:
                SocialUtil.shareImageWhats(this, this.mViewHolder.mRelativePhotoContent, view);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            this.setPhotoAsBackground();
        }
    }

    private void setPhotoAsBackground() {
        int targetW = this.mViewHolder.mImagePhoto.getWidth();
        int targetH = this.mViewHolder.mImagePhoto.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        String path = mFileForPhoto.getAbsolutePath();
        BitmapFactory.decodeFile(path, bmOptions);
        Uri photoURI = FileProvider.getUriForFile(this, AUTHORITY, mFileForPhoto);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);

        Bitmap bitmapRotated = ImageUtil.rotateImageIfRequired(bitmap, photoURI);

        this.mViewHolder.mImagePhoto.setImageBitmap(bitmapRotated);
    }

    public String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = this.getContentResolver().query(contentURI, null,
                null, null, null);

        if (cursor == null) {
            // path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            try {
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
            } catch (Exception e) {
                //AppLog.handleException(ImageHelper.class.getName(), e);
                //Toast.makeText(context, context.getResources().getString(R.string.error_get_image), Toast.LENGTH_SHORT).show();
                result = "";
            }
            cursor.close();
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.without_permission_camera_explanation))
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
/*        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Valida se é possível chamar essa intenção
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = ImageUtil.createImageFile(this);
                this.mViewHolder.mUriPhotoPath = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".my.package.name.provider", photoFile);
            } catch (IOException ex) {
                Toast.makeText(this, "Não foi possível iniciar a camera.", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".my.package.name.provider", photoFile));
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
*/
        // Create file to receive the image taken by the camera
        mFileForPhoto = null;
        try {
            mFileForPhoto = ImageUtil.createImageFile(this);
        } catch (IOException e) {
            Toast.makeText(this, "Não foi possível iniciar a camera.", Toast.LENGTH_SHORT).show();
        }

        // Returns null if create file was not possible
        if (mFileForPhoto != null) {

            // Provides the URI for the created file
            Uri photoURI = FileProvider.getUriForFile(this, AUTHORITY, mFileForPhoto);

            // Create intent to take picture
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {

                // Params of the intent
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Necessary to avoid unknown errors
                List<ResolveInfo> resInfoList = this.getApplicationContext().getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    this.getApplicationContext().grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                // Call the camera
                this.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (view.getId() == R.id.image_zoom_in)
            this.mLongEventType = LongEventType.ZoomIn;

        if (view.getId() == R.id.image_zoom_out)
            this.mLongEventType = LongEventType.ZoomOut;

        if (view.getId() == R.id.image_rotate_left)
            this.mLongEventType = LongEventType.RotateLeft;

        if (view.getId() == R.id.image_rotate_right)
            this.mLongEventType = LongEventType.RotateRight;

        mAutoIncrement = true;

        new RptUpdater().run();

        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int id = view.getId();
        if ((id == R.id.image_zoom_in)
                || (id == R.id.image_zoom_out)
                || (id == R.id.image_rotate_left)
                || (id == R.id.image_rotate_right)) {
            if (motionEvent.getAction() == motionEvent.ACTION_UP && mAutoIncrement) {
                mAutoIncrement = false;
                this.mLongEventType = null;
            }
        }

        return false;
    }

    private static class ViewHolder {
        ImageView mButtonZoomIn;
        ImageView mButtonZoomOut;
        ImageView mButtonRotateLeft;
        ImageView mButtonRotateRight;
        ImageView mButtonFinish;
        ImageView mButtonRemove;
        LinearLayout mLinearSharePanel;
        LinearLayout mLinearControlPanel;
        RelativeLayout mRelativePhotoContent;
        Uri mUriPhotoPath;
        ImageView mButtonTakePhoto;
        ImageView mImagePhoto;
        ImageView mImageInstagram;
        ImageView mImageFacebook;
        ImageView mImageTwitter;
        ImageView mImageWhatsapp;
    }

    private class RptUpdater implements Runnable {
        @Override
        public void run() {
            if (mAutoIncrement)
                mRepeatUpdateHandler.postDelayed(new RptUpdater(), 50);

            if (mLongEventType != null) {
                switch (mLongEventType) {
                    case ZoomIn:
                        ImageUtil.handleZoomIn(mImageSelected);
                        break;
                    case ZoomOut:
                        ImageUtil.handleZoomOut(mImageSelected);
                        break;
                    case RotateLeft:
                        ImageUtil.handleRotateLeft(mImageSelected);
                        break;
                    case RotateRight:
                        ImageUtil.handleRotateRight(mImageSelected);
                        break;
                }
            }
        }
    }
}
