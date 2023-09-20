package com.tmynttin.taskulaji.document;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;

public class Geometry implements Serializable {
    public String type;
    public double[][][] coordinates;

    public Geometry(double[][] coordinates) {
        this.coordinates = new double[1][][];
        this.coordinates[0] = coordinates;

        if (this.coordinates[0].length == 1) {
            type = "Point";
        }
        else if (this.coordinates[0].length > 1) {
            type = "Polygon";
        }
    }

    public String getType() {
        return type;
    }


    public String getCoordinates() {
        if(coordinates[0].length == 1) {
            return Arrays.toString(coordinates[0][0]);
        }
        else {
            return Arrays.toString(coordinates);
        }
    }

    @Override
    public String toString() {
        return "Geometry [type=" + type + ", coordinates=" + getCoordinates() + "]";
    }



}


