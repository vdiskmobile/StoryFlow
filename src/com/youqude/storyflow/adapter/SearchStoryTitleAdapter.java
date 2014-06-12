package com.youqude.storyflow.adapter;

import com.youqude.storyflow.R;
import com.youqude.storyflow.domain.StoryInfo;
import com.youqude.storyflow.ui.ShowStoryPicActivity;
import com.youqude.storyflow.utils.StoryLogger;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class SearchStoryTitleAdapter extends BaseAdapter{

    private static final String TAG = SearchStoryTitleAdapter.class.getSimpleName();
    
    Context mContext;
    ListView mListView;
    
    ArrayList<StoryInfo> data;
    
    public SearchStoryTitleAdapter(Context context, ListView listView) {
        this.mContext = context;
        this.mListView = listView;
    }

    @Override
    public int getCount() {
        
        if (data !=null) {
            return this.data.size();
        }
        return 0;
    }

    public void setData(ArrayList<StoryInfo> data){
        this.data = data;
    }
    
    public void notifyChanged(){
        notifyDataSetChanged();
    }
    
    @Override
    public Object getItem(int position) {
        return this.data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        ViewHolder holder;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.story_title_item, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            
            holder.mTextView = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.mButton = (Button) convertView.findViewById(R.id.btnEnter);
            holder.mButton.setOnClickListener(mOnClickListener);
        }
        
        holder = (ViewHolder) convertView.getTag();
        holder.mTextView.setText(data.get(position).title);
        
        return convertView;
    }
    
    
    public class ViewHolder{
        public TextView mTextView;
        public Button mButton;
    }
    
    
    OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = mListView.getPositionForView((View) v.getParent());
            StoryLogger.e(TAG, "Title clicked, row %d"+position);
            
            Intent intent = new Intent(mContext, ShowStoryPicActivity.class);
            intent.putExtra("storyId", data.get(position).storyId);
            intent.putExtra("title", data.get(position).title);
            mContext.startActivity(intent);
            
        }
    };
    

}
