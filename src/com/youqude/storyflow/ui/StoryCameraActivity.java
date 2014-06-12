package com.youqude.storyflow.ui;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowEventHandler;
import com.youqude.storyflow.utils.Utility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StoryCameraActivity extends BaseActivity implements StoryFlowEventHandler,SurfaceHolder.Callback,OnClickListener{

   
    Camera mCamera;
    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;

    
    Button mButton_Cancel;
    ImageView mButton_Camera;
    Button mButton_Gallery;
    
    RelativeLayout cameraLayout;
    RelativeLayout previewLayout;
    
    Button mButton_Retake;
    Button mButton_UsePicture;
    
    File sdImageMainDirectory;
    
    static final int GET_IMAGE_FROM_GALLERY = 0x1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.story_camera);
        
        mButton_Cancel = (Button) findViewById(R.id.btnCancel);
        mButton_Camera = (ImageView) findViewById(R.id.btnCamera);
        mButton_Gallery = (Button) findViewById(R.id.btnGallery);
        
        cameraLayout = (RelativeLayout) findViewById(R.id.cameraLayout);
        previewLayout = (RelativeLayout) findViewById(R.id.previewLayout);
        
        mButton_Retake = (Button) findViewById(R.id.btnRetake);
        mButton_UsePicture = (Button) findViewById(R.id.btnUsePicture);
       
        mButton_Cancel.setOnClickListener(this);
        mButton_Camera.setOnClickListener(this);
        mButton_Gallery.setOnClickListener(this);
        mButton_Retake.setOnClickListener(this);
        mButton_UsePicture.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        
//        getWindow().setFormat(PixelFormat.UNKNOWN);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        super.onResume();
    }
    
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSurfaceHolder.removeCallback(this);
    }

    @Override
    protected void afterServiceConnected() {

    }
    
    @Override
    public void handleSeviceResult(String err_msg, int eventId, Object rlt) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        try {
            mCamera.setDisplayOrientation(90);//默认0表示横屏，90表示竖屏
            mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
            // TODO: add more exception handling logic here
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();

        List<Size> sizes = parameters.getSupportedPreviewSizes();
        Size optimalSize = getOptimalPreviewSize(sizes, width, height);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (Exception e1) {
                e1.printStackTrace();
                Toast.makeText(StoryCameraActivity.this,
                        getString(R.string.camera_start_preview_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
    

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    
    
    
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] imageData, Camera c) {

            if (imageData != null) {

                Intent mIntent = new Intent();

                Date date = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss");
                String newPicFile = df.format(date) + ".jpg";
                StoreByteImage(StoryCameraActivity.this, imageData, 50,
                        newPicFile);
//                mCamera.startPreview();

              /*  setResult(FOTO_MODE, mIntent);
                finish();*/

            }
        }
        
    };

    public  boolean StoreByteImage(Context mContext, byte[] imageData,
            int quality, String expName) {

        String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/"+getResources().getString(R.string.app_name)+"/" + expName;
        
        sdImageMainDirectory = Utility.createDirFile(cachePath);
        try {

            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize = 4;

            Bitmap myImage = BitmapFactory.decodeByteArray(imageData, 0,
                    imageData.length,options);

            if (getWindowManager().getDefaultDisplay().getOrientation() == 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(90);
                myImage = Bitmap.createBitmap(myImage, 0, 0,
                        myImage.getWidth(),
                        myImage.getHeight(), matrix, true);
            }

            FileOutputStream fileOutputStream = new FileOutputStream(
                    sdImageMainDirectory);


            BufferedOutputStream bos = new BufferedOutputStream(
                    fileOutputStream);

            myImage.compress(CompressFormat.JPEG, 100, bos);
            
            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    
    
    
    
    
    @Override
    public void onClick(View v) {
        
        switch (v.getId()) {
            case R.id.btnCancel:
                finish();
                
                break;
            case R.id.btnCamera:
                
                cameraLayout.setVisibility(View.GONE);
                previewLayout.setVisibility(View.VISIBLE);
                mCamera.takePicture(null, mPictureCallback, mPictureCallback);
                
                break;
            case R.id.btnGallery:
                
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GET_IMAGE_FROM_GALLERY);
                
                break;

            case R.id.btnRetake:
                cameraLayout.setVisibility(View.VISIBLE);
                previewLayout.setVisibility(View.GONE);
                mCamera.startPreview();
                break;
            case R.id.btnUsePicture:
                
                /*ProgressDialog pd = new ProgressDialog(StoryCameraActivity.this);
                pd.setMessage(getString(R.string.make_thumbnail_loading));
                pd.setIndeterminate(true);
                pd.show();*/
                Intent intent2 = new Intent(StoryCameraActivity.this, NewStoryFlowActivity.class);
                try {
                    intent2.putExtra("imageFile", sdImageMainDirectory.getPath());
                    intent2.putExtra("imageFileSize", sdImageMainDirectory.length());
                    Bundle extras = getIntent().getExtras();
                    if (extras !=null) {
                        intent2.putExtra("storyTitle", extras.getString("storyTitle"));
                        intent2.putExtra("storyId", extras.getString("storyId"));
                    }
                    startActivity(intent2);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(StoryCameraActivity.this, "拍摄失败,请重新拍摄", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == GET_IMAGE_FROM_GALLERY) {
                
                Uri uri = data.getData();
                ContentResolver cr = this.getContentResolver();
                String str = uri.toString();
                Uri mediaUri = Uri.parse("content://media/external/images/media");
                String _id = str.substring(str.lastIndexOf("/") + 1, str.length());
                Cursor cursor = cr.query(mediaUri, new String[] {
                        Media._ID, Media.DATA, Media.SIZE, Media.DISPLAY_NAME
                }, MediaStore.Images.ImageColumns._ID + "=" + _id, null, null);
                cursor.moveToFirst();
                startManagingCursor(cursor);
                String filePath;
                long fileSize;
                if (cursor!=null && cursor.getCount()>0 ) {
                    filePath = cursor
                            .getString(cursor.getColumnIndexOrThrow(Media.DATA));
                    fileSize = cursor
                            .getLong(cursor.getColumnIndexOrThrow(Media.SIZE));
                } else {
                    mediaUri = Uri.parse("content://media/internal/images/media");
                    cursor = cr.query(mediaUri, new String[] {
                            Media._ID, Media.DATA, Media.SIZE, Media.DISPLAY_NAME
                    }, MediaStore.Images.ImageColumns._ID + "=" + _id, null, null);
                    cursor.moveToFirst();
                    startManagingCursor(cursor);
                    filePath = cursor
                            .getString(cursor.getColumnIndexOrThrow(Media.DATA));
                    fileSize = cursor
                            .getLong(cursor.getColumnIndexOrThrow(Media.SIZE));
                }
                Intent intent = new Intent(StoryCameraActivity.this, NewStoryFlowActivity.class);
                intent.putExtra("imageFile", filePath);
                intent.putExtra("imageFileSize", fileSize);
                startActivity(intent);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    

}
