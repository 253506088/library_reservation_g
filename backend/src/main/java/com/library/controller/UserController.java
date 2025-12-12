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
    
    /**
     * 修改用户类型
     */
    @PutMapping("/{id}/change-type")
    public Result<Object> changeUserType(@PathVariable Long id, 
                                        @RequestBody java.util.Map<String, String> request, 
                                        HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.getUserType())) {
            return Result.error(403, "只有管理员可以修改用户类型");
        }
        
        String userType = request.get("userType");
        if (userType == null || (!userType.equals("学生") && !userType.equals("管理员"))) {
            return Result.error("用户类型只能是'学生'或'管理员'");
        }
        
        try {
            userService.changeUserType(id, userType);
            return Result.success("用户类型修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}