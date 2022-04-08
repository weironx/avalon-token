package com.weironx.avalon.token;

import com.weironx.avalon.token.exception.LoginExpcetion;

import java.util.Collection;
import java.util.Map;

/**
 * @author weironx
 */
public interface TokenStore {

    Map<String, Object> findByToken(String token);

    AvalonTokenConfig getTokenConfig();

    Boolean isLogin();

    AvalonToken login(String id) throws LoginExpcetion;

    AvalonToken login(String id, Collection<String> authorities, String extend) throws LoginExpcetion;

    AvalonToken login(String id, Collection<String> authorities, Collection<String> roles, String extend) throws LoginExpcetion;

    void logoutAccessToken(String accessToken);

    void logout();

    void logout(String id);

    AvalonToken refresh(String refreshToken) throws LoginExpcetion;
}
