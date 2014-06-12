
package com.youqude.storyflow.ui;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class MainActivity extends TabActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TabHost tabHost;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.EXIT_CURRENT_SESSION_ACTION)) {
                tabHost.setCurrentTab(0);
            }

        }
    };

    private BroadcastReceiver mUserChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.CHANGE_USER_ACTION)) {
                
                Bundle extras = intent.getExtras();
                if (extras !=null) {
                    userId = extras.getString("mUserId");
                    StoryFlowApp.getInstance().mChangedUserId = userId;
                }
                
                tabHost.setCurrentTab(2);
            }

        }
    };

    private String userId;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        
        DisplayMetrics dm = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(dm);  
      /*  int screenWidth = dm.widthPixels;  
        int screenHeight = dm.heightPixels;  */
        
        StoryFlowApp.getInstance().mScreenDensityDpi = dm.densityDpi;
        StoryLogger.e(TAG, TAG+"--------------------->"+StoryFlowApp.getInstance().mScreenDensityDpi);
        
        setContentView(R.layout.tabhost);

        tabHost = this.getTabHost();

        Intent intent = new Intent(this, StoryHomeActivity.class);
        intent.putExtras(getIntent());
        TabHost.TabSpec spec1 = tabHost.newTabSpec(getString(R.string.story_home_tab))
                .setIndicator(createTabView(R.drawable.tab_find))
                .setContent(intent);
        tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = tabHost.newTabSpec(getString(R.string.story_camera_tab))
                .setIndicator(createTabButton(R.drawable.tab_camera))
                .setContent(new Intent(this, MainActivity.class));
        tabHost.addTab(spec2);

        Intent intent3 =new Intent(this, StorySelfActivity.class);
        if (!TextUtils.isEmpty(userId)) {
            intent3.putExtra("mUserId", userId);
        }
        TabHost.TabSpec spec3 = tabHost.newTabSpec(getString(R.string.story_self_tab))
                .setIndicator(createTabView(R.drawable.tab_self))
                .setContent(intent3);
        tabHost.addTab(spec3);

        tabHost.setOnTabChangedListener(new OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                StoryLogger.e("tabid", tabId);

                if (!TextUtils.isEmpty(StoryFlowApp.getInstance().userInfo.sessionId)) {
                    if (tabId.equals(getResources().getString(R.string.story_camera_tab))) {
                        tabHost.setCurrentTab(0);
                        startActivity(new Intent(MainActivity.this, StoryCameraActivity.class));
                    }
                } else {
                    if (tabId.equals(getResources().getString(R.string.story_camera_tab))
                           ) {
                        tabHost.setCurrentTab(0);
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                    
                    if (tabId.equals(getResources().getString(R.string.story_self_tab))) {
                        tabHost.setCurrentTab(0);
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                }

            }
        });

        IntentFilter filter = new IntentFilter(Constants.EXIT_CURRENT_SESSION_ACTION);
        registerReceiver(mReceiver, filter);

        IntentFilter changeUserFilter = new IntentFilter(Constants.CHANGE_USER_ACTION);
        registerReceiver(mUserChangeReceiver, changeUserFilter);

    }

    private View createTabView(int id) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.main_subtab, null);
        // LinearLayout layout = (LinearLayout)
        // view.findViewById(R.id.tab_root);
        ImageView iv = (ImageView) view.findViewById(R.id.tab_imageview_icon);
        // layout.setBackgroundResource(id);
        iv.setImageResource(id);
        return view;
    }

    private View createTabButton(int id) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.camera_button, null);
        ImageView mButton = (ImageView) view.findViewById(R.id.tab_button);
        mButton.setBackgroundResource(id);
        return view;
    }

    /*
     * @Override public boolean dispatchKeyEvent(KeyEvent event) { if
     * (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() ==
     * KeyEvent.ACTION_DOWN) { if (StoryFlowApp.getInstance().isShouldExit) {
     * exit(); } return true; } return super.dispatchKeyEvent(event); }
     */

    /*
     * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { if
     * (keyCode == KeyEvent.KEYCODE_BACK) { if
     * (StoryFlowApp.getInstance().isShouldExit) { exit(); } } return
     * super.onKeyDown(keyCode, event); }
     */

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {

        StoryLogger.e("onResume", TAG + "onResume");

        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
        unregisterReceiver(mUserChangeReceiver);
    }

}
