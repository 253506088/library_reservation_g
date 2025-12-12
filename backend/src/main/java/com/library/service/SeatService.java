package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.Seat;
import com.library.vo.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 座位服务接口
 */
public interface SeatService extends IService<Seat> {
    
    /**
     * 根据图书馆ID查询座位列表
     */
    List<Seat> getByLibraryId(Long libraryId);
    
    /**
     * 查询可用座位（指定时间段内未被预约的座位）
     */
    List<Seat> getAvailableSeats(Long libraryId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 分页查询座位
     */
    PageResult<Seat> pageQuery(int current, int size, Long libraryId, String seatNumber, String seatType, String status);
}