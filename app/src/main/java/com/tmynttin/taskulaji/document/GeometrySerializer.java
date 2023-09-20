package com.tmynttin.taskulaji.document;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class GeometrySerializer implements JsonSerializer<Geometry> {

    public static final String TAG = "GeometrySerializer";

    @Override
    public JsonElement serialize(Geometry src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject object = new JsonObject();
        String type = src.type;
        object.addProperty("type", type);

        double[][][] coordinates = src.coordinates;
        JsonArray jsonCoordinates = new JsonArray();

        if (type.equals("Point")) {
            jsonCoordinates.add(coordinates[0][0][0]);
            jsonCoordinates.add(coordinates[0][0][1]);
        }

        else if(type.equals("Polygon")) {
            JsonArray jsonCoordinates2 = new JsonArray();

            int count = coordinates[0].length;
            for (int i = 0; i < count; i++) {
                JsonArray jsonCoordinates3 = new JsonArray();
                jsonCoordinates3.add(coordinates[0][i][0]);
                jsonCoordinates3.add(coordinates[0][i][1]);
                jsonCoordinates2.add(jsonCoordinates3);
            }
            jsonCoordinates.add(jsonCoordinates2);
        }

        object.add("coordinates", jsonCoordinates);

        return object;
    }
}
