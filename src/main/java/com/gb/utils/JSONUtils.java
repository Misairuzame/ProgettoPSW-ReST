package com.gb.utils;

import com.gb.modelObject.Music;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class JSONUtils {

    public static String formatMessage(String msg, int httpCode, String reqStatus) {
        return new JSONObject()
                        .put("httpStatus", httpCode)
                        .put("requestStatus", reqStatus)
                        .put("message", msg)
                        .put("data", new JSONArray())
                        .toString(5);
    }

    public static String formatMusic(List<Music> musicList, int httpCode, String reqStatus, String msg) {
        JSONObject jsonObject = new JSONObject()
                .put("httpStatus", httpCode)
                .put("requestStatus", reqStatus)
                .put("message", msg);

        JSONArray jsonArray = new JSONArray();
        for (Music i : musicList) {
            jsonArray.put(i.getJson());
        }

        jsonObject.put("data", jsonArray);

        return jsonObject.toString(5);
    }

}
