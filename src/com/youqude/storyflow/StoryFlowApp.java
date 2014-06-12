
package com.youqude.storyflow;

import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.domain.UserInfo;

import android.app.Application;

import java.util.ArrayList;
import java.util.HashMap;

public class StoryFlowApp extends Application implements Thread.UncaughtExceptionHandler {

    private static StoryFlowApp mInstance;

    public UserInfo userInfo = null;
    
    public String storyTitle = "";
    
    public HashMap<String, ArrayList<AlbumInfo>> mHashMap = null;
    
    
    /**
     * 默认值
     */
    public int HorizontialPageNum = 1;
    public int HorizontialPageSize = 200;
    
    
    public String mChangedUserId = "";
    
    public int mScreenDensityDpi;
    
    public StoryFlowApp() {
    }

    public static StoryFlowApp getInstance() {
        if (null == mInstance) {
            mInstance = new StoryFlowApp();
        }
        return mInstance;
    }

    @Override
    public void onCreate() {
        
//        Thread.setDefaultUncaughtExceptionHandler(StoryFlowApp.getInstance());
        super.onCreate();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        android.os.Process.killProcess(android.os.Process.myPid());
        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread,
        ex);
    }

}
