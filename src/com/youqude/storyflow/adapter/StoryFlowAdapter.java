
package com.youqude.storyflow.adapter;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.domain.StoryInfo;
import com.youqude.storyflow.ui.HorizontialListView;
import com.youqude.storyflow.ui.ShowStoryPicActivity;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class StoryFlowAdapter extends BaseAdapter {

    private static final String TAG = StoryFlowAdapter.class.getSimpleName();
    
    Context mContext;
    // HorizontialListView mHorizontialListView ;

    ArrayList<StoryInfo> mData;
    
    HorizontialListViewAdapter mAdapter;
    
    StoryViewHolder holder = null;

    ListView mListView;
    
    String source;
    
    public StoryFlowAdapter(Context ctx, ListView mListView, String source) {

        this.mContext = ctx;
        this.mListView = mListView;
        this.source = source;
    }

    @Override
    public int getCount() {

        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    @Override
    public StoryInfo getItem(int position) {

        if (mData != null) {
            return mData.get(position);
        }
        return null;
    }

    public void setData(ArrayList<StoryInfo> data) {
        this.mData = data;
    }

    public void notifyChange() {
        notifyDataSetChanged();
    }
    
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            holder = new StoryViewHolder();
            convertView = inflater.inflate(R.layout.listview_item, null);
            holder.mTextView_Title = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.mTextView_storyStart = (TextView) convertView.findViewById(R.id.tvNickStart);
            holder.mTextView_nickname = (TextView) convertView.findViewById(R.id.tvNickname);
            holder.mHorizontialListView = (HorizontialListView) convertView
                    .findViewById(R.id.mHorizontialListView);
            convertView.setTag(holder);
        }

        holder = (StoryViewHolder) convertView.getTag();

        if (mData !=null && !mData.isEmpty() && StoryFlowApp.getInstance().mHashMap !=null
                && !StoryFlowApp.getInstance().mHashMap.isEmpty()) {
            ArrayList<AlbumInfo> mAlbumInfos = StoryFlowApp.getInstance().mHashMap.get(mData.get(position).storyId);
            
            mAdapter = new HorizontialListViewAdapter(mContext, mAlbumInfos, holder.mHorizontialListView, source);
            holder.mHorizontialListView.setAdapter(mAdapter);
            Utility.setListViewHeightBasedOnChildren(holder.mHorizontialListView);
            
            holder.mTextView_Title.setText(mData.get(position).title);
            holder.mTextView_storyStart.setText(mContext.getString(R.string.story_flow_starts));
            holder.mTextView_nickname.setText(mData.get(position).nickName);
        }
        
        holder.mTextView_nickname.setOnClickListener(mOnClickListener);
        
        return convertView;
    }

    public class StoryViewHolder {
        public TextView mTextView_Title;
        public TextView mTextView_storyStart;
        public TextView mTextView_nickname;
        public HorizontialListView mHorizontialListView;
    }

    /*
     * public View createView(){ LayoutInflater inflater = (LayoutInflater)
     * mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE); View view =
     * inflater.inflate(R.layout.listview_item, null); mHorizontialListView =
     * (HorizontialListView) view.findViewById(R.id.mHorizontialListView);
     * HorizontialListViewAdapter mAdapter = new
     * HorizontialListViewAdapter(mContext);
     * mHorizontialListView.setAdapter(mAdapter);
     * Utility.setListViewHeightBasedOnChildren(mHorizontialListView); return
     * view; }
     */

    OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = mListView.getPositionForView((View) v.getParent());
            StoryLogger.e(TAG, "Title clicked, row %d"+position);
            
            Intent intent = new Intent(Constants.CHANGE_USER_ACTION);
            
            if (source.equals("home")) {
                position--;
                try {
                    intent.putExtra("mUserId", mData.get(position).userId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (source.equals("self")) {
                try {
                    intent.putExtra("mUserId", mData.get(position).userId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mContext.sendBroadcast(intent);
            
        }
    };
}
