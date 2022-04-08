package com.weironx.avalon.token;

import com.weironx.avalon.token.exception.LoginExpcetion;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author weironx
 */
public class TokenHelper implements TokenStore {

    private static final Map<String, TokenHelper> tokenHelperMap = new HashMap<>();

    public static TokenHelper get() {
        if (DEFAULT_USER_TYPE == null) {
            DEFAULT_USER_TYPE = TokenManager.getTokenStoreMap().keySet().stream().findFirst().orElse(null);
        }
        return getByUserType(DEFAULT_USER_TYPE);
    }

    public static TokenHelper getByUserType(String userType) {
        if (userType == null) {
            return null;
        }
        if (tokenHelperMap.containsKey(userType)) {
            return tokenHelperMap.get(userType);
        }
        synchronized (TokenHelper.class) {
            if (tokenHelperMap.containsKey(userType)) {
                return tokenHelperMap.get(userType);
            }
            TokenHelper tokenHelper = new TokenHelper(TokenManager.get(userType));
            tokenHelperMap.put(userType, tokenHelper);
            return tokenHelper;
        }
    }

    private static String DEFAULT_USER_TYPE;

    private TokenStore tokenStore;

    private TokenHelper(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public Map<String, Object> findByToken(String token) {
        return tokenStore.findByToken(token);
    }

    @Override
    public AvalonTokenConfig getTokenConfig() {
        return tokenStore.getTokenConfig();
    }

    @Override
    public Boolean isLogin() {
        return tokenStore.isLogin();
    }

    @Override
    public AvalonToken login(String id) throws LoginExpcetion {
        return tokenStore.login(id);
    }

    @Override
    public AvalonToken login(String id, Collection<String> authorities, String extend) throws LoginExpcetion {
        return tokenStore.login(id, authorities, extend);
    }

    @Override
    public AvalonToken login(String id, Collection<String> authorities, Collection<String> roles, String extend) throws LoginExpcetion {
        return tokenStore.login(id, authorities, roles, extend);
    }

    @Override
    public void logoutAccessToken(String accessToken) {
        tokenStore.logoutAccessToken(accessToken);
    }

    @Override
    public void logout() {
        tokenStore.logout();
    }

    @Override
    public void logout(String id) {
        tokenStore.logout(id);
    }

    @Override
    public AvalonToken refresh(String refreshToken) throws LoginExpcetion {
        return tokenStore.refresh(refreshToken);
    }

    public String getId() {
        return AvalonContextHolder.getId();
    }

    public Long getIdAsLong() {
        return AvalonContextHolder.getIdAsLong();
    }


    public Integer getIdAsInt() {
        return AvalonContextHolder.getIdAsInt();
    }

    public String getToken() {
        return AvalonContextHolder.getToken();
    }

    public String getExtend() {
        return AvalonContextHolder.getExtend();
    }

    public String getUserType() {
        return AvalonContextHolder.getUserType();
    }

    public Collection<String> getAuthorities() {
        return AvalonContextHolder.getAuthorities();
    }

    public Collection<String> getRoles() {
        return AvalonContextHolder.getRoles();
    }

    public HttpServletRequest getHttpServletRequest() {
        return AvalonContextHolder.getHttpServletRequest();
    }

    public HttpServletResponse getHttpServletResponse() {
        return AvalonContextHolder.getHttpServletResponse();
    }
}
