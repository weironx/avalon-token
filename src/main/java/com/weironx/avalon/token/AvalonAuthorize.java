package com.weironx.avalon.token;

import java.lang.annotation.*;

/**
 * 权限校验
 * 用户类型匹配 且 权限值或角色值匹配 则通过
 * @author weironx
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AvalonAuthorize {

    /**
     *  权限值 匹配则通过
     * @return
     */
    String[] authorities() default {};

    /**
     * 角色值 匹配则通过
     * @return
     */
    String[] roles() default {};

    /**
     * 用户类型 不匹配则拒绝
     * @return
     */
    String[] userTypes() default {};
}
