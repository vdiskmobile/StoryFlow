
package com.youqude.storyflow.domain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StoryInfo extends BaseInfo {

    public String createTime;
    public String storyId;
    public String title;
    public String userId;
    public String nickName;

    public static ArrayList<StoryInfo> create(JSONObject jobj) throws JSONException {

        ArrayList<StoryInfo> data = new ArrayList<StoryInfo>();

        JSONObject jDataObj = jobj.optJSONObject("resp");
        if (jDataObj != null) {
            String infoCode = jDataObj.getString("infocode");

            if (infoCode.equals("200")) {
                JSONArray jsonArray = jDataObj.optJSONArray("story");

                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        StoryInfo info = new StoryInfo();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        info.createTime = jsonObject.optString("createTime");
                        info.storyId = jsonObject.optString("id");
                        info.title = jsonObject.getString("title");
                        info.userId = jsonObject.getString("userId");
                        info.nickName = jsonObject.optString("nickName");
                        data.add(info);
                    }
                } else {
                    JSONObject jsonObject = jDataObj.optJSONObject("story");
                    if (jsonObject != null) {
                        StoryInfo info = new StoryInfo();
                        info.createTime = jsonObject.optString("createTime");
                        info.storyId = jsonObject.optString("id");
                        info.title = jsonObject.getString("title");
                        info.userId = jsonObject.getString("userId");
                        info.nickName = jsonObject.optString("nickName");
                        data.add(info);
                    }
                }

                return data;
            }

            return null;
        }
        return null;
    }

    public static StoryInfo createStoryInfo(JSONObject jobj) throws JSONException {

        JSONObject jDataObj = jobj.optJSONObject("resp");
        if (jDataObj != null) {
            String infoCode = jDataObj.getString("infocode");

            if (infoCode.equals("200")) {
                JSONObject jsonObject = jDataObj.optJSONObject("story");

                if (jsonObject != null) {
                    StoryInfo info = new StoryInfo();
                    info.createTime = jsonObject.optString("createTime");
                    info.storyId = jsonObject.optString("id");
                    info.title = jsonObject.optString("title");
                    info.userId = jsonObject.optString("userId");
                    info.nickName = jsonObject.optString("nickName");
                    return info;
                }

                return null;
            }

            return null;
        }
        return null;
    }
}
