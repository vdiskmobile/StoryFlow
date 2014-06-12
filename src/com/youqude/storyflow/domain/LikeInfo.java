
package com.youqude.storyflow.domain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class LikeInfo extends BaseInfo implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -4455634819407487694L;
    
    public String createTime;
    public String id;
    public String picPath;
    public String picId;
    public String userId;
    public String storyId;
    public String likeStatus;

    public static ArrayList<LikeInfo> create(JSONObject jobj) throws JSONException {

        ArrayList<LikeInfo> data = new ArrayList<LikeInfo>();

        JSONObject jDataObj = jobj.optJSONObject("resp");
        if (jDataObj != null) {
            String infoCode = jDataObj.getString("infocode");

            if (infoCode.equals("200")) {
                JSONArray jsonArray = jDataObj.optJSONArray("like");

                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        LikeInfo info = new LikeInfo();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        info.createTime = jsonObject.getString("createTime");
                        info.id = jsonObject.getString("id");
                        info.picPath = jsonObject.getString("picPath");
                        info.picId = jsonObject.getString("picId");
                        info.userId = jsonObject.getString("userId");
                        info.storyId = jsonObject.getString("storyId");
                        info.likeStatus = jsonObject.getString("likeStatus");
                        data.add(info);
                    }
                } else {
                    JSONObject jsonObject = jDataObj.optJSONObject("like");
                    if (jsonObject !=null) {
                        LikeInfo info = new LikeInfo();
                        info.createTime = jsonObject.getString("createTime");
                        info.id = jsonObject.getString("id");
                        info.picPath = jsonObject.getString("picPath");
                        info.picId = jsonObject.getString("picId");
                        info.userId = jsonObject.getString("userId");
                        info.storyId = jsonObject.getString("storyId");
                        info.likeStatus = jsonObject.getString("likeStatus");
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
