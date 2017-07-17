package com.flipkart.sherlock.semantic.autosuggest.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

/**
 * Created by dhruv.pancholi on 30/05/17.
 */
@AllArgsConstructor
public class JsonSeDe {

    private ObjectMapper objectMapper;

    public String writeValueAsString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Unable to serialize the object: " + object.toString(), e);
        }
    }

    public <T> T readValue(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Unable to de-serialize the json: " + json, e);
        }
    }

    public <T> T readValue(String json, Class<T> klass) {
        try {
            return objectMapper.readValue(json, klass);
        } catch (Exception e) {
            throw new RuntimeException("Unable to de-serialize the json: " + json, e);
        }
    }

    public static JsonSeDe getInstance() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(objectMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return new JsonSeDe(objectMapper);
    }
}
