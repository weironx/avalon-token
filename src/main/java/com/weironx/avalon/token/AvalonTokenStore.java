package com.weironx.avalon.token;

import com.weironx.avalon.token.exception.LoginExpcetion;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author weironx
 */
public class AvalonTokenStore implements TokenStore {


    private AvalonTokenConfig avalonTokenConfig;

    private SerializationStrategy serializationStrategy;

    private StringRedisTemplate redisTemplate;

    private Environment environment;

    private TokenStrategy tokenStrategy;

    public AvalonTokenStore(StringRedisTemplate redisTemplate, Environment environment) {
        this.redisTemplate = redisTemplate;
        this.environment = environment;
        this.avalonTokenConfig = new AvalonTokenConfig();
        readEnv();
        this.serializationStrategy = new JsonSerializationStrategy();
        this.tokenStrategy = new UuidTokenStrategy();
        TokenManager.register(this.avalonTokenConfig.getUserType(), this);
    }


    public AvalonTokenStore(AvalonTokenConfig avalonTokenConfig, SerializationStrategy serializationStrategy, TokenStrategy tokenStrategy, StringRedisTemplate redisTemplate, Environment environment) {
        this.redisTemplate = redisTemplate;
        this.environment = environment;
        this.avalonTokenConfig = avalonTokenConfig;
        this.serializationStrategy = serializationStrategy;
        this.tokenStrategy = tokenStrategy;
        TokenManager.register(this.avalonTokenConfig.getUserType(), this);
    }

    private void readEnv() {
        if (this.environment == null) {
            return;
        }

        String header = this.environment.getProperty("avalon.token.header");
        if (header != null && header.trim().length() > 0) {
            this.avalonTokenConfig.setHeader(header.trim());
        }
        String tokenPrefix = this.environment.getProperty("avalon.token.tokenPrefix");
        if (tokenPrefix != null && tokenPrefix.trim().length() > 0) {
            this.avalonTokenConfig.setTokenPrefix(tokenPrefix.trim());
        }
        Long expiration = this.environment.getProperty("avalon.token.expiration", Long.class);
        if (expiration != null) {
            this.avalonTokenConfig.setExpiration(expiration);
        }
        Long refreshExpiration = this.environment.getProperty("avalon.token.refreshExpiration", Long.class);
        if (refreshExpiration != null) {
            this.avalonTokenConfig.setRefreshExpiration(refreshExpiration);
        }
        String userType = this.environment.getProperty("avalon.token.userType");
        if (userType != null && userType.trim().length() > 0) {
            this.avalonTokenConfig.setUserType(userType.trim());
        }
        String redisPrefix = this.environment.getProperty("avalon.token.redisPrefix");
        if (redisPrefix != null && redisPrefix.trim().length() > 0) {
            this.avalonTokenConfig.setRedisPrefix(redisPrefix.trim());
        }
        Boolean share = this.environment.getProperty("avalon.token.share", Boolean.class);
        if (share != null) {
            this.avalonTokenConfig.setShare(share);
        }
        Boolean concurrent = this.environment.getProperty("avalon.token.concurrent", Boolean.class);
        if (concurrent != null) {
            this.avalonTokenConfig.setConcurrent(concurrent);
        }
        Boolean refresh = this.environment.getProperty("avalon.token.refresh", Boolean.class);
        if (refresh != null) {
            this.avalonTokenConfig.setRefresh(refresh);
        }
    }

    public AvalonTokenStore(AvalonTokenConfig avalonTokenConfig) {
        this.avalonTokenConfig = avalonTokenConfig;
    }

    @Override
    public Map<String, Object> findByToken(String token) {
        String redisKey = getTokenConfig().getRedisPrefix() + ":access:" + token;
        if (redisTemplate.hasKey(redisKey)) {
            String val = redisTemplate.opsForValue().get(redisKey);
            Map<String, Object> map = serializationStrategy.deserializeString(val, Map.class);
            Map<String, Object> newMap = new HashMap<>(map.size());
            for (AvalonContextHolder.KeyEnum keyEnum : AvalonContextHolder.KeyEnum.values()) {
                Object obj = map.get(keyEnum.getKey());
                if (obj != null) {
                    newMap.put(keyEnum.getKey(), obj);
                }
            }
            return newMap;
        }
        return null;
    }

    @Override
    public AvalonTokenConfig getTokenConfig() {
        return avalonTokenConfig;
    }

    public void setTokenConfig(AvalonTokenConfig avalonTokenConfig) {
        this.avalonTokenConfig = avalonTokenConfig;
    }

    public SerializationStrategy getSerializationStrategy() {
        return serializationStrategy;
    }

    public void setSerializationStrategy(SerializationStrategy serializationStrategy) {
        this.serializationStrategy = serializationStrategy;
    }

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    @Override
    public Boolean isLogin() {
        String id = AvalonContextHolder.getId();
        return id != null && id.length() > 0;
    }

    @Override
    public AvalonToken login(String id) throws LoginExpcetion {
        return login(id, null, null, null);
    }

    @Override
    public AvalonToken login(String id, Collection<String> authorities, String extend) throws LoginExpcetion {
        return login(id, authorities, null, extend);
    }

    @Override
    public AvalonToken login(String id, Collection<String> authorities, Collection<String> roles, String extend) throws LoginExpcetion {
        String accessToken = null;
        Long expiration = null;
        String refreshToken = null;
        Long refreshExpiration = null;

        String idToAccessKey = this.avalonTokenConfig.getRedisPrefix() + ":idToAccess:" + id;
        String idToRefreshKey = this.avalonTokenConfig.getRedisPrefix() + ":idToRefresh:" + id;

        if (!this.avalonTokenConfig.getConcurrent()) {
            logout(id);
        }
        if (this.avalonTokenConfig.getShare()) {
            if (redisTemplate.hasKey(idToAccessKey)) {
                if (this.avalonTokenConfig.getShare()) {
                    Set<String> tokens = redisTemplate.opsForSet().members(idToAccessKey);
                    if (tokens != null && !tokens.isEmpty()) {
                        for (String t : tokens) {
                            String accessKey = this.avalonTokenConfig.getRedisPrefix() + ":access:" + t;
                            if (!redisTemplate.hasKey(accessKey)) {
                                redisTemplate.opsForSet().remove(idToAccessKey, t);
                            } else {
                                accessToken = t;
                                expiration = redisTemplate.getExpire(accessKey, TimeUnit.SECONDS);
                                break;
                            }
                        }
                    }
                }
            }
            if (this.avalonTokenConfig.getRefresh()) {
                if (accessToken != null) {
                    String refreshKey = this.avalonTokenConfig.getRedisPrefix() + ":accessToRefresh:" + accessToken;
                    if (redisTemplate.hasKey(refreshKey)) {
                        refreshToken = redisTemplate.opsForValue().get(refreshKey);
                        refreshExpiration = redisTemplate.getExpire(refreshKey, TimeUnit.SECONDS);
                    } else {
                        logout(id);
                        accessToken = null;
                        expiration = null;
                    }
                }
            }
        }


        if (accessToken == null) {
            accessToken = tokenStrategy.generate();
            String accessKey = this.getTokenConfig().getRedisPrefix() + ":access:" + accessToken;
            int count = 0;
            while (redisTemplate.hasKey(accessKey) && count++ < 10) {
                accessToken = tokenStrategy.generate();
                accessKey = this.getTokenConfig().getRedisPrefix() + ":access:" + accessToken;
            }
            if (count >= 10) {
                throw new LoginExpcetion("token生成重复");
            }
            Map<String, Object> accessMap = new HashMap<>();
            accessMap.put(AvalonContextHolder.KeyEnum.ID.getKey(), id);
            accessMap.put(AvalonContextHolder.KeyEnum.USER_TYPE.getKey(), this.avalonTokenConfig.getUserType());
            if (authorities != null && !authorities.isEmpty()) {
                accessMap.put(AvalonContextHolder.KeyEnum.AUTHORITIES.getKey(), authorities);
            }
            if (roles != null && !roles.isEmpty()) {
                accessMap.put(AvalonContextHolder.KeyEnum.ROLES.getKey(), roles);
            }
            if (extend != null) {
                accessMap.put(AvalonContextHolder.KeyEnum.EXTEND.getKey(), extend);
            }
            accessMap.put("timestamp", System.currentTimeMillis());
            expiration = avalonTokenConfig.getExpiration();
            redisTemplate.opsForValue().set(accessKey, this.serializationStrategy.serializeString(accessMap), expiration, TimeUnit.SECONDS);
            redisTemplate.opsForSet().add(idToAccessKey, accessToken);
            redisTemplate.expire(idToAccessKey, avalonTokenConfig.getExpiration(), TimeUnit.SECONDS);
        }
        if (avalonTokenConfig.getRefresh()) {
            if (refreshExpiration == null) {
                refreshToken = tokenStrategy.generate();
                String refreshKey = this.getTokenConfig().getRedisPrefix() + ":refresh:" + refreshToken;
                int count = 0;
                while (redisTemplate.hasKey(refreshKey) && count++ < 10) {
                    refreshToken = tokenStrategy.generate();
                    refreshKey = this.getTokenConfig().getRedisPrefix() + ":refresh:" + refreshToken;
                }
                if (count >= 10) {
                    throw new LoginExpcetion("token生成重复");
                }
                Map<String, Object> refreshMap = new HashMap<>();
                refreshMap.put(AvalonContextHolder.KeyEnum.ID.getKey(), id);
                refreshMap.put(AvalonContextHolder.KeyEnum.USER_TYPE.getKey(), this.avalonTokenConfig.getUserType());
                if (authorities != null && !authorities.isEmpty()) {
                    refreshMap.put(AvalonContextHolder.KeyEnum.AUTHORITIES.getKey(), authorities);
                }
                if (extend != null) {
                    refreshMap.put(AvalonContextHolder.KeyEnum.EXTEND.getKey(), extend);
                }
                refreshMap.put("timestamp", System.currentTimeMillis());
                refreshExpiration = avalonTokenConfig.getRefreshExpiration();
                redisTemplate.opsForValue().set(refreshKey, this.serializationStrategy.serializeString(refreshMap), refreshExpiration, TimeUnit.SECONDS);
                redisTemplate.opsForSet().add(idToRefreshKey, refreshToken);
                redisTemplate.expire(idToRefreshKey, avalonTokenConfig.getRefreshExpiration(), TimeUnit.SECONDS);

                String accessToRefreshKey = avalonTokenConfig.getRedisPrefix() + ":accessToRefresh:" + accessToken;
                String refreshToAccessKey = avalonTokenConfig.getRedisPrefix() + ":refreshToAccess:" + refreshToken;
                redisTemplate.opsForValue().set(accessToRefreshKey, refreshToken, expiration, TimeUnit.SECONDS);
                redisTemplate.opsForValue().set(refreshToAccessKey, accessToken, refreshExpiration, TimeUnit.SECONDS);
            }
        }

        AvalonToken token = new AvalonToken(accessToken, expiration, refreshToken, refreshExpiration, this.avalonTokenConfig.getUserType(), avalonTokenConfig.getTokenPrefix());
        return token;
    }

    /**
     * 退出token和refreshToken 不影响其他token
     *
     * @param accessToken
     */
    @Override
    public void logoutAccessToken(String accessToken) {
        String accessKey = this.avalonTokenConfig.getRedisPrefix() + ":access:" + accessToken;
        if (redisTemplate.hasKey(accessKey)) {
            redisTemplate.delete(accessKey);

            String val = redisTemplate.opsForValue().get(accessKey);
            Map map = this.serializationStrategy.deserializeString(val, Map.class);
            String id = map.get(AvalonContextHolder.KeyEnum.ID.getKey()).toString();

            String idToAccessKey = this.avalonTokenConfig.getRedisPrefix() + ":idToAccess:" + id;
            if (redisTemplate.hasKey(idToAccessKey)) {
                redisTemplate.opsForSet().remove(idToAccessKey, accessToken);
            }

            String accessToRefreshKey = this.avalonTokenConfig.getRedisPrefix() + ":accessToRefresh:" + accessToken;
            if (redisTemplate.hasKey(accessToRefreshKey)) {
                String refreshToken = redisTemplate.opsForValue().get(accessToRefreshKey);
                String refreshKey = this.avalonTokenConfig.getRedisPrefix() + ":refresh:" + refreshToken;
                String refreshToAccessKey = this.avalonTokenConfig.getRedisPrefix() + ":refreshToAccess:" + refreshToken;
                redisTemplate.delete(Arrays.asList(accessToRefreshKey, refreshKey, refreshToAccessKey));

                String idToRefreshKey = this.avalonTokenConfig.getRedisPrefix() + ":idToRefresh:" + id;
                redisTemplate.opsForSet().remove(idToRefreshKey, refreshToken);
            }
        }
    }

    @Override
    public void logout() {
        if (isLogin()) {
            logout(AvalonContextHolder.getId());
        }
    }

    @Override
    public void logout(String id) {
        String idToAccessKey = this.avalonTokenConfig.getRedisPrefix() + ":idToAccess:" + id;
        String idToRefreshKey = this.avalonTokenConfig.getRedisPrefix() + ":idToRefresh:" + id;
        if (redisTemplate.hasKey(idToAccessKey)) {
            Set<String> tokens = redisTemplate.opsForSet().members(idToAccessKey);
            if (tokens != null && !tokens.isEmpty()) {
                Set<String> accessSet = tokens.stream().map(t -> this.avalonTokenConfig.getRedisPrefix() + ":access:" + t).collect(Collectors.toSet());
                redisTemplate.delete(accessSet);
                Set<String> accessToRefreshSet = tokens.stream().map(t -> this.avalonTokenConfig.getRedisPrefix() + ":accessToRefresh:" + t).collect(Collectors.toSet());
                redisTemplate.delete(accessToRefreshSet);
            }
            redisTemplate.delete(idToAccessKey);
        }
        if (redisTemplate.hasKey(idToRefreshKey)) {
            Set<String> tokens = redisTemplate.opsForSet().members(idToRefreshKey);
            if (tokens != null && !tokens.isEmpty()) {
                Set<String> refreshSet = tokens.stream().map(t -> this.avalonTokenConfig.getRedisPrefix() + ":refresh:" + t).collect(Collectors.toSet());
                redisTemplate.delete(refreshSet);
                Set<String> refreshToAccessSet = tokens.stream().map(t -> this.avalonTokenConfig.getRedisPrefix() + ":refreshToAccess:" + t).collect(Collectors.toSet());
                redisTemplate.delete(refreshToAccessSet);
            }
            redisTemplate.delete(idToRefreshKey);
        }
    }


    @Override
    public AvalonToken refresh(String refreshToken) throws LoginExpcetion {
        String refreshKey = this.avalonTokenConfig.getRedisPrefix() + ":refresh:" + refreshToken;
        if (!redisTemplate.hasKey(refreshKey)) {
            throw new LoginExpcetion("用户未登录");
        }
        String val = redisTemplate.opsForValue().get(refreshKey);
        Map<String, Object> map = this.serializationStrategy.deserializeString(val, Map.class);
        String id = map.get(AvalonContextHolder.KeyEnum.ID.getKey()).toString();
        if (id == null || id.length() == 0) {
            throw new LoginExpcetion("用户未登录");
        }

        String accessToken = tokenStrategy.generate();
        String accessKey = this.getTokenConfig().getRedisPrefix() + ":access:" + accessToken;
        int count = 0;
        while (redisTemplate.hasKey(accessKey) && count++ < 10) {
            accessToken = tokenStrategy.generate();
            accessKey = this.getTokenConfig().getRedisPrefix() + ":access:" + accessToken;
        }
        if (count >= 10) {
            throw new LoginExpcetion("token生成重复");
        }
        String idToAccessKey = this.getTokenConfig().getRedisPrefix() + ":idToAccess:" + id;
        map.put("timestamp", System.currentTimeMillis());
        Long expiration = avalonTokenConfig.getExpiration();

        Long oldRefreshExpiration = redisTemplate.getExpire(refreshKey);
        expiration = expiration > oldRefreshExpiration ? oldRefreshExpiration : expiration;

        redisTemplate.opsForValue().set(accessKey, this.serializationStrategy.serializeString(map), expiration, TimeUnit.SECONDS);
        redisTemplate.opsForSet().add(idToAccessKey, accessToken);

        Long oldIdToAccessExpiration = redisTemplate.getExpire(idToAccessKey);
        Long idToAccessExpiration = expiration > oldIdToAccessExpiration ? expiration : oldIdToAccessExpiration;
        redisTemplate.expire(idToAccessKey, idToAccessExpiration, TimeUnit.SECONDS);

        AvalonToken avalonToken = new AvalonToken(accessToken, expiration, refreshToken, oldRefreshExpiration, this.avalonTokenConfig.getUserType(), this.avalonTokenConfig.getTokenPrefix());
        return avalonToken;
    }

    private void clearExpireToken(String id) {
        String idToAccessKey = this.avalonTokenConfig.getRedisPrefix() + ":idToAccess:" + id;
        String idToRefreshKey = this.avalonTokenConfig.getRedisPrefix() + ":idToRefresh:" + id;

        if (redisTemplate.hasKey(idToAccessKey)) {
            Set<String> tokens = redisTemplate.opsForSet().members(idToAccessKey);
            if (tokens != null && !tokens.isEmpty()) {
                for (String t : tokens) {
                    String accessKey = this.avalonTokenConfig.getRedisPrefix() + ":access:" + t;
                    String accessToRefreshKey = this.avalonTokenConfig.getRedisPrefix() + ":accessToRefresh:" + t;
                    redisTemplate.delete(Arrays.asList(accessKey, accessToRefreshKey));
                    redisTemplate.opsForSet().remove(idToAccessKey, t);
                }
            }
        }


        if (redisTemplate.hasKey(idToRefreshKey)) {
            Set<String> tokens = redisTemplate.opsForSet().members(idToRefreshKey);
            if (tokens != null && !tokens.isEmpty()) {
                for (String t : tokens) {
                    String refreshKey = this.avalonTokenConfig.getRedisPrefix() + ":refresh:" + t;
                    String refreshToAccessKey = this.avalonTokenConfig.getRedisPrefix() + ":refreshToAccess:" + t;
                    redisTemplate.delete(Arrays.asList(refreshKey, refreshToAccessKey));
                    redisTemplate.opsForSet().remove(idToRefreshKey, t);
                }
            }
        }
    }
}
