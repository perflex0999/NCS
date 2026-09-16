package com.ncs.order.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private Long userId;
    private String phone;
    private String nickname;
}
