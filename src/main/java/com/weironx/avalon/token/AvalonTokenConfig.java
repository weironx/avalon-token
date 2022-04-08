package com.weironx.avalon.token;


/**
 * @author weironx
 */
public class AvalonTokenConfig {

    /**
     * token头部的key名
     * 默认 Authorization
     */
    private String header;

    /**
     * token前缀
     * 默认 Bearer
     */
    private String tokenPrefix;

    /**
     * token过期时间
     * 默认 7200
     */
    private Long expiration;

    /**
     * 刷新token过期时间
     * 默认 604800
     */
    private Long refreshExpiration;

    /**
     * 用户类型
     * 默认 user
     */
    private String userType;

    /**
     * redis key前缀
     * 默认 user
     */
    private String redisPrefix;

    /**
     * 在多人登录同一账号时，是否共用一个token (为true时所有登录共用一个token, 为false时每次登录新建一个token)
     * 默认 false
     */
    private Boolean share;

    /**
     * 是否允许同一账号并发登录 (为true时允许一起登录, 为false时新登录挤掉旧登录)
     * 默认 true
     */
    private Boolean concurrent;

    /**
     * 是否生成刷新token
     * 默认 true
     */
    private Boolean refresh;

    public AvalonTokenConfig() {
        this.header = "Authorization";
        this.tokenPrefix = "Bearer";
        this.expiration = 7200L;
        this.refreshExpiration = 604800L;
        this.userType = "user";
        this.redisPrefix = "user";
        this.share = false;
        this.concurrent = true;
        this.refresh = true;
    }

    public String getHeader() {
        return header;
    }

    public AvalonTokenConfig setHeader(String header) {
        this.header = header;
        return this;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public AvalonTokenConfig setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
        return this;
    }

    public Long getExpiration() {
        return expiration;
    }

    public AvalonTokenConfig setExpiration(Long expiration) {
        this.expiration = expiration;
        return this;
    }

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }

    public AvalonTokenConfig setRefreshExpiration(Long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
        return this;
    }

    public String getUserType() {
        return userType;
    }

    public AvalonTokenConfig setUserType(String userType) {
        this.userType = userType;
        return this;
    }

    public String getRedisPrefix() {
        return redisPrefix;
    }

    public AvalonTokenConfig setRedisPrefix(String redisPrefix) {
        this.redisPrefix = redisPrefix;
        return this;
    }

    public Boolean getShare() {
        return share;
    }

    public AvalonTokenConfig setShare(Boolean share) {
        this.share = share;
        return this;
    }

    public Boolean getConcurrent() {
        return concurrent;
    }

    public AvalonTokenConfig setConcurrent(Boolean concurrent) {
        this.concurrent = concurrent;
        return this;
    }

    public Boolean getRefresh() {
        return refresh;
    }

    public AvalonTokenConfig setRefresh(Boolean refresh) {
        this.refresh = refresh;
        return this;
    }
}
