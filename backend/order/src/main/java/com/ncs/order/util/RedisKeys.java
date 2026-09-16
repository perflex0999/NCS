package com.ncs.order.util;

/**
 * Redis Key 常量
 */
public final class RedisKeys {

    private RedisKeys() {
    }

    /** 登录会话 token -> userId */
    public static final String SESSION_PREFIX = "ncs:session:";

    /** 设备充电分布式锁 key */
    public static final String DEVICE_LOCK_PREFIX = "ncs:lock:device:";
}
