package com.library.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.Library;
import com.library.mapper.LibraryMapper;
import com.library.service.LibraryService;
import org.springframework.stereotype.Service;

/**
 * 图书馆服务实现类
 */
@Service
public class LibraryServiceImpl extends ServiceImpl<LibraryMapper, Library> implements LibraryService {
}