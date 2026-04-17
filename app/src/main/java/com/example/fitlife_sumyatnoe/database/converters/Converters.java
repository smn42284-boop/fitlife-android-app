package com.example.fitlife_sumyatnoe.database.converters;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Converters {
    private static Gson gson = new Gson();

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null) {
            return "[]";
        }
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toList(String data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(data, type);
    }
}