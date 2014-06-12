
package com.youqude.storyflow.domain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class PicIdInfo extends BaseInfo implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -4455634819407487694L;

    public static ArrayList<String> create(JSONObject jobj) throws JSONException {

        ArrayList<String> data = new ArrayList<String>();

        JSONObject jDataObj = jobj.optJSONObject("resp");
        if (jDataObj != null) {
            String infoCode = jDataObj.getString("infocode");

            if (infoCode.equals("200")) {
                JSONArray jsonArray = jDataObj.optJSONArray("picIdList");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String picId = jsonArray.getString(i);
                        data.add(picId);
                    }
                } else {
                    String picId = jDataObj.getString("picIdList");
                    data.add(picId);
                }

                return data;
            }

            return null;
        }
        return null;
    }
}
