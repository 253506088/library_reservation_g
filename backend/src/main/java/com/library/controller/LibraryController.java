package com.library.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Library;
import com.library.entity.User;
import com.library.service.LibraryService;
import com.library.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 图书馆控制器
 */
@RestController
@RequestMapping("/library")
@CrossOrigin
public class LibraryController {
    
    @Autowired
    private LibraryService libraryService;
    
    /**
     * 查询所有图书馆（学生端使用）
     */
    @GetMapping("/list")
    public Result<List<Library>> list() {
        LambdaQueryWrapper<Library> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Library::getStatus, "正常");
        List<Library> libraries = libraryService.list(wrapper);
        return Result.success(libraries);
    }
    
    /**
     * 分页查询图书馆（管理员端使用）
     */
    @GetMapping("/page")
    public Result<IPage<Library>> page(@RequestParam(defaultValue = "1") int current,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String name,
                                       HttpSession session) {
        // 检查管理员权限
        User user = (User) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.getUserType())) {
            return Result.error(403, "无权限访问");
        }
        
        Page<Library> page = new Page<>(current, size);
        LambdaQueryWrapper<Library> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(Library::getName, name);
        }
        wrapper.orderByDesc(Library::getCreateTime);
        
        IPage<Library> result = libraryService.page(page, wrapper);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询图书馆
     */
    @GetMapping("/{id}")
    public Result<Library> getById(@PathVariable Long id) {
        Library library = libraryService.getById(id);
        if (library == null) {
            return Result.error("图书馆不存在");
        }
        return Result.success(library);
    }
    
    /**
     * 新增图书馆
     */
    @PostMapping
    public Result<Object> save(@RequestBody Library library, HttpSession session) {
        // 检查管理员权限
        User user = (User) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.getUserType())) {
            return Result.error(403, "无权限操作");
        }
        
        library.setStatus("正常");
        libraryService.save(library);
        return Result.success("新增成功");
    }
    
    /**
     * 更新图书馆
     */
    @PutMapping("/{id}")
    public Result<Object> update(@PathVariable Long id, @RequestBody Library library, HttpSession session) {
        // 检查管理员权限
        User user = (User) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.getUserType())) {
            return Result.error(403, "无权限操作");
        }
        
        library.setId(id);
        libraryService.updateById(library);
        return Result.success("更新成功");
    }
    
    /**
     * 删除图书馆
     */
    @DeleteMapping("/{id}")
    public Result<Object> delete(@PathVariable Long id, HttpSession session) {
        // 检查管理员权限
        User user = (User) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.getUserType())) {
            return Result.error(403, "无权限操作");
        }
        
        libraryService.removeById(id);
        return Result.success("删除成功");
    }
}