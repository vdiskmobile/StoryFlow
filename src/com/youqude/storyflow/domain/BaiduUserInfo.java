
package com.youqude.storyflow.domain;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class BaiduUserInfo extends BaseInfo {

    public String uid;
    public String uname;
    public String portrait;
    public String location;

    public static BaiduUserInfo create(JSONObject jobj) {
        BaiduUserInfo info = new BaiduUserInfo();

        info.uid = jobj.optString("uid", null);
        if (null != info.uid) {
            info.uname = jobj.optString("uname", null);//百度用户名
            info.portrait = jobj.optString("portrait", null);//头像 
            //http://himg.bdimg.com/sys/portraitn/item/%s.jpg
            info.portrait = String.format("http://himg.bdimg.com/sys/portraitn/item/%s.jpg", info.portrait);
            Locale locale = Locale.getDefault();;
            info.location = locale.getDisplayCountry();
            
            return info;
        }

        return null;
    }
}
