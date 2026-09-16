package com.ncs.order.web;

import com.ncs.common.exception.BizException;
import com.ncs.order.util.RedisKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录鉴权拦截器：从 Authorization 头读取 token，解析 userId 放入 ThreadLocal
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    public AuthInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) {
            throw new BizException(401, "未登录");
        }
        String userId = redisTemplate.opsForValue().get(RedisKeys.SESSION_PREFIX + token);
        if (userId == null) {
            throw new BizException(401, "登录已过期，请重新登录");
        }
        UserContext.set(Long.valueOf(userId));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
