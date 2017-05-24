package com.flipkart.sherlock.semantic.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Created by anurag.laddha on 15/04/17.
 */

/**
 * Serialisation/Deserialisation utils
 */
public class SerDeUtils {

    private static ObjectMapper objectMapper;

    static{
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * Cast string to desired generic type
     * @param value string to cast
     * @param typeReference: type to cast string to
     */
    public static <T> T castToGeneric(String value, TypeReference<T> typeReference) throws IOException {
        return !StringUtils.isBlank(value) ? objectMapper.readValue(value, typeReference) : null;
    }

    /**
     * Cast string to given type
     * @param value string to cast
     * @param clazz: type to cast string to
     */
    public static <T> T cast(String value, Class<T> clazz) throws IOException {
        if (clazz.equals(String.class)){
            //String needs to be encoded in quotes. "abc" must be encoded as "\"abc\"" for objectmapper to parse correctly.
            return (T) value;
        }
        else{
            return !StringUtils.isBlank(value) ? objectMapper.readValue(value, clazz) : null;
        }
    }

    /**
     * Returns value if it is not null, else returns default
     */
    public static <T> T getValueOrDefault(T value, T defaultValue){
        return value != null ? value : defaultValue;
    }
}
