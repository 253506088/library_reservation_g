package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.dto.ReservationDTO;
import com.library.entity.Reservation;
import com.library.entity.SystemConfig;
import com.library.mapper.ReservationMapper;
import com.library.service.ReservationService;
import com.library.service.SystemConfigService;
import com.library.utils.OrderNoUtils;
import com.library.vo.PageResult;
import com.library.vo.ReservationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约服务实现类
 */
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {
    
    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Override
    @Transactional
    public Reservation createReservation(Long userId, ReservationDTO reservationDTO) {
        // 验证预约时间
        LocalDateTime now = LocalDateTime.now();
        if (reservationDTO.getStartTime().isBefore(now)) {
            throw new RuntimeException("预约时间不能是过去时间");
        }
        
        if (reservationDTO.getEndTime().isBefore(reservationDTO.getStartTime())) {
            throw new RuntimeException("结束时间不能早于开始时间");
        }
        
        // 检查用户时间冲突
        int userConflict = baseMapper.checkUserTimeConflict(
                userId, reservationDTO.getStartTime(), reservationDTO.getEndTime(), null);
        if (userConflict > 0) {
            throw new RuntimeException("您在该时间段已有预约，不能重复预约");
        }
        
        // 检查座位时间冲突
        int seatConflict = baseMapper.checkSeatTimeConflict(
                reservationDTO.getSeatId(), reservationDTO.getStartTime(), reservationDTO.getEndTime(), null);
        if (seatConflict > 0) {
            throw new RuntimeException("该座位在指定时间段已被预约");
        }
        
        // 生成流水号
        String orderNo = generateOrderNo();
        
        // 创建预约记录
        Reservation reservation = new Reservation();
        reservation.setOrderNo(orderNo);
        reservation.setUserId(userId);
        reservation.setLibraryId(reservationDTO.getLibraryId());
        reservation.setSeatId(reservationDTO.getSeatId());
        reservation.setStartTime(reservationDTO.getStartTime());
        reservation.setEndTime(reservationDTO.getEndTime());
        reservation.setStatus("已预约");
        reservation.setQrCode(orderNo); // 二维码内容就是流水号
        
        save(reservation);
        
        return reservation;
    }
    
    @Override
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("只能取消自己的预约");
        }
        
        if (!"已预约".equals(reservation.getStatus())) {
            throw new RuntimeException("只能取消未使用的预约");
        }
        
        reservation.setStatus("已取消");
        updateById(reservation);
    }
    
    @Override
    public void adminCancelReservation(Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        if (!"已预约".equals(reservation.getStatus())) {
            throw new RuntimeException("只能取消未使用的预约");
        }
        
        reservation.setStatus("已取消");
        updateById(reservation);
    }
    
    @Override
    public void checkIn(String orderNo) {
        Reservation reservation = getByOrderNo(orderNo);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        if (!"已预约".equals(reservation.getStatus())) {
            throw new RuntimeException("该预约已失效");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 检查是否已过期（超过结束时间）
        if (now.isAfter(reservation.getEndTime())) {
            throw new RuntimeException("预约已过期，无法签到");
        }
        
        // 允许迟到，但不能超过结束时间
        reservation.setStatus("已使用");
        reservation.setCheckInTime(now);
        updateById(reservation);
    }
    
    @Override
    public IPage<ReservationVO> getReservationPage(int current, int size, Long userId, String status) {
        Page<ReservationVO> page = new Page<>(current, size);
        return baseMapper.selectReservationPage(page, userId, status);
    }
    
    @Override
    public PageResult<ReservationVO> getReservationPageQuery(int current, int size, Long userId, String status, 
                                                            String userName, Long libraryId, String seatNumber) {
        // 计算偏移量
        int offset = (current - 1) * size;
        
        // 查询数据
        List<ReservationVO> records = baseMapper.selectReservationPageWithCondition(
            userId, status, userName, libraryId, seatNumber, offset, size);
        
        // 查询总数
        long total = baseMapper.countReservationWithCondition(userId, status, userName, libraryId, seatNumber);
        
        // 返回分页结果
        return new PageResult<>(records, total, current, size);
    }
    
    @Override
    public Reservation getByOrderNo(String orderNo) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getOrderNo, orderNo);
        return getOne(wrapper);
    }
    
    @Override
    public ReservationVO getReservationDetailByOrderNo(String orderNo) {
        return baseMapper.selectReservationDetailByOrderNo(orderNo);
    }
    
    @Override
    @Transactional
    public void handleExpiredReservations() {
        List<Reservation> expiredReservations = baseMapper.selectExpiredReservations();
        
        for (Reservation reservation : expiredReservations) {
            reservation.setStatus("爽约");
            updateById(reservation);
        }
        
        log.info("处理过期预约记录：" + expiredReservations.size() + " 条");
    }
    
    @Override
    @Transactional
    public void handleSoonExpiredReservations() {
        List<Reservation> soonExpiredReservations = baseMapper.selectSoonExpiredReservations();
        
        for (Reservation reservation : soonExpiredReservations) {
            reservation.setStatus("爽约");
            updateById(reservation);
        }
        
        log.info("处理即将过期预约记录：" + soonExpiredReservations.size() + " 条");
    }
    
    /**
     * 生成预约流水号
     */
    private synchronized String generateOrderNo() {
        // 获取当日流水号
        SystemConfig config = systemConfigService.getByKey("daily_order_sequence");
        int sequence = Integer.parseInt(config.getConfigValue()) + 1;
        
        // 更新流水号
        config.setConfigValue(String.valueOf(sequence));
        systemConfigService.updateById(config);
        
        return OrderNoUtils.generateOrderNo(sequence);
    }
}