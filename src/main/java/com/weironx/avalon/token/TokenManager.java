package com.weironx.avalon.token;

import java.util.LinkedHashMap;

/**
 * @author weironx
 */
public class TokenManager {

    private static final LinkedHashMap<String, TokenStore> tokenStoreMap = new LinkedHashMap<>();

    public static void register(String userType, TokenStore tokenStore) {
        tokenStoreMap.put(userType, tokenStore);
    }

    public static TokenStore get(String userType) {
        return tokenStoreMap.get(userType);
    }

    public static LinkedHashMap<String, TokenStore> getTokenStoreMap() {
        return tokenStoreMap;
    }
}
