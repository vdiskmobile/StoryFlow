package com.youqude.storyflow.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class SinaUserInfo extends BaseInfo {

    public String screen_name;
    public String location;
    public String profile_image_url;
    
    public static SinaUserInfo create(JSONObject jobj) {
        
        SinaUserInfo sinaUserInfo = null;
        try {
            sinaUserInfo = new SinaUserInfo();
            sinaUserInfo.screen_name = jobj.getString("screen_name");
            sinaUserInfo.location = jobj.getString("location");
            sinaUserInfo.profile_image_url = jobj.optString("profile_image_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sinaUserInfo;
    }
    
}
