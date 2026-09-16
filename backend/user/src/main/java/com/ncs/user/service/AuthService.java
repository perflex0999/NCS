package com.ncs.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.common.exception.BizException;
import com.ncs.common.util.JwtUtil;
import com.ncs.user.dto.LoginRequest;
import com.ncs.user.dto.LoginResponse;
import com.ncs.user.entity.User;
import com.ncs.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String FIXED_CODE = "123456";

    private final UserMapper userMapper;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
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

        String token = JwtUtil.generate(user.getId(), user.getPhone());

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        return resp;
    }
}
