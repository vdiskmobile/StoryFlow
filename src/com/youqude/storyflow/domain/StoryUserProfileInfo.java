package com.youqude.storyflow.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class StoryUserProfileInfo extends BaseInfo {

    
    public String location;
    public String nickName;
    public String userAvatar;
    public String userId;
    
    public static StoryUserProfileInfo create(JSONObject jobj) throws JSONException{
        
        StoryUserProfileInfo info = new StoryUserProfileInfo();
        JSONObject jsonObject = jobj.optJSONObject("resp");
        
        if (jsonObject !=null) {
            String infoCode = jsonObject.getString("infocode");
            
            if (infoCode.equals("200")) {
                info.location = jsonObject.getString("location");
                info.nickName = jsonObject.getString("nickName");
                info.userAvatar = jsonObject.getString("userAvatar");
                info.userId = jsonObject.getString("userId");
                
                return info;
            } else {
                return null;
            }
        }
        
        return null;
    }
}
