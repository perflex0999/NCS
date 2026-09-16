package com.ncs.order.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String phone;
    private String code;
}
