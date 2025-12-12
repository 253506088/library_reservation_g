package com.library.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.Seat;
import com.library.entity.User;
import com.library.service.SeatService;
import com.library.vo.PageResult;
import com.library.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 座位控制器
 */
@RestController
@RequestMapping("/seat")
@CrossOrigin
public class SeatController {
    
    @Autowired
    private SeatService seatService;
    
    /**
     * 根据图书馆ID查询座位列表
     */
    @GetMapping("/library/{libraryId}")
    public Result<List<Seat>> getByLibraryId(@PathVariable Long libraryId) {
        List<Seat> seats = seatService.getByLibraryId(libraryId);
        return Result.success(seats);
    }
    
    /**
     * 查询可用座位
     */
    @GetMapping("/available")
    public Result<List<Seat>> getAvailableSeats(@RequestParam Long libraryId,
                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<Seat> seats = seatService.getAvailableSeats(libraryId, startTime, endTime);
        return Result.success(seats);
    }
    
    /**
     * 分页查询座位（管理员端使用）
     */
    @GetMapping("/page")
    public Result<PageResult<Seat>> page(@RequestParam(defaultValue = "1") int current,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) Long libraryId,
                                        @RequestParam(required = false) String seatNumber,
                                        @RequestParam(required = false) String seatType,
                                        @RequestParam(required = false) String status,
                                        HttpSession session) {
        // 检查管理员权限
        User user = (User) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.getUserType())) {
            return Result.error(403, "无权限访问");
        }
        
        // 使用手写SQL分页查询
        PageResult<Seat> result = seatService.pageQuery(current, size, libraryId, seatNumber, seatType, status);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询座位
     */
    @GetMapping("/{id}")
    public Result<Seat> getById(@PathVariable Long id) {
        Seat seat = seatService.getById(id);
        if (seat == null) {
            return Result.error("座位不存在");
        }
        return Result.success(seat);
    }
    
    /**
     * 新增座位
     */
    @PostMapping
    public Result<Object> save(@RequestBody Seat seat, HttpSession session) {
        // 检查管理员权限
        User user = (User) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.getUserType())) {
            return Result.error(403, "无权限操作");
        }
        
        // 检查座位编号是否重复
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Seat::getLibraryId, seat.getLibraryId())
               .eq(Seat::getSeatNumber, seat.getSeatNumber());
        if (seatService.count(wrapper) > 0) {
            return Result.error("该图书馆已存在相同编号的座位");
        }
        
        seat.setStatus("正常");
        seatService.save(seat);
        return Result.success("新增成功");
    }
    
    /**
     * 更新座位
     */
    @PutMapping("/{id}")
    public Result<Object> update(@PathVariable Long id, @RequestBody Seat seat, HttpSession session) {
        // 检查管理员权限
        User user = (User) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.getUserType())) {
            return Result.error(403, "无权限操作");
        }
        
        // 检查座位编号是否重复（排除自己）
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Seat::getLibraryId, seat.getLibraryId())
               .eq(Seat::getSeatNumber, seat.getSeatNumber())
               .ne(Seat::getId, id);
        if (seatService.count(wrapper) > 0) {
            return Result.error("该图书馆已存在相同编号的座位");
        }
        
        seat.setId(id);
        seatService.updateById(seat);
        return Result.success("更新成功");
    }
    
    /**
     * 删除座位
     */
    @DeleteMapping("/{id}")
    public Result<Object> delete(@PathVariable Long id, HttpSession session) {
        // 检查管理员权限
        User user = (User) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.getUserType())) {
            return Result.error(403, "无权限操作");
        }
        
        seatService.removeById(id);
        return Result.success("删除成功");
    }
}