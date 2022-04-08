package com.weironx.avalon.token;

/**
 * @author weironx
 */
public class AvalonToken {

    /**
     * 访问token
     */
    private String accessToken;

    /**
     * token过期时间
     */
    private Long expiration;

    /**
     * 刷新token
     */
    private String refreshToken;

    /**
     * 刷新token过期时间
     */
    private Long refreshExpiration;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * token前缀
     */
    private String tokenPrefix;


    public AvalonToken(String accessToken, Long expiration, String refreshToken, Long refreshExpiration, String userType,String tokenPrefix) {
        this.accessToken = accessToken;
        this.expiration = expiration;
        this.refreshToken = refreshToken;
        this.refreshExpiration = refreshExpiration;
        this.userType = userType;
        this.tokenPrefix = tokenPrefix;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Long getExpiration() {
        return expiration;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }

    public String getUserType() {
        return userType;
    }
}
