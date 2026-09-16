package com.ncs.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.common.exception.BizException;
import com.ncs.order.dto.LoginRequest;
import com.ncs.order.dto.LoginResponse;
import com.ncs.order.entity.User;
import com.ncs.order.mapper.UserMapper;
import com.ncs.order.util.RedisKeys;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class AuthService {

    private static final String FIXED_CODE = "123456";

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    public AuthService(UserMapper userMapper, StringRedisTemplate redisTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    public LoginResponse login(LoginRequest req) {
        if (!FIXED_CODE.equals(req.getCode())) {
            throw new BizException("验证码错误");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (user == null) {
            user = new User();
            user.setPhone(req.getPhone());
            user.setNickname("用户" + req.getPhone().substring(req.getPhone().length() - 4));
            userMapper.insert(user);
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(RedisKeys.SESSION_PREFIX + token, String.valueOf(user.getId()), Duration.ofDays(7));

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        return resp;
    }
}
