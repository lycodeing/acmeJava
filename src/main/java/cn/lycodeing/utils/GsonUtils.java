package cn.lycodeing.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * json 工具类
 *
 * @author lycodeing
 */
public class GsonUtils {

    private static final Gson GSON = new GsonBuilder().setLenient().create();


    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }
}
