package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.dto.ReservationDTO;
import com.library.entity.Reservation;
import com.library.vo.ReservationVO;

/**
 * 预约服务接口
 */
public interface ReservationService extends IService<Reservation> {
    
    /**
     * 创建预约
     */
    Reservation createReservation(Long userId, ReservationDTO reservationDTO);
    
    /**
     * 取消预约
     */
    void cancelReservation(Long reservationId, Long userId);
    
    /**
     * 管理员取消预约
     */
    void adminCancelReservation(Long reservationId);
    
    /**
     * 扫码签到
     */
    void checkIn(String orderNo);
    
    /**
     * 分页查询预约记录
     */
    IPage<ReservationVO> getReservationPage(int current, int size, Long userId, String status);
    
    /**
     * 根据流水号查询预约记录
     */
    Reservation getByOrderNo(String orderNo);
    
    /**
     * 根据流水号查询预约记录详情
     */
    ReservationVO getReservationDetailByOrderNo(String orderNo);
    
    /**
     * 处理过期预约（定时任务调用）
     */
    void handleExpiredReservations();
}