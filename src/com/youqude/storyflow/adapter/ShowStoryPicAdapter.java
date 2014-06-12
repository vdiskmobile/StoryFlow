package com.youqude.storyflow.adapter;

import com.youqude.storyflow.R;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.domain.ImageCache;
import com.youqude.storyflow.net.ImageDownloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class ShowStoryPicAdapter extends BaseAdapter{

    private static final String TAG = ShowStoryPicAdapter.class.getSimpleName();
    
    Context mContext;
    
    ArrayList<AlbumInfo> data;
    
    private ImageCache mImageCache;
    
    private ImageDownloader downloader;
    
    public ShowStoryPicAdapter(Context context, ImageCache mImageCache) {
        this.mContext = context;
        this.mImageCache = mImageCache;
        downloader = new ImageDownloader();
    }

    @Override
    public int getCount() {
       
        if (data!=null) {
            return data.size();
        }
        return 0;
        
    }

    public void setData(ArrayList<AlbumInfo> data){
        this.data = data;
    }
    
    @Override
    public AlbumInfo getItem(int position) {
        return this.data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    public void notifyChanged(){
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        ViewHolder holder;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_thumbnail_item, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            
            holder.mImageView = (ImageView) convertView.findViewById(R.id.mImageView);
        }
        
        holder = (ViewHolder) convertView.getTag();
        
        String picPath = data.get(position).picPath;
        int index = picPath.lastIndexOf(".");
        picPath = picPath.substring(0, index);
        picPath = picPath +".80x80x0.jpg"; 
        
        try {
            downloader.download(picPath, holder.mImageView, mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
       /* Bitmap bitmap = mImageCache.get(data.get(position).picPath);
        if (bitmap==null) {
            image.setImageBitmap(Utils.getFileIcon(ctx,
                    R.drawable.picture_icon));
        }else{
            holder.mImageView.setImageBitmap(bitmap);
        }*/
        
        
        return convertView;
    }
    
    
    class ViewHolder{
        public ImageView mImageView;
    }
    

}
