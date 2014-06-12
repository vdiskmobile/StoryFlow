
package com.youqude.storyflow.ui;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryAPI;
import com.youqude.storyflow.StoryFlowEventHandler;
import com.youqude.storyflow.adapter.SearchStoryTitleAdapter;
import com.youqude.storyflow.adapter.SearchStoryTitleAdapter.ViewHolder;
import com.youqude.storyflow.domain.StoryInfo;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class SearchStoryTitleActivity extends BaseActivity implements StoryFlowEventHandler,
        OnClickListener {

    private static final String TAG = SearchStoryTitleActivity.class.getSimpleName();
    
    ImageButton mImageButton_delete;
    Button mButton_finish;
    ProgressBar mProgressBarLoading;
    
    EditText mEditText;
    
    ListView mListView;
    SearchStoryTitleAdapter mAdapter;
    
    
    ArrayList<StoryInfo> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.search_story_title);

        mImageButton_delete = (ImageButton) findViewById(R.id.btnDelete);
        mButton_finish = (Button) findViewById(R.id.btnFinish);
        mEditText = (EditText) findViewById(R.id.search_text);
        mProgressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
        
        mEditText.addTextChangedListener(mTextWatcher);
        
        mImageButton_delete.setOnClickListener(this);
        mButton_finish.setOnClickListener(this);
        
        mListView = (ListView) findViewById(R.id.listview);
       /* LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View mHeaderView = inflater.inflate(R.layout.header, null);
        mListView.addHeaderView(mHeaderView);*/
        
        mAdapter = new SearchStoryTitleAdapter(this, mListView);
        mListView.setAdapter(mAdapter);
        
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                ViewHolder holder = (ViewHolder) view.getTag();
                
                StoryLogger.e(TAG, TAG+":"+ holder.mTextView.getText()+"---->"+position);
                Intent intent = new Intent();
                intent.putExtra("storyTitle", holder.mTextView.getText());
                intent.putExtra("storyId", data.get(position).storyId);
                setResult(RESULT_OK, intent);
                finish();
                
            }
            
        });
       
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void afterServiceConnected() {

    }

    @Override
    public void handleSeviceResult(String err_msg, int eventId, Object rlt) {

        mProgressBarLoading.setVisibility(View.GONE);
        mImageButton_delete.setVisibility(View.VISIBLE);
        
        switch (eventId) {
            case Constants.STORY_TITLE_LIST_SUCCESS:{
                 
                /**
                 * 初始化数据源
                 */
                Object[] obj = (Object[]) rlt;
                data = (ArrayList<StoryInfo>) obj[0];
                mAdapter.setData(data);
                mAdapter.notifyChanged();
                
                
                
                break;
            }
            case Constants.STORY_TITLE_LIST_FAILED:{
                Toast.makeText(SearchStoryTitleActivity.this,
                        getResources().getString(R.string.data_load_failed_toast),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.STORY_TITLE_LIST_NET_INTERRUPT:{
                Toast.makeText(SearchStoryTitleActivity.this,
                        getResources().getString(R.string.no_network_connection_toast),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            default:
                break;
        }
        
        
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDelete:
                mEditText.setText("");
                break;
            case R.id.btnFinish:
                Intent intent = new Intent();
                intent.putExtra("storyTitle", mEditText.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
                break;

            default:
                break;
        }

    }
    
    TextWatcher mTextWatcher = new TextWatcher() {
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        
        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s.toString())) {
                mProgressBarLoading.setVisibility(View.VISIBLE);
                mImageButton_delete.setVisibility(View.GONE);
                if (mService !=null) {
                    mService.searchStoryTitleListByKeywords(SearchStoryTitleActivity.this, s.toString());
                }
            } else {
                mProgressBarLoading.setVisibility(View.GONE);
                mImageButton_delete.setVisibility(View.GONE);
            }
        }
    };

}
