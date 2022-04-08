package com.weironx.avalon.token;

import com.weironx.avalon.token.exception.AuthorizeException;
import com.weironx.avalon.token.exception.LoginExpcetion;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;

/**
 * @author weironx
 */
@Aspect
@Order(AvalonTokenConstant.ORDER)
public class AvalonAuthorizeAspect {

    @Pointcut("@annotation(com.weironx.avalon.token.AvalonAuthorize)")
    public void webLog() {
    }

    @Around("webLog()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        String id = AvalonContextHolder.getId();
        if (id == null || id.trim().length() <= 0) {
            throw new LoginExpcetion("用户未登录");
        }
        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        AvalonAuthorize annotation = method.getAnnotation(AvalonAuthorize.class);
        if (annotation != null) {
            boolean fail = false;
            String[] userTypes = annotation.userTypes();
            if (userTypes != null && userTypes.length > 0) {
                fail = true;
                for (String userType : userTypes) {
                    if (Objects.equals(userType, AvalonContextHolder.getUserType())) {
                        fail = false;
                        break;
                    }
                }
                if (fail) {
                    throw new AuthorizeException("用户类型错误，禁止访问");
                }
                fail = false;
            }
            String[] methodAuthorities = annotation.authorities();
            if (methodAuthorities != null && methodAuthorities.length > 0) {
                Collection<String> userAuthorities = AvalonContextHolder.getAuthorities();
                if (userAuthorities != null && userAuthorities.size() > 0) {
                    for (String methodAuthority : methodAuthorities) {
                        if (userAuthorities.contains(methodAuthority)) {
                            return point.proceed();
                        }
                    }
                }
                fail = true;
            }
            String[] methodRoles = annotation.roles();
            if (methodRoles != null && methodRoles.length > 0) {
                Collection<String> userRoles = AvalonContextHolder.getRoles();
                if (userRoles != null && userRoles.size() > 0) {
                    for (String methodRole : methodRoles) {
                        if (userRoles.contains(methodRole)) {
                            return point.proceed();
                        }
                    }
                }
                fail = true;
            }
            if (fail) {
                throw new AuthorizeException("用户无权限，禁止访问");
            }
        }
        return point.proceed();
    }


}
