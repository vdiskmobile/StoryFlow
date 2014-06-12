
package com.youqude.storyflow.domain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class AlbumInfo extends BaseInfo implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -4455634819407487694L;
    
    public String createTime;
    public String id;
    public String picPath;
    public String userId;
    public String nickname;
    public String storyId;
    public String userAvatar;
    public String description;
    public String storyTitle = "";
    public boolean isLiked;

    public static ArrayList<AlbumInfo> create(JSONObject jobj) throws JSONException {

        ArrayList<AlbumInfo> data = new ArrayList<AlbumInfo>();

        JSONObject jDataObj = jobj.optJSONObject("resp");
        if (jDataObj != null) {
            String infoCode = jDataObj.getString("infocode");

            if (infoCode.equals("200")) {
                JSONArray jsonArray = jDataObj.optJSONArray("album");

                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        AlbumInfo info = new AlbumInfo();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        info.createTime = jsonObject.getString("createTime");
                        info.id = jsonObject.getString("id");
                        info.picPath = jsonObject.optString("picPath");
                        info.userId = jsonObject.getString("userId");
                        info.nickname = jsonObject.optString("nickname");
                        info.userAvatar = jsonObject.optString("userAvatar");
                        info.storyId = jsonObject.getString("storyId");
                        info.description = jsonObject.optString("description");
                        data.add(info);
                    }
                } else {
                    JSONObject jsonObject = jDataObj.optJSONObject("album");
                    if (jsonObject !=null) {
                        AlbumInfo info = new AlbumInfo();
                        info.createTime = jsonObject.getString("createTime");
                        info.id = jsonObject.getString("id");
                        info.picPath = jsonObject.optString("picPath");
                        info.userId = jsonObject.getString("userId");
                        info.nickname = jsonObject.optString("nickname");
                        info.userAvatar = jsonObject.optString("userAvatar");
                        info.storyId = jsonObject.getString("storyId");
                        info.description = jsonObject.optString("description");
                        data.add(info);
                    } 
                }

                return data;
            }

            return null;
        }
        return null;
    }
}
