package com.youqude.storyflow.adapter;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.domain.StoryInfo;
import com.youqude.storyflow.net.ImageDownloader;
import com.youqude.storyflow.ui.HorizontialListView;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;

public class HorizontialListViewAdapter extends BaseAdapter {

    private static final String TAG = HorizontialListViewAdapter.class.getSimpleName();
    
    Context ctx;
    private ImageDownloader downloader;
    
    ArrayList<AlbumInfo> mAlbumInfos ;
    
    HorizontialListView mHorizontialListView;
    
    String source;
    
    public HorizontialListViewAdapter(Context ctx, ArrayList<AlbumInfo> mAlbumInfos,  HorizontialListView horizontialListView
            , String source){
        this.ctx = ctx;
        this.mAlbumInfos = mAlbumInfos;
        this.mHorizontialListView = horizontialListView;
        this.source = source;
        downloader = new ImageDownloader();
    }
    
    @Override
    public int getCount() {
        
        if (mAlbumInfos !=null) {
            StoryLogger.e(TAG, TAG+":mAlbumInfos--->"+mAlbumInfos.size());
            return mAlbumInfos.size();
        }
        return 0;
    }

    @Override
    public AlbumInfo getItem(int position) {
        return mAlbumInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ArrayList<AlbumInfo> getItems(){
        
        return mAlbumInfos;
    }
    
    @Override
    public boolean areAllItemsEnabled() {
        
        StoryLogger.e(TAG, TAG+"------------->");
        
        return super.areAllItemsEnabled();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.horizontial_item, null);
            holder = new ViewHolder();
            holder.mImageView = (ImageView)convertView.findViewById(R.id.mImageView);
            convertView.setTag(holder);
            
        }
        
        holder = (ViewHolder) convertView.getTag();
        holder.mImageView.setImageResource(R.drawable.default_icon);
        if (mAlbumInfos !=null) {
            String picPath = mAlbumInfos.get(position).picPath;
            int index = picPath.lastIndexOf(".");
            picPath = picPath.substring(0, index);
            picPath = picPath +".80x80x0.jpg"; 
            if (!TextUtils.isEmpty(picPath)) {
                try {
                    downloader.download(picPath,  holder.mImageView, ctx);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                holder.mImageView.setImageResource(R.drawable.default_icon);
            }
        } 
       
        
        mHorizontialListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
                if (source.equals("home")) {
                    Intent intent2 = new Intent(Constants.CHANGE_STORY_HOME_DES_CONTENT_VIEW_ACTION);
                    intent2.putExtra("data", mAlbumInfos);
                    intent2.putExtra("mStoryId", mAlbumInfos.get(position).storyId);
                    intent2.putExtra("index", position);
                    ctx.sendBroadcast(intent2);
                } else if (source.equals("self")) {
                    Intent intent = new Intent(Constants.CHANGE_STORY_SELF_DES_CONTENT_VIEW_ACTION);
                    intent.putExtra("data", mAlbumInfos);
                    intent.putExtra("mStoryId", mAlbumInfos.get(position).storyId);
                    intent.putExtra("index", position);
                    ctx.sendBroadcast(intent);
                }
            }
        });
        
        
        
        return convertView;
    }
    
    
    
    private class ViewHolder{
        public ImageView mImageView;
       
    }

    
}
