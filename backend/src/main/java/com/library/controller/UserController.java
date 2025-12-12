package com.library.controller;

import com.library.entity.User;
import com.library.service.UserService;
import com.library.vo.PageResult;
import com.library.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    public Result<PageResult<User>> page(@RequestParam(defaultValue = "1") int current,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String username,
                                        @RequestParam(required = false) String realName,
                                        @RequestParam(required = false) String userType,
                                        @RequestParam(required = false) String status,
                                        HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.getUserType())) {
            return Result.error(403, "只有管理员可以查看用户列表");
        }
        
        // 使用手写SQL分页查询
        PageResult<User> result = userService.getUserPageQuery(
            current, size, username, realName, userType, status);
        return Result.success(result);
    }
    
    /**
     * 切换用户状态（启用/禁用）
     */
    @PutMapping("/{id}/toggle-status")
    public Result<Object> toggleStatus(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.getUserType())) {
            return Result.error(403, "只有管理员可以操作用户状态");
        }
        
        try {
            userService.toggleUserStatus(id);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}