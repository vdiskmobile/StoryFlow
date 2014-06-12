package com.youqude.storyflow.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class StoryUserTabCountInfo extends BaseInfo {

    
    public String likeCount;
    public String picCount;
    public String storyCount;
    
    public static StoryUserTabCountInfo create(JSONObject jobj) throws JSONException{
        
        StoryUserTabCountInfo info = new StoryUserTabCountInfo();
        JSONObject jsonObject = jobj.optJSONObject("resp");
        
        if (jsonObject !=null) {
            String infoCode = jsonObject.getString("infocode");
            
            if (infoCode.equals("200")) {
                JSONObject jsonObject2 = jsonObject.optJSONObject("home");
                if (jsonObject2 !=null) {
                    info.likeCount = jsonObject2.getString("likeCount");
                    info.picCount = jsonObject2.getString("picCount");
                    info.storyCount = jsonObject2.getString("storyCount");
                    
                    return info;
                }
                return null;
            } else {
                return null;
            }
        }
        
        return null;
    }
}
