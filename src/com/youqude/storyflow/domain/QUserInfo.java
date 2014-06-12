package com.youqude.storyflow.domain;

import com.youqude.storyflow.utils.StoryLogger;

import org.json.JSONException;
import org.json.JSONObject;

public class QUserInfo extends BaseInfo {

    public String nick;
    public String location;
    public String head;
    
    public static QUserInfo create(String rlt) {
        
        JSONObject jobj = null;
        QUserInfo qUserInfo = null;
        try {
            qUserInfo = new QUserInfo();
            jobj = new JSONObject(rlt);
            JSONObject jDataObj = jobj.optJSONObject("data");

            qUserInfo.nick = jDataObj.getString("nick");
            qUserInfo.location = jDataObj.getString("location");
            qUserInfo.head = jDataObj.optString("head");
            qUserInfo.head =  qUserInfo.head+"/100";
            StoryLogger.e("HEAD", "HEAD---------->"+qUserInfo.head);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return qUserInfo;
    }
    
}
