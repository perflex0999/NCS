package com.ncs.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.common.exception.BizException;
import com.ncs.order.entity.Reservation;
import com.ncs.order.mapper.ReservationMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 充电桩预约服务：预约锁桩 30 分钟，超时未充电则锁定 24 小时
 */
@Service
public class ReserveService {

    private final ReservationMapper reservationMapper;

    public ReserveService(ReservationMapper reservationMapper) {
        this.reservationMapper = reservationMapper;
    }

    /**
     * 预约：锁桩 30 分钟
     */
    public Reservation reserve(Long userId, String deviceNo) {
        checkPenalty(userId);

        LocalDateTime now = LocalDateTime.now();
        // 设备是否已被他人预约
        Reservation active = reservationMapper.selectOne(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getDeviceNo, deviceNo)
                .eq(Reservation::getStatus, Reservation.STATUS_ACTIVE)
                .gt(Reservation::getExpireTime, now)
                .last("LIMIT 1"));
        if (active != null) {
            throw new BizException("该充电桩已被预约，请选择其他桩");
        }

        Reservation r = new Reservation();
        r.setDeviceNo(deviceNo);
        r.setUserId(userId);
        r.setReserveTime(now);
        r.setExpireTime(now.plusMinutes(30));
        r.setStatus(Reservation.STATUS_ACTIVE);
        reservationMapper.insert(r);
        return r;
    }

    /**
     * 充电前校验：① 是否被锁(24h) ② 桩是否被他人预约
     * 若该用户本人已预约此桩，则标记为已使用
     */
    public void checkAndUse(Long userId, String deviceNo) {
        checkPenalty(userId);

        LocalDateTime now = LocalDateTime.now();
        Reservation active = reservationMapper.selectOne(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getDeviceNo, deviceNo)
                .eq(Reservation::getStatus, Reservation.STATUS_ACTIVE)
                .gt(Reservation::getExpireTime, now)
                .last("LIMIT 1"));
        if (active != null && !active.getUserId().equals(userId)) {
            throw new BizException("该充电桩已被他人预约");
        }
        if (active != null && active.getUserId().equals(userId)) {
            active.setStatus(Reservation.STATUS_USED);
            reservationMapper.updateById(active);
        }
    }

    /**
     * 惩罚校验：24 小时内有「预约超时未充电」的记录则锁定
     */
    private void checkPenalty(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayAgo = now.minusHours(24);
        Long expired = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .eq(Reservation::getStatus, Reservation.STATUS_ACTIVE)
                .lt(Reservation::getExpireTime, now)
                .ge(Reservation::getExpireTime, dayAgo));
        if (expired != null && expired > 0) {
            throw new BizException("您有预约超时未充电，已锁定 24 小时");
        }
    }
}
