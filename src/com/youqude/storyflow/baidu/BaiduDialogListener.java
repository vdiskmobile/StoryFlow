package com.youqude.storyflow.baidu;

import android.os.Bundle;

public interface BaiduDialogListener {

    public  void onComplete(Bundle paramBundle);

    public  void onException(String paramString);

    public  void onCancel();
}
