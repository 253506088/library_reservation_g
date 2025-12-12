package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.dto.LoginDTO;
import com.library.dto.RegisterDTO;
import com.library.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     */
    User login(LoginDTO loginDTO);
    
    /**
     * 用户注册
     */
    User register(RegisterDTO registerDTO);
    
    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);
}