# avalon-token
一个简单用于springboot权限认证的工具，使用redis用于存储。
##配置
```
@Configuration
public class TokenConfig {

    @Bean
    public AvalonTokenStore avalonTokenStore(StringRedisTemplate redisTemplate, Environment environment) {
        return new AvalonTokenStore(redisTemplate, environment);
    }

    @Bean
    public AvalonTokenFilter avalonTokenFilter() {
        return new AvalonTokenFilter();
    }

    @Bean
    public AvalonAuthorizeAspect avalonAuthorizeAspect() {
        return new AvalonAuthorizeAspect();
    }

}
```
不同用户类型使用不同配置
```
    @Bean
    public AvalonTokenStore userAvalonTokenStore(StringRedisTemplate redisTemplate, Environment environment) {
        AvalonTokenStore userAvalonTokenStore = new AvalonTokenStore(redisTemplate, environment);
        userAvalonTokenStore.getTokenConfig()
                .setUserType("other")
                .setRedisPrefix("other");
        return userAvalonTokenStore;
    }
```

application.yml
```
avalon:
  token:
    #token头部的key名 默认 Authorization
    header: Authorization
    #token前缀 默认 Bearer
    tokenPrefix: Bearer
    #token过期时间 默认 7200
    expiration: 7200
    #刷新token过期时间 默认 604800
    refreshExpiration: 604800
    #用户类型 默认 user
    userType: user
    #redis key前缀 默认 user
    redisPrefix: user
    #在多人登录同一账号时，是否共用一个token (为true时所有登录共用一个token, 为false时每次登录新建一个token) 默认 false
    share: false
    #是否允许同一账号并发登录 (为true时允许一起登录, 为false时新登录挤掉旧登录) 默认 true
    concurrent: true
    #是否生成刷新token 默认 true
    refresh: true
```
##使用
###接口使用
```
    /**
     * 需要鉴权的接口 使用 @AvalonAuthorize
     * @return
     */
    @PostMapping(value = "/pub/test")
    @AvalonAuthorize(roles = {"role"}, authorities = {"auth"})
    public CommonResult<Void> test() {
        return CommonResult.success();
    }
```
###代码使用
```
#根据id登录
TokenHelper.get().login("1");
#根据id、权限登录
TokenHelper.get().login("1", Arrays.asList("authorities"),"extend");
#根据id、权限、角色登录
TokenHelper.get().login("1", Arrays.asList("authorities"), Arrays.asList("roles"), "extend");
#根据token查询信息
TokenHelper.get().findByToken("token");
#判断登录
TokenHelper.get().isLogin();
#根据token退出
TokenHelper.get().logoutAccessToken("token");
#根据当前用户token退出
TokenHelper.get().logout();
#根据id退出
TokenHelper.get().logout("1");
#根据刷新token刷新
TokenHelper.get().refresh("refreshToken");
#获取当前用户id
TokenHelper.get().getId();
TokenHelper.get().getIdAsLong();
TokenHelper.get().getIdAsInt();
#获取当前用户token
TokenHelper.get().getToken();
#获取当前用户携带信息
TokenHelper.get().getExtend();
#获取当前用户类型
TokenHelper.get().getUserType();
#获取当前用户权限
TokenHelper.get().getAuthorities();
#获取当前用户角色
TokenHelper.get().getRoles();
#获取request
TokenHelper.get().getHttpServletRequest();
#获取response
TokenHelper.get().getHttpServletResponse();

#不同用户类型可以使用
TokenHelper.getByUserType("other").login("1");
```
