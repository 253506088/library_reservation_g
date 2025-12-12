package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.Seat;
import com.library.mapper.ReservationMapper;
import com.library.mapper.SeatMapper;
import com.library.service.SeatService;
import com.library.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 座位服务实现类
 */
@Service
public class SeatServiceImpl extends ServiceImpl<SeatMapper, Seat> implements SeatService {
    
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Override
    public List<Seat> getByLibraryId(Long libraryId) {
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Seat::getLibraryId, libraryId)
               .eq(Seat::getStatus, "正常");
        return list(wrapper);
    }
    
    @Override
    public List<Seat> getAvailableSeats(Long libraryId, LocalDateTime startTime, LocalDateTime endTime) {
        // 获取图书馆所有正常状态的座位
        List<Seat> allSeats = getByLibraryId(libraryId);
        
        // 过滤掉在指定时间段内有冲突的座位
        return allSeats.stream()
                .filter(seat -> {
                    int conflictCount = reservationMapper.checkSeatTimeConflict(
                            seat.getId(), startTime, endTime, null);
                    return conflictCount == 0;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResult<Seat> pageQuery(int current, int size, Long libraryId, String seatNumber) {
        // 计算偏移量
        int offset = (current - 1) * size;
        
        // 查询数据
        List<Seat> records = baseMapper.selectPageWithCondition(libraryId, seatNumber, offset, size);
        
        // 查询总数
        long total = baseMapper.countWithCondition(libraryId, seatNumber);
        
        // 返回分页结果
        return new PageResult<>(records, total, current, size);
    }
}