package br.com.falbuquerque.logging.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Converts objects into their JSON representations.
 * 
 * @author Felipe Albuquerque
 */
public class JsonParser {

    private final Gson serializer = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * Converts an object to its JSON representation.
     * 
     * @param object
     *            the object to be converted
     * @return the JSON representation of the given object
     */
    public String toJson(Object object) {
        return serializer.toJson(object);
    }

}
