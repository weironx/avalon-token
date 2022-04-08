package com.weironx.avalon.token;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author weironx
 */
public class AvalonContextHolder {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = new ThreadLocal<>();

    public static void put(String key, Object value) {
        Map<String, Object> map = CONTEXT.get();
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(key, value);
        CONTEXT.set(map);
    }

    public static void put(KeyEnum key, Object value) {
        put(key.getKey(),value);
    }

    public static <T> T get(String key) {
        Map<String, Object> map = CONTEXT.get();
        return (T) map.get(key);
    }

    public static <T> T get(KeyEnum key) {
        return get(key.getKey());
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static String getId() {
        return get(KeyEnum.ID);
    }

    public static Long getIdAsLong() {
        String id = getId();
        if (id == null || id.trim().length() < 1) {
            return null;
        }
        return Long.valueOf(id);
    }


    public static Integer getIdAsInt() {
        String id = getId();
        if (id == null || id.trim().length() < 1) {
            return null;
        }
        return Integer.valueOf(id);
    }

    public static String getToken() {
        return get(KeyEnum.TOKEN);
    }

    public static String getExtend() {
        return get(KeyEnum.EXTEND);
    }

    public static String getUserType() {
        return get(KeyEnum.USER_TYPE);
    }

    public static Collection<String> getAuthorities() {
        return get(KeyEnum.AUTHORITIES);
    }

    public static Collection<String> getRoles() {
        return get(KeyEnum.ROLES);
    }

    public static HttpServletRequest getHttpServletRequest() {
        return get(KeyEnum.HTTP_SERVLET_REQUEST);
    }

    public static HttpServletResponse getHttpServletResponse() {
        return get(KeyEnum.HTTP_SERVLET_RESPONSE);
    }


    public enum KeyEnum {
        ID("id"),
        TOKEN("token"),
        EXTEND("extend"),
        USER_TYPE("userType"),
        AUTHORITIES("authorities"),
        ROLES("roles"),
        HTTP_SERVLET_REQUEST("HttpServletRequest"),
        HTTP_SERVLET_RESPONSE("HttpServletResponse"),

        ;

        private String key;

        KeyEnum(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
