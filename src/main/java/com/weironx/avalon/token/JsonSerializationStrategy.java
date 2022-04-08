package com.weironx.avalon.token;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;


/**
 * @author weironx
 */
public class JsonSerializationStrategy implements SerializationStrategy {

    private static Logger log = LoggerFactory.getLogger(JsonSerializationStrategy.class);

    public static ObjectMapper mapper;

    /**
     * 序列化级别，默认只序列化不为空的字段
     */
    protected static final JsonInclude.Include DEFAULT_PROPERTY_INCLUSION = JsonInclude.Include.NON_NULL;

    /**
     * 是否缩进JSON格式
     */
    protected static final boolean IS_ENABLE_INDENT_OUTPUT = false;

    static {
        try {
            //初始化
            mapper = new ObjectMapper();
            //配置序列化级别
            mapper.setSerializationInclusion(DEFAULT_PROPERTY_INCLUSION);
            //配置JSON缩进支持
            mapper.configure(SerializationFeature.INDENT_OUTPUT, IS_ENABLE_INDENT_OUTPUT);
        } catch (Exception e) {
            log.error("jackson mapper init error", e);
        }
    }

    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return new byte[0];
        }

        try {
            return mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Could not serialize: " + ex.getMessage(), ex);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            String s = new String(bytes, "utf-8");
            return mapper.readValue(s, clazz);
        } catch (JsonProcessingException je) {
            throw new RuntimeException("Could not deserialize: " + je.getMessage(), je);
        } catch (UnsupportedEncodingException ue) {
            throw new RuntimeException("Could not deserialize: " + ue.getMessage(), ue);
        }
    }

    @Override
    public String serializeString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException je) {
            throw new RuntimeException("Could not serialize: " + je.getMessage(), je);
        }
    }

    @Override
    public <T> T deserializeString(String str, Class<T> clazz) {
        if (str == null || str.trim().length() < 1) {
            return null;
        }
        try {
            return mapper.readValue(str, clazz);
        } catch (JsonProcessingException je) {
            throw new RuntimeException("Could not deserialize: " + je.getMessage(), je);
        }
    }

}
