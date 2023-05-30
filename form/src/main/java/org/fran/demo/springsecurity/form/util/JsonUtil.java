package org.fran.demo.springsecurity.form.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.json.JsonSanitizer;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author qiushi
 * @date 2023/5/19
 */
public class JsonUtil {
    private static ObjectMapper objectMapper;

    static{
        objectMapper = new ObjectMapper();
        //允许未引用的字段名
        objectMapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        //忽略未知字段
        objectMapper.enable(JsonGenerator.Feature.IGNORE_UNKNOWN);
        //Bean为空不报异常
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public static <V> V from(InputStream inputStream, Class<V> c) {
        try {
            return objectMapper.readValue(inputStream, c);
        } catch (IOException e) {
            return null;
        }
    }

    public static <V> V from(InputStream inputStream, TypeReference<V> type) {
        try {
            return objectMapper.readValue(inputStream, type);
        } catch (IOException e) {
            return null;
        }
    }

    public static <V> V from(String json, Class<V> c) {
        try {
            return objectMapper.readValue(JsonSanitizer.sanitize(json), c);
        } catch (IOException e) {
            return null;
        }
    }

    public static <V> V from(String json, TypeReference<V> type) {
        try {
            return objectMapper.readValue(JsonSanitizer.sanitize(json), type);
        } catch (IOException e) {
            return null;
        }
    }

    public static <V> String to(V v) {
        try {
            return objectMapper.writeValueAsString(v);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}


