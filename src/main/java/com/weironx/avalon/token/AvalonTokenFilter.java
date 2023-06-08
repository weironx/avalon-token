package com.weironx.avalon.token;

import org.springframework.core.annotation.Order;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


/**
 * @author weironx
 */
@Order(AvalonTokenConstant.ORDER)
public class AvalonTokenFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            AvalonContextHolder.put(AvalonContextHolder.KeyEnum.HTTP_SERVLET_REQUEST, request);
            AvalonContextHolder.put(AvalonContextHolder.KeyEnum.HTTP_SERVLET_RESPONSE, response);

            for (Map.Entry<String, TokenStore> entry : TokenManager.getTokenStoreMap().entrySet()) {
                TokenStore tokenStore = entry.getValue();
                String header = request.getHeader(tokenStore.getTokenConfig().getHeader());
                if (header != null && header.startsWith(tokenStore.getTokenConfig().getTokenPrefix()) && header.length() > (tokenStore.getTokenConfig().getTokenPrefix().length() + 1)) {
                    String token = header.substring(tokenStore.getTokenConfig().getTokenPrefix().length() + 1);
                    if (token != null && !token.isEmpty()) {
                        AvalonContextHolder.put(AvalonContextHolder.KeyEnum.TOKEN, token);
                    }
                    Map<String, Object> map = tokenStore.findByToken(token);
                    if (map != null && !map.isEmpty()) {
                        for (Map.Entry<String, Object> contextEntry : map.entrySet()) {
                            AvalonContextHolder.put(contextEntry.getKey(), contextEntry.getValue());
                        }
                        break;
                    }

                }
            }

            chain.doFilter(request, response);
        } finally {
            AvalonContextHolder.clear();
        }
    }

    @Override
    public void destroy() {

    }
}
