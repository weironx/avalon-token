package com.weironx.avalon.token;

/**
 * @author weironx
 */
public interface SerializationStrategy {

    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);

    String serializeString(Object obj);

    <T> T deserializeString(String str, Class<T> clazz);
}
