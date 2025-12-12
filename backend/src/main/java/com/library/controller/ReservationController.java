package com.library.controller;

import com.library.dto.ReservationDTO;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.service.ReservationService;
import com.library.vo.PageResult;
import com.library.vo.ReservationVO;
import com.library.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * 预约控制器
 */
@RestController
@RequestMapping("/reservation")
@CrossOrigin
public class ReservationController {
    
    @Autowired
    private ReservationService reservationService;
    
    /**
     * 创建预约
     */
    @PostMapping
    public Result<Reservation> create(@Validated @RequestBody ReservationDTO reservationDTO, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        
        if (!"学生".equals(user.getUserType())) {
            return Result.error(403, "只有学生可以预约座位");
        }
        
        try {
            Reservation reservation = reservationService.createReservation(user.getId(), reservationDTO);
            return Result.success("预约成功", reservation);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消预约
     */
    @PutMapping("/{id}/cancel")
    public Result<Object> cancel(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        
        try {
            if ("学生".equals(user.getUserType())) {
                reservationService.cancelReservation(id, user.getId());
            } else if ("管理员".equals(user.getUserType())) {
                reservationService.adminCancelReservation(id);
            } else {
                return Result.error(403, "无权限操作");
            }
            return Result.success("取消成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 扫码签到
     */
    @PostMapping("/checkin")
    public Result<Object> checkIn(@RequestParam String orderNo, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.getUserType())) {
            return Result.error(403, "只有管理员可以扫码验证");
        }
        
        try {
            reservationService.checkIn(orderNo);
            return Result.success("签到成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 分页查询预约记录
     */
    @GetMapping("/page")
    public Result<PageResult<ReservationVO>> page(@RequestParam(defaultValue = "1") int current,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(required = false) String status,
                                                 HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        
        Long userId = null;
        if ("学生".equals(user.getUserType())) {
            // 学生只能查看自己的预约记录
            userId = user.getId();
        }
        // 管理员可以查看所有预约记录，userId为null
        
        // 使用手写SQL分页查询
        PageResult<ReservationVO> result = reservationService.getReservationPageQuery(current, size, userId, status);
        return Result.success(result);
    }
    
    /**
     * 根据流水号查询预约记录
     */
    @GetMapping("/order/{orderNo}")
    public Result<Reservation> getByOrderNo(@PathVariable String orderNo) {
        Reservation reservation = reservationService.getByOrderNo(orderNo);
        if (reservation == null) {
            return Result.error("预约记录不存在");
        }
        return Result.success(reservation);
    }
    
    /**
     * 根据流水号查询预约记录详情
     */
    @GetMapping("/detail/{orderNo}")
    public Result<ReservationVO> getDetailByOrderNo(@PathVariable String orderNo) {
        ReservationVO reservation = reservationService.getReservationDetailByOrderNo(orderNo);
        if (reservation == null) {
            return Result.error("预约记录不存在");
        }
        return Result.success(reservation);
    }
}