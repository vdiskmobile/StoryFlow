
package com.youqude.storyflow.net;

import com.youqude.storyflow.adapter.ShowStoryPicAdapter;
import com.youqude.storyflow.adapter.StoryFlowDescriptionAdapter;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.domain.MemoryImageCache;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class StoryDesBitmapLoadAsyncTask extends AsyncTask<Void, Void, Bitmap> {

    private static final String TAG = StoryDesBitmapLoadAsyncTask.class.getSimpleName();

    StoryFlowDescriptionAdapter mAdapter;
    Context ctx;
    ArrayList<AlbumInfo> data;

    private BitmapFactory.Options options = new BitmapFactory.Options();
    private Bitmap bitmapThumbnail = null;

    private int mStartIndex;
    private int mEndIndex;
    private MemoryImageCache mImageCache;

    public StoryDesBitmapLoadAsyncTask(Context ctx, StoryFlowDescriptionAdapter adapter, int mStartIndex,
            int mEndIndex, MemoryImageCache mImageCache) {
        this.ctx = ctx;
        this.mAdapter = adapter;
        this.mStartIndex = mStartIndex;
        this.mEndIndex = mEndIndex;
        this.mImageCache = mImageCache;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {

        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        Bitmap bitmap = null;

        for (; mStartIndex < mEndIndex; mStartIndex++) {
            try {
                StoryLogger.e(TAG, TAG + ":" + mStartIndex);
                AlbumInfo element = mAdapter.getItem(mStartIndex);
                if (element.picPath != null && !element.picPath.trim().equals("")) {

                    int index = element.picPath.lastIndexOf(".");
                    element.picPath = element.picPath.substring(0, index);

                    StoryLogger.e("picPath", element.picPath);
                    
                    element.picPath = element.picPath +".460x360x0.jpg";
                    
                    StoryLogger.e("picPath", element.picPath);

                    bitmap = mImageCache.get(element.picPath);
                    
                    if (bitmap != null) {
                    } else {
                        bitmap = downloadImageThumbnail(element);
                        if (bitmap != null) {
                            mImageCache.put(element.picPath, bitmap);
                            bitmap = null;
                            addImages(mImageCache);
                            publishProgress();
                        }
                    }
                } 
            } catch (Exception e) {
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                    if (baos != null) {
                        baos.flush();
                        baos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }
    
    @Override
    protected void onProgressUpdate(Void... values) {
        mAdapter.notifyDataSetChanged();
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
//        adapter.notifyDataSetChanged();
        super.onPostExecute(bitmap);
    }

    private  void addImages(MemoryImageCache imageCache) {
        if (mImageCache == null) {
            mImageCache = imageCache;
        } else {
            mImageCache.putAll(imageCache);
        }
    }

    private Bitmap downloadImageThumbnail(AlbumInfo element) {
        boolean flag = true;
        try {
            options.inSampleSize = 16;
            options.inScaled = true;
            options.inJustDecodeBounds = false;

            // 本地有缓存图片,读取本地缓存图片,本地没有,请求服务器,然后写缓存

            byte[] data = Utility.getImage(element.picPath, element.id);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (bitmap != null) {
                return bitmap;
            } else {
            }
        } catch (Exception e) {
        }
        return null;
    }

}
